package com.ociweb.iot.hardware;

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
import com.ociweb.iot.maker.HTTPResponseListener;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.iot.maker.StateChangeListener;
import com.ociweb.iot.maker.TimeTrigger;
import com.ociweb.pronghorn.iot.HTTPClientRequestStage;
import com.ociweb.pronghorn.iot.MessagePubSubStage;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.ReadDeviceInputStage;
import com.ociweb.pronghorn.iot.TrafficCopStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.i2c.I2CJFFIStage;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.network.ClientCoordinator;
import com.ociweb.pronghorn.network.NetGraphBuilder;
import com.ociweb.pronghorn.network.schema.NetPayloadSchema;
import com.ociweb.pronghorn.network.schema.NetPayloadSchema;
import com.ociweb.pronghorn.network.schema.ReleaseSchema;
import com.ociweb.pronghorn.network.schema.NetRequestSchema;
import com.ociweb.pronghorn.network.schema.NetResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.util.hash.IntHashTable;
import com.ociweb.pronghorn.schema.MessagePubSub;
import com.ociweb.pronghorn.schema.MessageSubscription;
import com.ociweb.pronghorn.stage.route.ReplicatorStage;
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
	protected boolean useNetClient;
	
	protected long debugI2CRateLastTime;

	protected HardwareConnection[] digitalInputs; //Button, Motion
	protected HardwareConnection[] digitalOutputs;//Relay Buzzer

	protected HardwareConnection[] analogInputs;  //Light, UV, Moisture
	protected HardwareConnection[] pwmOutputs;    //Servo   //(only 3, 5, 6, 9, 10, 11 when on edison)

	protected I2CConnection[] i2cInputs;
	protected I2CConnection[] i2cOutputs;

	private long timeTriggerRate;
	private long timeTriggerStart;
	
	
	private Blocker channelBlocker;

	public final GraphManager gm;

	private static final int DEFAULT_LENGTH = 16;
	private static final int DEFAULT_PAYLOAD_SIZE = 128;

	protected final PipeConfig<TrafficReleaseSchema> releasePipesConfig   = new PipeConfig<TrafficReleaseSchema>(TrafficReleaseSchema.instance, DEFAULT_LENGTH);
	protected final PipeConfig<TrafficOrderSchema> orderPipesConfig       = new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, DEFAULT_LENGTH);
	protected final PipeConfig<TrafficAckSchema> ackPipesConfig           = new PipeConfig<TrafficAckSchema>(TrafficAckSchema.instance, DEFAULT_LENGTH);
	protected final PipeConfig<GroveResponseSchema> groveResponseConfig   = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, DEFAULT_LENGTH, DEFAULT_PAYLOAD_SIZE);
	protected final PipeConfig<I2CResponseSchema> i2CResponseSchemaConfig = new PipeConfig<I2CResponseSchema>(I2CResponseSchema.instance, DEFAULT_LENGTH, DEFAULT_PAYLOAD_SIZE);

	public final I2CBacking i2cBacking;

	protected static final long MS_TO_NS = 1_000_000;


	private static final Logger logger = LoggerFactory.getLogger(HardwareImpl.class);

	public Enum<?> beginningState;


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
			return null;
		}
	}

	protected HardwareConnection[] growHardwareConnections(HardwareConnection[] original, HardwareConnection toAdd) {
		final int len = original.length;
		//Validate that what we are adding is safe
		int i = len;
		while (--i>=0) {
			if (original[i].register == toAdd.register) {
				throw new UnsupportedOperationException("This connection "+toAdd.register+" already has attachment "+original[i].twig+" so the attachment "+toAdd.twig+" can not be added.");
			}
		}

		//Grow the array
		HardwareConnection[] result = new HardwareConnection[len+1];
		System.arraycopy(original, 0, result, 0, len);
		result[len] = toAdd;
		return result;
	}

	protected I2CConnection[] growI2CConnections(I2CConnection[] original, I2CConnection toAdd){

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



	protected Hardware internalConnectAnalog(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if (t.isInput()) {
			assert(!t.isOutput());
			analogInputs = growHardwareConnections(analogInputs, new HardwareConnection(t,connection, customRate, customAverageMS, everyValue));
		} else {
			assert(t.isOutput());
			pwmOutputs = growHardwareConnections(pwmOutputs, new HardwareConnection(t,connection, customRate, customAverageMS, everyValue));
		}
		return this;
	}  

	protected Hardware internalConnectDigital(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if (t.isInput()) {
			assert(!t.isOutput());
			digitalInputs = growHardwareConnections(digitalInputs, new HardwareConnection(t,connection, customRate, customAverageMS, everyValue));
		} else {
			assert(t.isOutput());
			digitalOutputs = growHardwareConnections(digitalOutputs, new HardwareConnection(t,connection, customRate, customAverageMS, everyValue));
		}
		return this;
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
		timeTriggerStart = System.currentTimeMillis()+rateInMS;
		return this;
	}
	
	public Hardware setTriggerRate(TimeTrigger trigger) {	
		long period = trigger.getRate();
		timeTriggerRate = period;
		long now = System.currentTimeMillis();		
		long soFar = (now % period);		
		timeTriggerStart = (now - soFar) + period;				
		return this;
	}
	

	public Hardware useI2C() {
		this.configI2C = true; //TODO: enusre pi grove turns this on at all times, 
		                       //TODO: when this is NOT on do not build the i2c pipes.
		return this;
	}
	
	public Hardware useNetClient() {
		this.useNetClient = true;
		return this;
	}


	public long getTriggerRate() {
		return timeTriggerRate;
	}
	public long getTriggerStart() {
		return timeTriggerStart;
	}
	

	public abstract HardwarePlatformType getPlatformType();
	public abstract int read(Port port); //Platform specific
	public abstract void write(Port port, int value); //Platform specific
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
			result[pos++] = new HardwareConnection(analogInputs[j].twig,(int) EdisonConstants.ANALOG_CONNECTOR_TO_PIN[analogInputs[j].register]);
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
				if (mapAnalogs ? base[j].register ==  EdisonConstants.ANALOG_CONNECTOR_TO_PIN[items[i].register] :  base[j]==items[i]) {
					throw new UnsupportedOperationException("Connector "+items[i]+" is assigned more than once.");
				}
			}
		}     
	}

	public abstract CommandChannel newCommandChannel(PipeConfig<GroveRequestSchema> pinPipeConfig, PipeConfig<I2CCommandSchema> i2cPipeConfig, 
			 PipeConfig<MessagePubSub> pubSubConfig,
             PipeConfig<NetRequestSchema> netRequestConfig,
             PipeConfig<TrafficOrderSchema> orderPipeConfig);

	static final boolean debug = false;

	public void shutdown() {
		//can be overridden by specific hardware impl if shutdown is supported.
	}



	public final void buildStages(
			IntHashTable subscriptionPipeLookup,
			IntHashTable netPipeLookup,			
			Pipe<GroveResponseSchema>[] responsePipes,     //one for each listener of this type (broadcast to all)
			Pipe<I2CResponseSchema>[] i2cResponsePipes,    //one for each listener of this type (broadcast to all)
			Pipe<MessageSubscription>[] subscriptionPipes, //one for each listener of this type (subscription per pipe)
			Pipe<NetResponseSchema>[] netResponsePipes,

			Pipe<TrafficOrderSchema>[] orderPipes,    //one for each command channel 

			Pipe<GroveRequestSchema>[] requestPipes,  //one for each command channel 
			Pipe<I2CCommandSchema>[] i2cPipes,        //one for each command channel 
			Pipe<MessagePubSub>[] messagePubSub,      //one for each command channel 
			Pipe<NetRequestSchema>[] netRequestPipes  //one for each command channel
			) {

		assert(orderPipes.length == i2cPipes.length);
		assert(orderPipes.length == requestPipes.length);

		int commandChannelCount = orderPipes.length;
		
		
		int eventSchemas = 0;
		
		//TODO: based on the pipes use each of these
		int TYPE_PIN = eventSchemas++;
		int TYPE_I2C = eventSchemas++;
		int TYPE_MSG = eventSchemas++;
		int TYPE_NET = useNetClient(netPipeLookup, netResponsePipes, netRequestPipes) ? eventSchemas++ : -1;
						

		Pipe<TrafficReleaseSchema>[][] masterGoOut = new Pipe[eventSchemas][commandChannelCount];
		Pipe<TrafficAckSchema>[][]     masterAckIn = new Pipe[eventSchemas][commandChannelCount];

		long timeout = 20_000; //20 seconds

		int maxGoPipeId = 0;
		int t = commandChannelCount;
		while (--t>=0) {

			int p = eventSchemas;//major command requests that can come from commandChannels
			Pipe<TrafficReleaseSchema>[] goOut = new Pipe[p];
			Pipe<TrafficAckSchema>[] ackIn = new Pipe[p];
			while (--p>=0) {
				masterGoOut[p][t] = goOut[p] = new Pipe<TrafficReleaseSchema>(releasePipesConfig);
				maxGoPipeId = Math.max(maxGoPipeId, goOut[p].id);
				
				masterAckIn[p][t] = ackIn[p]=new Pipe<TrafficAckSchema>(ackPipesConfig);								
			}
			
			TrafficCopStage trafficCopStage = new TrafficCopStage(gm, timeout, orderPipes[t], ackIn, goOut);

		}
		
		
		
		////////
		//create the network client stages
		////////
		if (useNetClient(netPipeLookup, netResponsePipes, netRequestPipes)) {
			
			System.err.println("loaded client http");
			if (masterGoOut[TYPE_NET].length != masterAckIn[TYPE_NET].length) {
				throw new UnsupportedOperationException(masterGoOut[TYPE_NET].length+"!="+masterAckIn[TYPE_NET].length);
			}
			if (masterGoOut[TYPE_NET].length != netRequestPipes.length) {
				throw new UnsupportedOperationException(masterGoOut[TYPE_NET].length+"!="+netRequestPipes.length);
			}
			
			assert(masterGoOut[TYPE_NET].length == masterAckIn[TYPE_NET].length);
			assert(masterGoOut[TYPE_NET].length == netRequestPipes.length);
			
			
			PipeConfig<NetRequestSchema> netRequestConfig = new PipeConfig<NetRequestSchema>(NetRequestSchema.instance, 30,1<<9);		
			PipeConfig<NetPayloadSchema> clientNetRequestConfig = new PipeConfig<NetPayloadSchema>(NetPayloadSchema.instance,4,16000); 		

			//BUILD GRAPH
			
			int connectionsInBits=10;			
			int maxPartialResponses=4;
			ClientCoordinator ccm = new ClientCoordinator(connectionsInBits, maxPartialResponses);

			//TODO: tie this in tonight.
			int inputsCount = 1;
			int outputsCount = 1;
			Pipe<NetPayloadSchema>[] clientRequests = new Pipe[outputsCount];
			int r = outputsCount;
			while (--r>=0) {
				clientRequests[r] = new Pipe<NetPayloadSchema>(clientNetRequestConfig);		
			}
			HTTPClientRequestStage requestStage = new HTTPClientRequestStage(gm, this, ccm, netRequestPipes, masterGoOut[TYPE_NET], masterAckIn[TYPE_NET], clientRequests);
			
			
			NetGraphBuilder.buildHTTPClientGraph(true, gm, maxPartialResponses, ccm, netPipeLookup, 10, 1<<15,
												 clientRequests, netResponsePipes, 2, 2, 2); 
						
		}// else {
			//System.err.println("skipped  "+IntHashTable.isEmpty(netPipeLookup)+"  "+netResponsePipes.length+"   "+netRequestPipes.length  );
		//}
		
		/////////
		//always create the pub sub and state management stage?
		/////////
		//TODO: only create when subscriptionPipeLookup is not empty and subscriptionPipes has zero length.
		if (IntHashTable.isEmpty(subscriptionPipeLookup) && subscriptionPipes.length==0) {
			logger.trace("can save some resources by not starting up the unused pub sub service.");
		}
		createMessagePubSubStage(subscriptionPipeLookup, messagePubSub, masterGoOut[TYPE_MSG], masterAckIn[TYPE_MSG], 
				                 subscriptionPipes);

		//////////////////
		//only build and connect I2C if it is used for either in or out  
		//////////////////
		Pipe<I2CResponseSchema> masterI2CResponsePipe = null;
		if (i2cResponsePipes.length>0) {
			masterI2CResponsePipe = new Pipe<I2CResponseSchema>(i2CResponseSchemaConfig);
			ReplicatorStage i2cResponseSplitter = new ReplicatorStage<I2CResponseSchema>(gm, masterI2CResponsePipe, i2cResponsePipes);   
		}
		if (i2cPipes.length>0 || (null!=masterI2CResponsePipe)) {
			createI2COutputInputStage(i2cPipes, masterGoOut[TYPE_I2C], masterAckIn[TYPE_I2C], masterI2CResponsePipe);
		}

		//////////////
		//only build and connect gpio input responses if it is used
		//////////////
		if (responsePipes.length>0) {
			Pipe<GroveResponseSchema> masterResponsePipe = new Pipe<GroveResponseSchema>(groveResponseConfig);
			ReplicatorStage responseSplitter = new ReplicatorStage<GroveResponseSchema>(gm, masterResponsePipe, responsePipes);      
			createADInputStage(masterResponsePipe);
		}
		
		///////////////
		//must always create output stage   TODO: if there are no outputs attached do not schedule this stage, could trim earlier
		///////////////
		createADOutputStage(requestPipes, masterGoOut[TYPE_PIN], masterAckIn[TYPE_PIN]);

		channelBlocker = new Blocker(maxGoPipeId+1);
		   
	       
	}

	private boolean useNetClient(IntHashTable netPipeLookup, Pipe<NetResponseSchema>[] netResponsePipes, Pipe<NetRequestSchema>[] netRequestPipes) {
		
		if (isUseNetClient() && IntHashTable.isEmpty(netPipeLookup)) {
			throw new UnsupportedOperationException("useNetClient is enabled however no HTTPResponseListener instances were registered.");
		}
		
		return !IntHashTable.isEmpty(netPipeLookup) && (netResponsePipes.length!=0) && (netRequestPipes.length!=0);
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
		final StageScheduler scheduler = //disabled until recursion is fixed...  new FixedThreadsScheduler(gm, Runtime.getRuntime().availableProcessors()); 
				                         new ThreadPerStageScheduler(gm);

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
		return listener instanceof PubSubListener || listener instanceof StateChangeListener<?>;
	}

	public boolean isListeningToHTTPResponse(Object listener) {
		boolean result = listener instanceof HTTPResponseListener;
		if (result) {
			if (isUseNetClient()) {
				throw new UnsupportedOperationException("In declareConnections call useNetClient() on the hardware object to enable use of this listener.");
			}
		}
		return result;
	}

	
	/**
	 * access to system time.  This method is required so it can be monitored and simulated by unit tests.
	 */
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	public void blockChannelUntil(int channelId, long timeInMillis) {        
		channelBlocker.until(channelId, timeInMillis);
	}

	public boolean isChannelBlocked(int channelId) {
		if (null != channelBlocker)  {
			return channelBlocker.isBlocked(channelId);
		} else {
			return false;
		}
	}

	public long releaseChannelBlocks(long now) {
		if (null != channelBlocker) {
			channelBlocker.releaseBlocks(now);
			return channelBlocker.durationToNextRelease(now, -1);
		} else {
			return -1; //was not init so there are no possible blocked channels.
		}
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

	public Pipe<MessagePubSub> consumeStartupSubscriptions() {
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
		
		long[] schedulePeriods = new long[localInputs.length];
		for (int i = 0; i < localInputs.length; i++) {
			schedulePeriods[i] = localInputs[i].responseMS*MS_TO_NS;
		}
		//logger.debug("known I2C rates: {}",Arrays.toString(schedulePeriods));
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

	public byte convertToPort(byte connection) {
		return connection;
	}
			

	public Hardware connect(IODevice t, Port port, int customRateMS, int customAvgWindowMS, boolean everyValue) {
		
		if (0 != (port.mask&Port.IS_ANALOG)) {
			return internalConnectAnalog(t, port.port, customRateMS, customAvgWindowMS, everyValue);			
		}
		
		if (0 != (port.mask&Port.IS_DIGITAL)) {
			return internalConnectDigital(t, port.port, customRateMS, customAvgWindowMS, everyValue);			
		}
		
		return this;
	}

	public Hardware connect(IODevice t, Port port, int customRateMS, int customAvgWindowMS) {
		
		if (0 != (port.mask&Port.IS_ANALOG)) {
			return internalConnectAnalog(t, port.port, customRateMS, customAvgWindowMS, false);			
		}
		
		if (0 != (port.mask&Port.IS_DIGITAL)) {
			return internalConnectDigital(t, port.port, customRateMS, customAvgWindowMS, false);			
		}
		
		return this;
	}

	public Hardware connect(IODevice t, Port port, int customRateMS) {
		
		if (port.isAnalog()) {
			return internalConnectAnalog(t, port.port, customRateMS, -1, false);			
		} else {
			return internalConnectDigital(t, port.port, customRateMS, -1, false);			
		}
		
	}
	
	public Hardware connect(IODevice t, Port port, int customRateMS, boolean everyValue) {
		
		if (port.isAnalog()) {
			return internalConnectAnalog(t, port.port, customRateMS, -1, everyValue);			
		} else {
			return internalConnectDigital(t, port.port, customRateMS, -1, everyValue);			
		}
		
	}
	
	public Hardware connect(IODevice t, Port port) {
		
		if (0 != (port.mask&Port.IS_ANALOG)) {
			return internalConnectAnalog(t, port.port, -1, -1, false);			
		}
		
		if (0 != (port.mask&Port.IS_DIGITAL)) {
			return internalConnectDigital(t, port.port, -1, -1, false);			
		}
		
		return this;
	}

	public boolean isUseNetClient() {
		return useNetClient;
	}

}