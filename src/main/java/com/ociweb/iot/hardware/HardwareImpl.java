package com.ociweb.iot.hardware;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.impl.DirectHardwareAnalogDigitalOutputStage;
import com.ociweb.iot.hardware.impl.edison.EdisonConstants;
//github.com/oci-pronghorn/PronghornIoT.git
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.pronghorn.TrafficCopStage;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.ReadDeviceInputStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.i2c.I2CJFFIStage;
import com.ociweb.pronghorn.iot.i2c.PureJavaI2CStage;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.MessageSubscription;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.util.hash.IntHashTable;
import com.ociweb.pronghorn.stage.route.SplitterStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.util.Blocker;
import com.ociweb.pronghorn.util.math.PMath;
import com.ociweb.pronghorn.util.math.ScriptedSchedule;

public abstract class HardwareImpl implements Hardware {


	private static final int MAX_MOVING_AVERAGE_SUPPORTED = 101; //TOOD: is this still needed, remove???

	private static final HardwareConnection[] EMPTY = new HardwareConnection[0];

	protected boolean configI2C;       //Humidity, LCD need I2C address so..
	protected long debugI2CRateLastTime;

	protected HardwareConnection[] digitalInputs; //Button, Motion
	protected HardwareConnection[] digitalOutputs;//Relay Buzzer

	protected HardwareConnection[] analogInputs;  //Light, UV, Moisture
	protected HardwareConnection[] pwmOutputs;    //Servo   //(only 3, 5, 6, 9, 10, 11 when on edison)

	protected I2CConnection[] i2cInputs;
	protected I2CConnection[] i2cOutputs;

	private long timeTriggerRate;
	private Blocker channelBlocker;

	public final GraphManager gm;

	private static final int DEFAULT_LENGTH = 16;
	private static final int DEFAULT_PAYLOAD_SIZE = 128;

	protected final PipeConfig<TrafficReleaseSchema> releasePipesConfig          = new PipeConfig<TrafficReleaseSchema>(TrafficReleaseSchema.instance, DEFAULT_LENGTH);
	protected final PipeConfig<TrafficOrderSchema> orderPipesConfig          = new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, DEFAULT_LENGTH);
	protected final PipeConfig<TrafficAckSchema> ackPipesConfig = new PipeConfig<TrafficAckSchema>(TrafficAckSchema.instance, DEFAULT_LENGTH);
	protected final PipeConfig<GroveResponseSchema> groveResponseConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, DEFAULT_LENGTH, DEFAULT_PAYLOAD_SIZE);
	protected final PipeConfig<I2CResponseSchema> i2CResponseSchemaConfig = new PipeConfig<I2CResponseSchema>(I2CResponseSchema.instance, DEFAULT_LENGTH, DEFAULT_PAYLOAD_SIZE);

	public final I2CBacking i2cBacking;

	protected static final long MS_TO_NS = 1_000_000;


	private static final Logger logger = LoggerFactory.getLogger(HardwareImpl.class);

	Enum<?> beginningState;


	/////////////////
	///Pipes for initial startup declared subscriptions. (Not part of graph)
	private final int maxStartupSubs = 64;
	private final int maxTopicLengh  = 128;
	private Pipe<MessagePubSub> tempPipeOfStartupSubscriptions;
	/////////////////
	/////////////////

	protected ReentrantLock devicePinConfigurationLock = new ReentrantLock();




	public HardwareImpl(GraphManager gm, I2CBacking i2cBacking) {
		this(gm, i2cBacking, false,false,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY);
	}

	protected HardwareImpl(GraphManager gm, I2CBacking i2cBacking, boolean publishTime, boolean configI2C, HardwareConnection[] multiDigitalInput,
			HardwareConnection[] digitalInputs, HardwareConnection[] digitalOutputs, HardwareConnection[] pwmOutputs, HardwareConnection[] analogInputs) {

		this.i2cBacking = i2cBacking;

		this.configI2C = configI2C; //may be removed.

		this.digitalInputs = digitalInputs;
		this.digitalOutputs = digitalOutputs;
		this.pwmOutputs = pwmOutputs;
		this.analogInputs = analogInputs;
		this.gm = gm;

		this.getTempPipeOfStartupSubscriptions().initBuffers();
	}

	public <E extends Enum<E>> boolean isValidState(E state) {

		if (null!=beginningState) {
			return beginningState.getClass()==state.getClass();    		
		}
		return false;
	}


	public static I2CBacking getI2CBacking(byte deviceNum) {
		try {
			return new I2CNativeLinuxBacking(deviceNum);
		} catch (Throwable t) {
			//avoid non error case that is used to detect which hardware is running.
			if ((null==t.getMessage()) || !t.getMessage().contains("Could not open")) {
				logger.error("unable to find binary bindings ", t);
			}
			return null;
		}
	}

	protected HardwareConnection[] growHardwareConnections(HardwareConnection[] original, HardwareConnection toAdd) {
		final int len = original.length;
		//Validate that what we are adding is safe
		int i = len;
		while (--i>=0) {
			if (original[i].connection == toAdd.connection) {
				throw new UnsupportedOperationException("This connection "+toAdd.connection+" already has attachment "+original[i].twig+" so the attachment "+toAdd.twig+" can not be added.");
			}
		}

		//Grow the array
		HardwareConnection[] result = new HardwareConnection[len+1];
		System.arraycopy(original, 0, result, 0, len);
		result[len] = toAdd;
		return result;
	}

	protected I2CConnection[] growI2CConnections(I2CConnection[] original, I2CConnection toAdd){
		System.out.println("Adding I2C Connection");
		if (null==original) {
			return new I2CConnection[] {toAdd};
		} else {
			int l = original.length;
			I2CConnection[] result = new I2CConnection[l+1];
			System.arraycopy(original, 0, result, 0, l);
			result[l] = toAdd;
			return result;
		}
	}

	public Hardware connectAnalog(IODevice t, int connection) {
		return connectAnalog(t,connection,-1);
	}

	public Hardware connectAnalog(IODevice t, int connection, int customRate) {
		if (t.isInput()) {
			assert(!t.isOutput());
			connectAnalogInput(t, connection, customRate, -1, false);
		} else {
			assert(t.isOutput());
			connectAnalogOutput(t, connection, customRate, -1, false);
		}
		return this;
	}  

	public Hardware connectAnalog(IODevice t, int connection, int customRate, int customAverageMS) {
		return connectAnalog(t,connection,customRate,customAverageMS, true); 
	}

	public Hardware connectAnalog(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if (t.isInput()) {
			assert(!t.isOutput());
			connectAnalogInput(t, connection, customRate, customAverageMS, everyValue);
		} else {
			assert(t.isOutput());
			connectAnalogOutput(t, connection, customRate, customAverageMS, everyValue);
		}
		return this;
	}  

	public Hardware connectDigital(IODevice t, int connection) {
		return connectDigital(t,connection,-1);
	}

	public Hardware connectDigital(IODevice t, int connection, int customRate) {
		if (t.isInput()) {
			assert(!t.isOutput());
			connectDigitalInput(t, connection, customRate, -1, false);
		} else {
			assert(t.isOutput());
			connectDigitalOutput(t, connection, customRate, -1, false);
		}
		return this;
	}  

	public Hardware connectDigital(IODevice t, int connection, int customRate, int customAverageMS) {
		return connectDigital(t,connection,customRate,customAverageMS, true); 
	}

	public Hardware connectDigital(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if (t.isInput()) {
			assert(!t.isOutput());
			connectDigitalInput(t, connection, customRate, customAverageMS, everyValue);
		} else {
			assert(t.isOutput());
			connectDigitalOutput(t, connection, customRate, customAverageMS, everyValue);
		}
		return this;
	}  

	protected void connectAnalogOutput(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if(customAverageMS > 0){
			pwmOutputs = growHardwareConnections(pwmOutputs, new HardwareConnection(t,connection, customRate, customAverageMS, everyValue));
		}else{
			pwmOutputs = growHardwareConnections(pwmOutputs, new HardwareConnection(t,connection, customRate));
		}
	}

	protected void connectAnalogInput(IODevice t, int connection, int customRate, int customAverageMS,
			boolean everyValue) {
		if(customAverageMS > 0){
			analogInputs = growHardwareConnections(analogInputs, new HardwareConnection(t,connection, customRate, customAverageMS, everyValue));
		}else{
			analogInputs = growHardwareConnections(analogInputs, new HardwareConnection(t,connection, customRate));
		}
	}

	protected void connectDigitalOutput(IODevice t, int connection, int customRate, int customAverageMS,
			boolean everyValue) {
		if(customAverageMS > 0){
			digitalOutputs = growHardwareConnections(digitalOutputs, new HardwareConnection(t,connection, customRate, customAverageMS, everyValue));
		}else{
			digitalOutputs = growHardwareConnections(digitalOutputs, new HardwareConnection(t,connection, customRate));
		}
	}

	protected void connectDigitalInput(IODevice t, int connection, int customRate, int customAverageMS,
			boolean everyValue) {
		if(customAverageMS > 0){
			digitalInputs = growHardwareConnections(digitalInputs, new HardwareConnection(t,connection, customRate, customAverageMS, everyValue));
		}else{
			digitalInputs = growHardwareConnections(digitalInputs, new HardwareConnection(t,connection, customRate));
		}
	}

	public Hardware connectI2C(IODevice t){ 
		logger.debug("Connecting I2C Device "+t.getClass());
		if(t.isInput()){
			assert(!t.isOutput());
			i2cInputs = growI2CConnections(i2cInputs, t.getI2CConnection());
		}else if(t.isOutput()){
			assert(!t.isInput());
			i2cOutputs = growI2CConnections(i2cOutputs, t.getI2CConnection());
		}
		return this;
	}

	public <E extends Enum<E>> Hardware startStateMachineWith(E state) {   	
		beginningState = state;	
		return this;
	}

	public Hardware setTriggerRate(long rateInMS) {
		timeTriggerRate = rateInMS;
		return this;
	}

	public Hardware useI2C() {
		this.configI2C = true;
		return this;
	}


	public long getTriggerRate() {
		return timeTriggerRate;
	}

	public abstract int digitalRead(int connector); //Platform specific
	public abstract int analogRead(int connector); //Platform specific
	public abstract void digitalWrite(int connector, int value); //Platform specific
	public abstract void analogWrite(int connector, int value); //Platform specific
	public abstract ReactiveListenerStage createReactiveListener(GraphManager gm,  Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes);

	public int maxAnalogMovingAverage() {
		return MAX_MOVING_AVERAGE_SUPPORTED;
	}


	public void coldSetup(){
		//TODO: I2C Setup methods
	};

	protected HardwareConnection[] buildUsedLines() {

		HardwareConnection[] result = new HardwareConnection[digitalInputs.length+
		                                                     digitalOutputs.length+
		                                                     pwmOutputs.length+
		                                                     analogInputs.length+
		                                                     (configI2C?2:0)];

		int pos = 0;
		System.arraycopy(digitalInputs, 0, result, pos, digitalInputs.length);
		pos+=digitalInputs.length;

		findDup(result,pos,digitalOutputs, false);
		System.arraycopy(digitalOutputs, 0, result, pos, digitalOutputs.length);
		pos+=digitalOutputs.length;

		findDup(result,pos,pwmOutputs, false);
		System.arraycopy(pwmOutputs, 0, result, pos, pwmOutputs.length);
		pos+=pwmOutputs.length;        

		findDup(result,pos,analogInputs, true);
		int j = analogInputs.length;
		while (--j>=0) {
			result[pos++] = new HardwareConnection(analogInputs[j].twig,(int) EdisonConstants.ANALOG_CONNECTOR_TO_PIN[analogInputs[j].connection]);
		}

		if (configI2C) {
			findDup(result,pos,EdisonConstants.i2cPins, false);
			System.arraycopy(EdisonConstants.i2cPins, 0, result, pos, EdisonConstants.i2cPins.length);
			pos+=EdisonConstants.i2cPins.length;
		}

		return result;
	}

	private static void findDup(HardwareConnection[] base, int baseLimit, HardwareConnection[] items, boolean mapAnalogs) {
		int i = items.length;
		while (--i>=0) {
			int j = baseLimit;
			while (--j>=0) {
				if (mapAnalogs ? base[j].connection ==  EdisonConstants.ANALOG_CONNECTOR_TO_PIN[items[i].connection] :  base[j]==items[i]) {
					throw new UnsupportedOperationException("Connector "+items[i]+" is assigned more than once.");
				}
			}
		}     
	}

	public abstract CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> orderPipe);

	static final boolean debug = false;

	public void shutdown() {
		// TODO The caller would like to stop the operating system cold, need platform specific call?
	}



	public final void buildStages(
			IntHashTable subscriptionPipeLookup,
			Pipe<GroveResponseSchema>[] responsePipes,     //one for each listener of this type (broadcast to all)
			Pipe<I2CResponseSchema>[] i2cResponsePipes,    //one for each listener of this type (broadcast to all)
			Pipe<MessageSubscription>[] subscriptionPipes, //one for each listener of this type (subscription per pipe)

			Pipe<TrafficOrderSchema>[] orderPipes,   //one for each command channel 

			Pipe<GroveRequestSchema>[] requestPipes, //one for each command channel 
			Pipe<I2CCommandSchema>[] i2cPipes,       //one for each command channel 
			Pipe<MessagePubSub>[] messagePubSub      //one for each command channel 

			) {


		assert(orderPipes.length == i2cPipes.length);
		assert(orderPipes.length == requestPipes.length);


		int t = orderPipes.length;

		Pipe<TrafficReleaseSchema>[]          masterI2CgoOut = new Pipe[t];
		Pipe<TrafficAckSchema>[]              masterI2CackIn = new Pipe[t]; 

		Pipe<TrafficReleaseSchema>[]          masterPINgoOut = new Pipe[t];
		Pipe<TrafficAckSchema>[]              masterPINackIn = new Pipe[t]; 

		Pipe<TrafficReleaseSchema>[]          masterMsggoOut = new Pipe[t];
		Pipe<TrafficAckSchema>[]              masterMsgackIn = new Pipe[t]; 



		while (--t>=0) {

			Pipe<TrafficReleaseSchema> i2cGoPipe = new Pipe<TrafficReleaseSchema>(releasePipesConfig);
			Pipe<TrafficReleaseSchema> pinGoPipe = new Pipe<TrafficReleaseSchema>(releasePipesConfig);
			Pipe<TrafficReleaseSchema> msgGoPipe = new Pipe<TrafficReleaseSchema>(releasePipesConfig);

			Pipe<TrafficAckSchema> i2cAckPipe = new Pipe<TrafficAckSchema>(ackPipesConfig);
			Pipe<TrafficAckSchema> pinAckPipe = new Pipe<TrafficAckSchema>(ackPipesConfig);
			Pipe<TrafficAckSchema> msgAckPipe = new Pipe<TrafficAckSchema>(ackPipesConfig);

			masterI2CgoOut[t] = i2cGoPipe;
			masterI2CackIn[t] = i2cAckPipe;

			masterPINgoOut[t] = pinGoPipe;
			masterPINackIn[t] = pinAckPipe;            

			masterMsggoOut[t] = msgGoPipe;
			masterMsgackIn[t] = msgAckPipe;  

			Pipe<TrafficReleaseSchema>[] goOut = new Pipe[]{pinGoPipe, i2cGoPipe, msgGoPipe};
			Pipe<TrafficAckSchema>[] ackIn = new Pipe[]{pinAckPipe, i2cAckPipe, msgAckPipe};
			long timeout = 20_000; //20 seconds
			TrafficCopStage trafficCopStage = new TrafficCopStage(gm, timeout, orderPipes[t], ackIn, goOut);

		}

		createMessagePubSubStage(subscriptionPipeLookup, messagePubSub, masterMsggoOut, masterMsgackIn, subscriptionPipes);

		createADOutputStage(requestPipes, masterPINgoOut, masterPINackIn);

		//only build and connect I2C if it is used for either in or out  
		Pipe<I2CResponseSchema> masterI2CResponsePipe = null;
		if (i2cResponsePipes.length>0) {
			masterI2CResponsePipe = new Pipe<I2CResponseSchema>(i2CResponseSchemaConfig);
			SplitterStage i2cResponseSplitter = new SplitterStage<I2CResponseSchema>(gm, masterI2CResponsePipe, i2cResponsePipes);   
		}
		if (i2cPipes.length>0 || (null!=masterI2CResponsePipe)) {
			createI2COutputInputStage(i2cPipes, masterI2CgoOut, masterI2CackIn, masterI2CResponsePipe);
		}

		//only build and connect gpio responses if it is used
		if (responsePipes.length>0) {

			if (!hasDigitalOrAnalogInputs()) {
				//we have listeners but there are no inputs connected.

				//TODO: must check even earlier to remove these.

			}

			Pipe<GroveResponseSchema> masterResponsePipe = new Pipe<GroveResponseSchema>(groveResponseConfig);
			SplitterStage responseSplitter = new SplitterStage<GroveResponseSchema>(gm, masterResponsePipe, responsePipes);      
			createADInputStage(masterResponsePipe);



		}

	}

	private void createMessagePubSubStage(IntHashTable subscriptionPipeLookup,
			Pipe<MessagePubSub>[] messagePubSub,
			Pipe<TrafficReleaseSchema>[] masterMsggoOut, 
			Pipe<TrafficAckSchema>[] masterMsgackIn, 
			Pipe<MessageSubscription>[] subscriptionPipes) {


		new MessagePubSubStage(this.gm, subscriptionPipeLookup, this, messagePubSub, masterMsggoOut, masterMsgackIn, subscriptionPipes);


	}

	protected void createADInputStage(Pipe<GroveResponseSchema> masterResponsePipe) {
		//NOTE: rate is NOT set since stage sets and configs its own rate based on polling need.
		ReadDeviceInputStage adInputStage = new ReadDeviceInputStage(this.gm, masterResponsePipe, this);
	}

	protected void createI2COutputInputStage(Pipe<I2CCommandSchema>[] i2cPipes,
			Pipe<TrafficReleaseSchema>[] masterI2CgoOut, Pipe<TrafficAckSchema>[] masterI2CackIn, Pipe<I2CResponseSchema> masterI2CResponsePipe) {

		if (hasI2CInputs()) {
			I2CJFFIStage i2cJFFIStage = new I2CJFFIStage(gm, masterI2CgoOut, i2cPipes, masterI2CackIn, masterI2CResponsePipe, this);
		} else {
			//TODO: build an output only version of this stage because there is nothing to read
			I2CJFFIStage i2cJFFIStage = new I2CJFFIStage(gm, masterI2CgoOut, i2cPipes, masterI2CackIn, masterI2CResponsePipe, this);
		}
	}

	protected void createADOutputStage(Pipe<GroveRequestSchema>[] requestPipes, Pipe<TrafficReleaseSchema>[] masterPINgoOut, Pipe<TrafficAckSchema>[] masterPINackIn) {
		DirectHardwareAnalogDigitalOutputStage adOutputStage = new DirectHardwareAnalogDigitalOutputStage(gm, requestPipes, masterPINgoOut, masterPINackIn, this);
	}

	public StageScheduler createScheduler(DeviceRuntime iotDeviceRuntime) {
		//NOTE: need to consider different schedulers in the future.
		final StageScheduler scheduler = new ThreadPerStageScheduler(gm);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				scheduler.shutdown();
				scheduler.awaitTermination(30, TimeUnit.MINUTES);
			}
		});
		return scheduler;
	}

	public boolean isListeningToI2C(Object listener) {
		return listener instanceof I2CListener;
	}

	public boolean isListeningToPins(Object listener) {
		return listener instanceof DigitalListener || listener instanceof AnalogListener || listener instanceof RotaryListener;
	}

	public boolean isListeningToSubscription(Object listener) {
		return listener instanceof PubSubListener;
	}

	/**
	 * access to system time.  This method is required so it can be monitored and simulated by unit tests.
	 * @return
	 */
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	public void initChannelBlocker(int channelsCount) {
		channelBlocker = new Blocker(channelsCount);
	}

	public void blockChannelUntil(int channelId, long timeInMillis) {        
		channelBlocker.until(channelId, timeInMillis);
	}

	public boolean isChannelBlocked(int channelId) {
		return channelBlocker.isBlocked(channelId);
	}

	public void releaseChannelBlocks(long now) {
		channelBlocker.releaseBlocks(now);
	}

	public long nanoTime() {
		return System.nanoTime();
	}

	public Enum[] getStates() {
		return null==beginningState? new Enum[0] : beginningState.getClass().getEnumConstants();
	}

	public void addStartupSubscription(CharSequence topic, int systemHash) {

		Pipe<MessagePubSub> pipe = getTempPipeOfStartupSubscriptions();

		if (PipeWriter.tryWriteFragment(pipe, MessagePubSub.MSG_SUBSCRIBE_100)) {
			PipeWriter.writeUTF8(pipe, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_TOPIC_1, topic);
			PipeWriter.writeInt(pipe, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_SUBSCRIBERIDENTITYHASH_4, systemHash);
			PipeWriter.publishWrites(pipe);
		} else {
			throw new UnsupportedOperationException("Limited number of startup subscriptions "+maxStartupSubs+" encountered.");
		}
	}

	private Pipe<MessagePubSub> getTempPipeOfStartupSubscriptions() {
		if (null==tempPipeOfStartupSubscriptions) {

			final PipeConfig<MessagePubSub> messagePubSubConfig = new PipeConfig<MessagePubSub>(MessagePubSub.instance, maxStartupSubs,maxTopicLengh);   
			tempPipeOfStartupSubscriptions = new Pipe<MessagePubSub>(messagePubSubConfig);

		}		

		return tempPipeOfStartupSubscriptions;
	}

	Pipe<MessagePubSub> consumeStartupSubscriptions() {
		Pipe<MessagePubSub> result = tempPipeOfStartupSubscriptions;
		tempPipeOfStartupSubscriptions = null;//no longer needed
		return result;
	}

	public boolean hasI2CInputs() {
		return this.i2cInputs!=null && this.i2cInputs.length>0;
	}

	public I2CConnection[] getI2CInputs() {
		return null==i2cInputs?new I2CConnection[0]:i2cInputs;
	}

	public HardwareConnection[] getAnalogInputs() {
		return analogInputs;
	}

	public HardwareConnection[] getDigitalInputs() {
		return digitalInputs;
	}

	public ScriptedSchedule buildI2CPollSchedule() {
		I2CConnection[] localInputs = getI2CInputs();
		System.out.println(localInputs.length);
		long[] schedulePeriods = new long[localInputs.length];
		for (int i = 0; i < localInputs.length; i++) {
			schedulePeriods[i] = localInputs[i].responseMS*MS_TO_NS;
		}
		logger.info(""+schedulePeriods.length);
		System.out.println("known I2C rates: "+Arrays.toString(schedulePeriods));
		return PMath.buildScriptedSchedule(schedulePeriods);

	}

	public boolean hasDigitalOrAnalogInputs() {
		return (analogInputs.length+digitalInputs.length)>0;
	}

	public HardwareConnection[] combinedADConnections() {
		HardwareConnection[] localAInputs = getAnalogInputs();
		HardwareConnection[] localDInputs = getDigitalInputs();

		int totalCount = localAInputs.length + localDInputs.length;

		HardwareConnection[] results = new HardwareConnection[totalCount];
		System.arraycopy(localAInputs, 0, results, 0,                   localAInputs.length);
		System.arraycopy(localDInputs, 0, results, localAInputs.length, localDInputs.length);

		return results;
	}

	public ScriptedSchedule buildADPollSchedule() {
		HardwareConnection[] localAInputs = getAnalogInputs();
		HardwareConnection[] localDInputs = getDigitalInputs();

		int totalCount = localAInputs.length + localDInputs.length;
		if (0==totalCount) {
			return null;
		}

		long[] schedulePeriods = new long[totalCount];
		int j = 0;
		for (int i = 0; i < localAInputs.length; i++) {
			schedulePeriods[j++] = localAInputs[i].responseMS*MS_TO_NS;
		}
		for (int i = 0; i < localDInputs.length; i++) {
			schedulePeriods[j++] = localDInputs[i].responseMS*MS_TO_NS;
		}
		//analogs then the digitals

		return PMath.buildScriptedSchedule(schedulePeriods);

	}


}