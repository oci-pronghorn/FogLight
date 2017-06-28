package com.ociweb.iot.hardware;

import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.impl.BuilderImpl;
import com.ociweb.gl.impl.schema.IngressMessages;
import com.ociweb.gl.impl.schema.MessagePubSub;
import com.ociweb.gl.impl.schema.MessageSubscription;
import com.ociweb.gl.impl.schema.TrafficAckSchema;
import com.ociweb.gl.impl.schema.TrafficOrderSchema;
import com.ociweb.gl.impl.schema.TrafficReleaseSchema;
import com.ociweb.gl.impl.stage.MessagePubSubStage;
import com.ociweb.gl.impl.stage.TrafficCopStage;
import com.ociweb.iot.hardware.impl.DirectHardwareAnalogDigitalOutputStage;
import com.ociweb.iot.hardware.impl.SerialDataSchema;
import com.ociweb.iot.hardware.impl.SerialDataWriterStage;
import com.ociweb.iot.hardware.impl.SerialInputSchema;
import com.ociweb.iot.hardware.impl.SerialOutputSchema;
import com.ociweb.iot.hardware.impl.SerialDataReaderStage;
import com.ociweb.iot.hardware.impl.edison.EdisonConstants;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.Baud;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.iot.maker.SerialListener;
import com.ociweb.pronghorn.iot.HTTPClientRequestStage;
import com.ociweb.pronghorn.iot.ReadDeviceInputStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.i2c.I2CJFFIStage;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.iot.rs232.RS232Client;
import com.ociweb.pronghorn.iot.rs232.RS232Clientable;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.network.ClientCoordinator;
import com.ociweb.pronghorn.network.NetGraphBuilder;
import com.ociweb.pronghorn.network.schema.ClientHTTPRequestSchema;
import com.ociweb.pronghorn.network.schema.NetPayloadSchema;
import com.ociweb.pronghorn.network.schema.NetResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.util.hash.IntHashTable;
import com.ociweb.pronghorn.stage.route.ReplicatorStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.math.PMath;
import com.ociweb.pronghorn.util.math.ScriptedSchedule;

public abstract class HardwareImpl extends BuilderImpl implements Hardware {


	private static final int MAX_MOVING_AVERAGE_SUPPORTED = 101; //TOOD: is this still needed, remove???

	private static final HardwareConnection[] EMPTY = new HardwareConnection[0];

	protected boolean configI2C;       //Humidity, LCD need I2C address so..
	protected int i2cBus;
	protected long debugI2CRateLastTime;

	protected HardwareConnection[] digitalInputs; //Button, Motion
	protected HardwareConnection[] digitalOutputs;//Relay Buzzer

	protected HardwareConnection[] analogInputs;  //Light, UV, Moisture
	protected HardwareConnection[] pwmOutputs;    //Servo   //(only 3, 5, 6, 9, 10, 11 when on edison)

	protected I2CConnection[] i2cInputs;
	protected I2CConnection[] i2cOutputs;
	

	private static final int DEFAULT_LENGTH = 16;
	private static final int DEFAULT_PAYLOAD_SIZE = 128;


	public final I2CBacking i2cBacking;

	protected static final long MS_TO_NS = 1_000_000;


	private static final Logger logger = LoggerFactory.getLogger(HardwareImpl.class);

	protected final IODevice[] deviceOnPort= new IODevice[Port.values().length];
	
	/////////////////
	///Pipes for initial startup declared subscriptions. (Not part of graph)
	private final int maxStartupSubs = 64;
	private final int maxTopicLengh  = 128;
	private Pipe<MessagePubSub> tempPipeOfStartupSubscriptions;
	/////////////////
	/////////////////

	protected ReentrantLock devicePinConfigurationLock = new ReentrantLock();

	protected RS232Client rs232Client;
	protected String rs232ClientDevice = "/dev/ttyMFD1";//custom hardware should override this edison value
	protected Baud   rs232ClientBaud = Baud.B_____9600;
	protected String bluetoothDevice = null;
	

	private static final boolean debug = false;

    private int IDX_PIN = -1;
    private int IDX_I2C = -1;
    private int IDX_MSG = -1;
    private int IDX_NET = -1;
    private int IDX_SER = -1;
	
    public IODevice getConnectedDevice(Port p) {    	
    	return deviceOnPort[p.ordinal()];
    }

	public HardwareImpl(GraphManager gm, I2CBacking i2cBacking) {
		this(gm, i2cBacking, false,false,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY);
	}

	protected HardwareImpl(GraphManager gm, I2CBacking i2cBacking, boolean publishTime, boolean configI2C, HardwareConnection[] multiDigitalInput,
			HardwareConnection[] digitalInputs, HardwareConnection[] digitalOutputs, HardwareConnection[] pwmOutputs, HardwareConnection[] analogInputs) {

		super(gm);
		
		this.i2cBacking = i2cBacking;

		this.configI2C = configI2C; //may be removed.
		this.i2cBus = -1; // TODO: Should this be initialized in some more complexicated way?

		this.digitalInputs = digitalInputs;
		this.digitalOutputs = digitalOutputs;
		this.pwmOutputs = pwmOutputs;
		this.analogInputs = analogInputs;

		this.getTempPipeOfStartupSubscriptions().initBuffers();
	}

	public static I2CBacking getI2CBacking(byte deviceNum) {
		try {
			return new I2CNativeLinuxBacking().configure(deviceNum);
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

	public Hardware useSerial(Baud baud) {
		this.rs232ClientBaud = baud;
		return this;
	}

	public Hardware useI2C() {
		this.configI2C = true; //TODO: ensure pi grove turns this on at all times,
		                       //TODO: when this is NOT on do not build the i2c pipes.
		return this;
	}
	
	@Deprecated //would be nice if we did not have to do this.
	public Hardware useI2C(int bus) {
		this.configI2C = true; //TODO: ensure pi grove turns this on at all times,
		                       //TODO: when this is NOT on do not build the i2c pipes.
		this.i2cBus = bus;
		return this;
	}
	
	public boolean isUseI2C() {
		return this.configI2C;
	}

	public abstract HardwarePlatformType getPlatformType();
	public abstract int read(Port port); //Platform specific
	public abstract void write(Port port, int value); //Platform specific

	public int maxAnalogMovingAverage() {
		return MAX_MOVING_AVERAGE_SUPPORTED;
	}


	public void coldSetup(){
		//TODO: I2C Setup methods
	}

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


	public void shutdown() {
		super.shutdown();
		//can be overridden by specific hardware impl if shutdown is supported.
	}

	

	private boolean useNetClient(IntHashTable netPipeLookup, Pipe<NetResponseSchema>[] netResponsePipes, Pipe<ClientHTTPRequestSchema>[] netRequestPipes) {
		
		if (isUseNetClient() && IntHashTable.isEmpty(netPipeLookup)) {
			throw new UnsupportedOperationException("useNetClient is enabled however no HTTPResponseListener instances were registered.");
		}
		
		return !IntHashTable.isEmpty(netPipeLookup) && (netResponsePipes.length!=0) && (netRequestPipes.length!=0);
	}


	private void createUARTInputStage(Pipe<SerialInputSchema> masterUARTPipe) {
		RS232Clientable client = buildSerialClient();
		new SerialDataReaderStage(this.gm, masterUARTPipe, client);
	}


	
	protected RS232Clientable buildSerialClient() {
		if (null==rs232Client) {
			//custom hardware can override this
			rs232Client = new RS232Client(rs232ClientDevice, rs232ClientBaud);
		}
		return rs232Client;
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

	
	public boolean isListeningToSerial(Object listener) {
		return listener instanceof SerialListener;
	}
	
	public boolean isListeningToI2C(Object listener) {
		return listener instanceof I2CListener;
	}

	public boolean isListeningToPins(Object listener) {
		return listener instanceof DigitalListener || listener instanceof AnalogListener || listener instanceof RotaryListener;
	}



	private Pipe<MessagePubSub> getTempPipeOfStartupSubscriptions() {
		if (null==tempPipeOfStartupSubscriptions) {

			final PipeConfig<MessagePubSub> messagePubSubConfig = new PipeConfig<MessagePubSub>(MessagePubSub.instance, maxStartupSubs,maxTopicLengh);   
			tempPipeOfStartupSubscriptions = new Pipe<MessagePubSub>(messagePubSubConfig);

		}		

		return tempPipeOfStartupSubscriptions;
	}


	public boolean hasSerialInputs() {
		if (true ) {
			throw new UnsupportedOperationException("not yet implemented");
		}
		return true; 
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
	
	public boolean hasDigitalOrAnalogOutputs() {
		return (pwmOutputs.length+digitalOutputs.length)>0;
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
		
		deviceOnPort[port.ordinal()] = t;
		
		if (0 != (port.mask&Port.IS_ANALOG)) {
			return internalConnectAnalog(t, port.port, customRateMS, customAvgWindowMS, everyValue);			
		}
		
		if (0 != (port.mask&Port.IS_DIGITAL)) {
			return internalConnectDigital(t, port.port, customRateMS, customAvgWindowMS, everyValue);			
		}
		
		return this;
	}

	public Hardware connect(IODevice t, Port port, int customRateMS, int customAvgWindowMS) {
		
		deviceOnPort[port.ordinal()] = t;
		
		if (0 != (port.mask&Port.IS_ANALOG)) {
			return internalConnectAnalog(t, port.port, customRateMS, customAvgWindowMS, false);			
		}
		
		if (0 != (port.mask&Port.IS_DIGITAL)) {
			return internalConnectDigital(t, port.port, customRateMS, customAvgWindowMS, false);			
		}
		
		return this;
	}

	public Hardware connect(IODevice t, Port port, int customRateMS) {
		
		deviceOnPort[port.ordinal()] = t;
				
		if (port.isAnalog()) {
			return internalConnectAnalog(t, port.port, customRateMS, -1, false);			
		} else {
			return internalConnectDigital(t, port.port, customRateMS, -1, false);			
		}
		
	}
	
	public Hardware connect(IODevice t, Port port, int customRateMS, boolean everyValue) {
		
		deviceOnPort[port.ordinal()] = t;
		
		if (port.isAnalog()) {
			return internalConnectAnalog(t, port.port, customRateMS, -1, everyValue);			
		} else {
			return internalConnectDigital(t, port.port, customRateMS, -1, everyValue);			
		}
		
	}
	
	public Hardware connect(IODevice t, Port port) {
		
		deviceOnPort[port.ordinal()] = t;
		
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

	public void releasePinOutTraffic(int count, GreenCommandChannel<?> gcc) {		
		GreenCommandChannel.publishGo(count, IDX_PIN, gcc);		
	}

	public void releaseI2CTraffic(int count, GreenCommandChannel<?> gcc) {
		GreenCommandChannel.publishGo(count, IDX_I2C, gcc);
	}
	
	@Override
	public void releasePubSubTraffic(int count, GreenCommandChannel<?> gcc) {
		GreenCommandChannel.publishGo(count, IDX_MSG, gcc);
	}

	public void buildStages(IntHashTable subscriptionPipeLookup2, IntHashTable netPipeLookup2, GraphManager gm2) {
		
		Pipe<GroveResponseSchema>[] responsePipes = GraphManager.allPipesOfType(gm2, GroveResponseSchema.instance);
		Pipe<I2CResponseSchema>[] i2cResponsePipes = GraphManager.allPipesOfType(gm2, I2CResponseSchema.instance);
		Pipe<NetResponseSchema>[] netResponsePipes = GraphManager.allPipesOfType(gm2, NetResponseSchema.instance);
		Pipe<TrafficOrderSchema>[] orderPipes = GraphManager.allPipesOfType(gm2, TrafficOrderSchema.instance);
		Pipe<GroveRequestSchema>[] requestPipes = GraphManager.allPipesOfType(gm2, GroveRequestSchema.instance);
		Pipe<I2CCommandSchema>[] i2cPipes = GraphManager.allPipesOfType(gm2, I2CCommandSchema.instance);
		Pipe<ClientHTTPRequestSchema>[] netRequestPipes = GraphManager.allPipesOfType(gm2, ClientHTTPRequestSchema.instance);			
		Pipe<SerialOutputSchema>[] serialOutputPipes = GraphManager.allPipesOfType(gm2, SerialOutputSchema.instance);		
		Pipe<SerialInputSchema>[] serialInputPipes = GraphManager.allPipesOfType(gm2, SerialInputSchema.instance);
		
		Pipe<MessageSubscription>[] subscriptionPipes = GraphManager.allPipesOfType(gm2, MessageSubscription.instance);
		Pipe<MessagePubSub>[] messagePubSub = GraphManager.allPipesOfType(gm2, MessagePubSub.instance);
		Pipe<IngressMessages>[] ingressMessagePipes = GraphManager.allPipesOfType(gm2, IngressMessages.instance);
		
		int commandChannelCount = orderPipes.length;

		int eventSchemas = 0;
		
		IDX_PIN = requestPipes.length>0 ? eventSchemas++ : -1;
		IDX_I2C = eventSchemas++;
		IDX_MSG = (IntHashTable.isEmpty(subscriptionPipeLookup2) && subscriptionPipes.length==0 && messagePubSub.length==0) ? -1 : eventSchemas++;
		IDX_NET = useNetClient(netPipeLookup2, netResponsePipes, netRequestPipes) ? eventSchemas++ : -1;
		IDX_SER = serialOutputPipes.length>0 ? eventSchemas++ : -1;
						
		
		Pipe<TrafficReleaseSchema>[][] masterGoOut = new Pipe[eventSchemas][commandChannelCount];
		Pipe<TrafficAckSchema>[][]     masterAckIn = new Pipe[eventSchemas][commandChannelCount];
		
		long timeout = 20_000; //20 seconds
		
		//TODO: can we share this while with the parent BuilderImpl, I think so..
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
		initChannelBlocker(maxGoPipeId);
		
		
		////////
		//create the network client stages
		////////
		if (useNetClient(netPipeLookup2, netResponsePipes, netRequestPipes)) {
			
			System.err.println("loaded client http");
			if (masterGoOut[IDX_NET].length != masterAckIn[IDX_NET].length) {
				throw new UnsupportedOperationException(masterGoOut[IDX_NET].length+"!="+masterAckIn[IDX_NET].length);
			}
			if (masterGoOut[IDX_NET].length != netRequestPipes.length) {
				throw new UnsupportedOperationException(masterGoOut[IDX_NET].length+"!="+netRequestPipes.length);
			}
			
			assert(masterGoOut[IDX_NET].length == masterAckIn[IDX_NET].length);
			assert(masterGoOut[IDX_NET].length == netRequestPipes.length);
			
			
			PipeConfig<ClientHTTPRequestSchema> netRequestConfig = new PipeConfig<ClientHTTPRequestSchema>(ClientHTTPRequestSchema.instance, 30,1<<9);		
			PipeConfig<NetPayloadSchema> clientNetRequestConfig = new PipeConfig<NetPayloadSchema>(NetPayloadSchema.instance,4,16000); 		
		
			//BUILD GRAPH
			
			int connectionsInBits=10;			
			int maxPartialResponses=4;
			ClientCoordinator ccm = new ClientCoordinator(connectionsInBits, maxPartialResponses, true);
		
			int outputsCount = 1;
			Pipe<NetPayloadSchema>[] clientRequests = new Pipe[outputsCount];
			int r = outputsCount;
			while (--r>=0) {
				clientRequests[r] = new Pipe<NetPayloadSchema>(clientNetRequestConfig);		
			}
			
			HTTPClientRequestStage requestStage = new HTTPClientRequestStage(gm, this, ccm, netRequestPipes, masterGoOut[IDX_NET], masterAckIn[IDX_NET], clientRequests);
						
			NetGraphBuilder.buildHTTPClientGraph(gm, maxPartialResponses, ccm, netPipeLookup2, 10, 1<<15, clientRequests, netResponsePipes); 
			
						
		}
		
		if (IDX_MSG <0) {
				logger.info("saved some resources by not starting up the unused pub sub service.");
		} else {
			 	createMessagePubSubStage(subscriptionPipeLookup2, ingressMessagePipes, messagePubSub, masterGoOut[IDX_MSG], masterAckIn[IDX_MSG], subscriptionPipes);
		}
				
		//////////////////
		//only build and connect I2C if it is used for either in or out  
		//////////////////
		Pipe<I2CResponseSchema> masterI2CResponsePipe = null;
		if (i2cResponsePipes.length>0) {
			masterI2CResponsePipe =  I2CResponseSchema.instance.newPipe(DEFAULT_LENGTH, DEFAULT_PAYLOAD_SIZE);
			new ReplicatorStage<I2CResponseSchema>(gm, masterI2CResponsePipe, i2cResponsePipes);   
		}
		if (i2cPipes.length>0 || (null!=masterI2CResponsePipe)) {
			createI2COutputInputStage(i2cPipes, masterGoOut[IDX_I2C], masterAckIn[IDX_I2C], masterI2CResponsePipe);
		}
		
		//////////////
		//only build and connect gpio input responses if it is used
		//////////////
		if (responsePipes.length>0) {
			Pipe<GroveResponseSchema> masterResponsePipe = GroveResponseSchema.instance.newPipe(DEFAULT_LENGTH, DEFAULT_PAYLOAD_SIZE);
			new ReplicatorStage<GroveResponseSchema>(gm, masterResponsePipe, responsePipes);      
			createADInputStage(masterResponsePipe);
		}
		
		//////////////
		//only build serial input if the data is consumed
		//////////////
		if (serialInputPipes.length>0) {
			Pipe<SerialInputSchema> masterUARTPipe = SerialDataSchema.instance.newPipe(DEFAULT_LENGTH, DEFAULT_PAYLOAD_SIZE);
			new ReplicatorStage<SerialInputSchema>(gm, masterUARTPipe, serialInputPipes);   
			createUARTInputStage(masterUARTPipe);
		}		
		
		/////////////
		//only build serial output if data is sent
		/////////////
		if (serialOutputPipes.length>0) {			
			createSerialOutputStage(serialOutputPipes, masterGoOut, masterAckIn);			
		}
				
		///////////////
		//only build direct pin output when we detected its use
		///////////////
		if (IDX_PIN>=0) {
			createADOutputStage(requestPipes, masterGoOut[IDX_PIN], masterAckIn[IDX_PIN]);
		}
	}

	protected void createSerialOutputStage(Pipe<SerialOutputSchema>[] serialOutputPipes,
			Pipe<TrafficReleaseSchema>[][] masterGoOut, Pipe<TrafficAckSchema>[][] masterAckIn) {
		new SerialDataWriterStage(gm, serialOutputPipes, masterGoOut[IDX_SER], masterAckIn[IDX_SER],
				                     this, this.buildSerialClient());
	}

	public static int serialIndex(HardwareImpl hardware) {
		return hardware.IDX_SER;
	}
	
	public static int i2cIndex(HardwareImpl hardware) {
		return hardware.IDX_I2C;
	}
	
	@Override
	public int pubSubIndex() {
		return IDX_MSG;
	}
	
	@Override
	public int netIndex() {
		return IDX_NET;
	}

	
	
}