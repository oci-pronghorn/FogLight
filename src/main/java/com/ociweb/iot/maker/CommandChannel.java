package com.ociweb.iot.maker;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.impl.schema.MessagePubSub;
import com.ociweb.gl.impl.schema.TrafficOrderSchema;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.network.schema.ClientHTTPRequestSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

/**
 * Represents a dedicated channel for communicating with a single device
 * or resource on an IoT system.
 */
public abstract class CommandChannel {

    private final Pipe<?>[] outputPipes;
    protected final Pipe<TrafficOrderSchema> goPipe;
    
    protected final Pipe<I2CCommandSchema> i2cOutput;  //TODO: find a way to not create if not used like http or message pup/sub
    protected final Pipe<GroveRequestSchema> pinOutput; //TODO: find a way to not create if not used like http or message pup/sub
    
    private Pipe<ClientHTTPRequestSchema> httpRequest;
    private Pipe<MessagePubSub> messagePubSub;
    
    protected AtomicBoolean aBool = new AtomicBoolean(false);   

    public static final int ANALOG_BIT = 0x40; //added to connection to track if this is the analog .0vs digital
    protected static final long MS_TO_NS = 1_000_000;
    
    protected DataOutputBlobWriter<I2CCommandSchema> i2cWriter;  
    protected int runningI2CCommandCount;
    
    private Object listener;

    //TODO: need to set this as a constant driven from the known i2c devices and the final methods, what is the biggest command sequence?
    protected final int maxCommands = 50;
    
    private final int maxOpenTopics = 1;
    
    protected final int pinPipeIdx = 0; 
    protected final int i2cPipeIdx = 1;
    protected final int subPipeIdx = 2;
    protected final int netPipeIdx = 3;
    protected final int goPipeIdx = 4;
    
    
    private HardwareImpl hardware;
	private final int MAX_COMMAND_FRAGMENTS_SIZE;
        
    protected CommandChannel(GraphManager gm, HardwareImpl hardware,
    						 PipeConfig<GroveRequestSchema> pinConfig, 
    					   	 PipeConfig<I2CCommandSchema> i2cConfig,  
                             PipeConfig<MessagePubSub> pubSubConfig,
                             PipeConfig<ClientHTTPRequestSchema> netRequestConfig,
                             PipeConfig<TrafficOrderSchema> goPipeConfig) {
    	
       this.outputPipes = new Pipe<?>[]{new Pipe<GroveRequestSchema>(pinConfig),
    	                                new Pipe<I2CCommandSchema>(i2cConfig),
    	                                newPubSubPipe(pubSubConfig),
    	                                hardware.isUseNetClient() ? newNetRequestPipe(netRequestConfig) : null,
    	                                new Pipe<TrafficOrderSchema>(goPipeConfig)};
       
       //using index helps avoid bugs by ensuring we use and known these index values.
       this.goPipe = (Pipe<TrafficOrderSchema>) outputPipes[goPipeIdx];
       this.i2cOutput = (Pipe<I2CCommandSchema>) outputPipes[i2cPipeIdx];
       this.messagePubSub = (Pipe<MessagePubSub>) outputPipes[subPipeIdx];
       this.httpRequest = (Pipe<ClientHTTPRequestSchema>) outputPipes[netPipeIdx];
       this.pinOutput = (Pipe<GroveRequestSchema>) outputPipes[pinPipeIdx];
                             
       if (Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands >= this.i2cOutput.sizeOfSlabRing) {
           throw new UnsupportedOperationException("maxCommands too large or pipe is too small, pipe size must be at least "+(Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands));
       }
       this.hardware = hardware;
       
       MAX_COMMAND_FRAGMENTS_SIZE = Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands;
    }
    
    private static Pipe<MessagePubSub> newPubSubPipe(PipeConfig<MessagePubSub> config) {
    	return new Pipe<MessagePubSub>(config) {
			@SuppressWarnings("unchecked")
			@Override
			protected DataOutputBlobWriter<MessagePubSub> createNewBlobWriter() {
				return new PayloadWriter(this);
			}
    		
    	};
    }
    
    private static Pipe<ClientHTTPRequestSchema> newNetRequestPipe(PipeConfig<ClientHTTPRequestSchema> config) {
    	return new Pipe<ClientHTTPRequestSchema>(config) {
			@SuppressWarnings("unchecked")
			@Override
			protected DataOutputBlobWriter<ClientHTTPRequestSchema> createNewBlobWriter() {
				return new PayloadWriter(this);
			}
    		
    	};   	
    	
    }

    Pipe<?>[] getOutputPipes() {
    	return outputPipes;
    }
    
    
    void setListener(Object listener) {
        if (null != this.listener) {
            throw new UnsupportedOperationException("Bad Configuration, A CommandChannel can only be held and used by a single listener lambda/class");
        }
        this.listener = listener;
        
        
        
        
        
    }

    
    protected void publishGo(int count, int pipeIdx) {
        if(PipeWriter.tryWriteFragment(goPipe, TrafficOrderSchema.MSG_GO_10)) {                 
            PipeWriter.writeInt(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_PIPEIDX_11, pipeIdx);
            PipeWriter.writeInt(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_COUNT_12, count);
            PipeWriter.publishWrites(goPipe);
        } else {
            throw new UnsupportedOperationException("Was already check and should not have run out of space.");
        }
    }

    protected boolean enterBlockOk() {
        return aBool.compareAndSet(false, true);
    }
    
    protected boolean exitBlockOk() {
        return aBool.compareAndSet(true, false);
    }

    /**
     * Causes this channel to delay processing any actions until the specified
     * amount of time has elapsed.
     *
     * @param msDuration Milliseconds to delay.
     *
     * @return True if blocking was successful, and false otherwise.
     */
    public abstract boolean block(long msDuration);

    /**
     * Causes this channel to delay processing any actions on a given {@link Port}
     * until the specified amount of time has elapsed.
     *
     * @param port Port to temporarily stop processing actions on.
     * @param durationMilli Milliseconds until the port will process actions again.
     *
     * @return True if blocking was successful, and false otherwise.
     */
    public abstract boolean block(Port port, long durationMilli);

    /**
     * Causes this channel to delay processing any actions on a given {@link Port}
     * until the specified UNIX time is reached.
     *
     * @param port Port to temporarily stop processing actions on.
     * @param time Time, in milliseconds, since the UNIX epoch that indicates
     *             when actions should resume processing.
     *
     * @return True if blocking was successful, and false otherwise.
     */
    public abstract boolean blockUntil(Port port, long time);

    /**
     * Sets the value of an analog/digital port on this command channel.
     *
     * @param port {@link Port} to set the value of.
     * @param value Value to set the port to.
     *
     * @return True if the port could be set, and false otherwise.
     */
    public abstract boolean setValue(Port port, int value);

    /**
     * Sets the value of an analog/digital port on this command channel and then
     * delays processing of all future actions on this port until a specified
     * amount of time passes.
     *
     * @param port {@link Port} to set the value of.
     * @param value Value to set the port to.
     * @param durationMilli Time in milliseconds to delay processing of future actions
     *                      on this port.
     *
     * @return True if the port could be set, and false otherwise.
     */
    public abstract boolean setValueAndBlock(Port port, int value, long durationMilli);

    /**
     * "Pulses" a given port, setting its state to True/On and them immediately
     * setting its state to False/Off.
     *
     * @param port {@link Port} to pulse.
     *
     * @return True if the port could be pulsed, and false otherwise.
     */
    public abstract boolean digitalPulse(Port port);

    /**
     * "Pulses" a given port, setting its state to True/On and them immediately
     * setting its state to False/Off.
     *
     * @param port {@link Port} to pulse.
     * @param durationNanos Time in nanoseconds to sustain the pulse for.
     *
     * @return True if the port could be pulsed, and false otherwise.
     */
    public abstract boolean digitalPulse(Port port, long durationNanos);

    /**
     * Opens an I2C connection.
     *
     * @param targetAddress I2C address to open a connection to.
     *
     * @return An {@link DataOutputBlobWriter} with an {@link I2CCommandSchema} that's
     *         connected to the specified target address.
     *
     */
    public DataOutputBlobWriter<I2CCommandSchema> i2cCommandOpen(int targetAddress) {       
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {

            if (PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, targetAddress);

                if (null==i2cWriter) {
                    //TODO: add init method that we we will call from stage to do this.
                    i2cWriter = new DataOutputBlobWriter<I2CCommandSchema>(i2cOutput);//hack for now until we can get this into the scheduler TODO: nathan follow up.
                }
                 
                DataOutputBlobWriter.openField(i2cWriter);
                return i2cWriter;
            } else {
                throw new UnsupportedOperationException("Pipe is too small for large volume of i2c data");
            }
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
        
    }

    /**
     * Triggers a delay for a given I2C address.
     *
     * @param targetAddress I2C address to trigger a delay on.
     * @param durationNanos Time in nanoseconds to delay.
     */
    public void i2cDelay(int targetAddress, long durationNanos) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            if (++runningI2CCommandCount > maxCommands) {
                throw new UnsupportedOperationException("too many commands, found "+runningI2CCommandCount+" but only left room for "+maxCommands);
            }
        
            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)) {

                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11, targetAddress);
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12, targetAddress);
                PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13, durationNanos);

                PipeWriter.publishWrites(i2cOutput);

            }else {
                throw new UnsupportedOperationException("Pipe is too small for large volume of i2c data");
            }    
            
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
        
    }

    /**
     * @return True if the I2C bus is ready for communication, and false otherwise.
     */
    public boolean i2cIsReady() {
        return PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.hasRoomForFragmentOfSize(i2cOutput, MAX_COMMAND_FRAGMENTS_SIZE);
       
    }

    /**
     * Flushes all awaiting I2C data to the I2C bus for consumption.
     */
    public void i2cFlushBatch() {        
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            publishGo(runningI2CCommandCount,i2cPipeIdx);
            runningI2CCommandCount = 0;

        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }

    /**
     * TODO: What does this do?
     */
    public void i2cCommandClose() {  
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            if (++runningI2CCommandCount > maxCommands) {
                throw new UnsupportedOperationException("too many commands, found "+runningI2CCommandCount+" but only left room for "+maxCommands);
            }
            
            DataOutputBlobWriter.closeHighLevelField(i2cWriter, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            PipeWriter.publishWrites(i2cOutput);
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }        
    }

    /**
     * Submits an HTTP GET request asynchronously.
     *
     * The response to this HTTP GET will be sent to any HTTPResponseListeners
     * associated with the listener for this command channel.
     *
     * @param domain Root domain to submit the request to (e.g., google.com)
     * @param port Port to submit the request to.
     * @param route Route on the domain to submit the request to (e.g., /api/hello)
     *
     * @return True if the request was successfully submitted, and false otherwise.
     */
    public boolean httpGet(CharSequence domain, int port, CharSequence route) {
    	return httpGet(domain,port,route,(HTTPResponseListener)listener);

    }

    /**
     * Submits an HTTP GET request asynchronously.
     *
     * @param host Root domain to submit the request to (e.g., google.com)
     * @param port Port to submit the request to.
     * @param route Route on the domain to submit the request to (e.g., /api/hello)
     * @param listener {@link HTTPResponseListener} that will handle the response.
     *
     * @return True if the request was successfully submitted, and false otherwise.
     */
    public boolean httpGet(CharSequence host, int port, CharSequence route, HTTPResponseListener listener) {
    	
    	if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(httpRequest, ClientHTTPRequestSchema.MSG_HTTPGET_100)) {
                	    
    		PipeWriter.writeInt(httpRequest, ClientHTTPRequestSchema.MSG_HTTPGET_100_FIELD_PORT_1, port);
    		PipeWriter.writeUTF8(httpRequest, ClientHTTPRequestSchema.MSG_HTTPGET_100_FIELD_HOST_2, host);
    		PipeWriter.writeUTF8(httpRequest, ClientHTTPRequestSchema.MSG_HTTPGET_100_FIELD_PATH_3, route);
    		PipeWriter.writeInt(httpRequest, ClientHTTPRequestSchema.MSG_HTTPGET_100_FIELD_LISTENER_10, System.identityHashCode(listener));
    		PipeWriter.writeUTF8(httpRequest, ClientHTTPRequestSchema.MSG_HTTPGET_100_FIELD_HEADERS_7, "");
    		PipeWriter.publishWrites(httpRequest);
            
    		publishGo(1,subPipeIdx);
            
            return true;
        }        
        return false;
    	
    }

    /**
     * Submits an HTTP POST request asynchronously.
     *
     * The response to this HTTP POST will be sent to any HTTPResponseListeners
     * associated with the listener for this command channel.
     *
     * @param domain Root domain to submit the request to (e.g., google.com)
     * @param port Port to submit the request to.
     * @param route Route on the domain to submit the request to (e.g., /api/hello)
     *
     * @return True if the request was successfully submitted, and false otherwise.
     */
    public PayloadWriter httpPost(CharSequence domain, int port, CharSequence route) {
    	return httpPost(domain,port,route,(HTTPResponseListener)listener);    	
    }

    /**
     * Submits an HTTP POST request asynchronously.
     *
     * @param host Root domain to submit the request to (e.g., google.com)
     * @param port Port to submit the request to.
     * @param route Route on the domain to submit the request to (e.g., /api/hello)
     * @param listener {@link HTTPResponseListener} that will handle the response.
     *
     * @return True if the request was successfully submitted, and false otherwise.
     */
    public PayloadWriter httpPost(CharSequence host, int port, CharSequence route, HTTPResponseListener listener) {
    	if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(httpRequest, ClientHTTPRequestSchema.MSG_HTTPPOST_101)) {
                	    
    		PipeWriter.writeInt(httpRequest, ClientHTTPRequestSchema.MSG_HTTPPOST_101_FIELD_PORT_1, port);
    		PipeWriter.writeUTF8(httpRequest, ClientHTTPRequestSchema.MSG_HTTPPOST_101_FIELD_HOST_2, host);
    		PipeWriter.writeUTF8(httpRequest, ClientHTTPRequestSchema.MSG_HTTPPOST_101_FIELD_PATH_3, route);
    		PipeWriter.writeInt(httpRequest, ClientHTTPRequestSchema.MSG_HTTPPOST_101_FIELD_LISTENER_10, System.identityHashCode(listener));

            publishGo(1,subPipeIdx);
            
            PipeWriter.writeUTF8(httpRequest, ClientHTTPRequestSchema.MSG_HTTPPOST_101_FIELD_HEADERS_7, "");//no additional headers 
            
            PayloadWriter pw = (PayloadWriter) Pipe.outputStream(messagePubSub);
            pw.openField(ClientHTTPRequestSchema.MSG_HTTPPOST_101_FIELD_PAYLOAD_5,this);  
            return pw;
        } else {
        	return null;
        }    	
    }

    /**
     * Subscribes the listener associated with this command channel to
     * a topic.
     *
     * @param topic Topic to subscribe to.
     *
     * @return True if the topic was successfully subscribed to, and false
     *         otherwise.
     */
    public boolean subscribe(CharSequence topic) {
    	
    	if (null==listener || null == goPipe) {
    		throw new UnsupportedOperationException("Can not subscribe before startup. Call addSubscription when registering listener."); 
    	}
    	
        return subscribe(topic, (PubSubListener)listener);
    }

    /**
     * Subscribes a listener to a topic on this command channel.
     *
     * @param topic Topic to subscribe to.
     * @param listener Listener to subscribe.
     *
     * @return True if the topic was successfully subscribed to, and false
     *         otherwise.
     */
    public boolean subscribe(CharSequence topic, PubSubListener listener) {
        if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(messagePubSub, MessagePubSub.MSG_SUBSCRIBE_100)) {
            
            PipeWriter.writeInt(messagePubSub, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_SUBSCRIBERIDENTITYHASH_4, System.identityHashCode(listener));
            PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_TOPIC_1, topic);
            
            PipeWriter.publishWrites(messagePubSub);
            
            publishGo(1,subPipeIdx);
            
            return true;
        }        
        return false;
    }

    /**
     * Unsubscribes the listener associated with this command channel from
     * a topic.
     *
     * @param topic Topic to unsubscribe from.
     *
     * @return True if the topic was successfully unsubscribed from, and false otherwise.
     */
    public boolean unsubscribe(CharSequence topic) {
        return unsubscribe(topic, (PubSubListener)listener);
    }

    /**
     * Unsubscribes a listener from a topic.
     *
     * @param topic Topic to unsubscribe from.
     * @param listener Listener to unsubscribe.
     *
     * @return True if the topic was successfully unsubscribed from, and false otherwise.
     */
    public boolean unsubscribe(CharSequence topic, PubSubListener listener) {
        if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(messagePubSub, MessagePubSub.MSG_UNSUBSCRIBE_101)) {
            
            PipeWriter.writeInt(messagePubSub, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_SUBSCRIBERIDENTITYHASH_4, System.identityHashCode(listener));
            PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_UNSUBSCRIBE_101_FIELD_TOPIC_1, topic);
            
            PipeWriter.publishWrites(messagePubSub);
            
            publishGo(1,subPipeIdx);
            
            return true;
        }        
        return false;
    }

    /**
     * Changes the state of this command channel's state machine.
     *
     * @param state State to transition to.
     *
     * @return True if the state was successfully transitioned, and false otherwise.
     */
    public <E extends Enum<E>> boolean changeStateTo(E state) {
    	 assert(hardware.isValidState(state));
    	 if (!hardware.isValidState(state)) {
    		 throw new UnsupportedOperationException("no match "+state.getClass());
    	 }
    	
    	 if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(messagePubSub, MessagePubSub.MSG_CHANGESTATE_70)) {

    		 PipeWriter.writeInt(messagePubSub, MessagePubSub.MSG_CHANGESTATE_70_FIELD_ORDINAL_7,  state.ordinal());
             PipeWriter.publishWrites(messagePubSub);
             
             publishGo(1,subPipeIdx);
    		 return true;
    	 }

    	return false;
    	
    }

    /**
     * Opens a topic on this channel for writing.
     *
     * @param topic Topic to open.
     *
     * @return {@link PayloadWriter} attached to the given topic.
     */
    public PayloadWriter openTopic(CharSequence topic) {
        
        if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(messagePubSub, MessagePubSub.MSG_PUBLISH_103)) {
            
            PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1, topic);            
            PayloadWriter pw = (PayloadWriter) Pipe.outputStream(messagePubSub);
            		          
            pw.openField(MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3,this);            
                        
            return pw;
            
        } else {
            //breaks fluent api use because there is no place to write the data.
            //makers/students will have a lesson where we do new Optional(openTopic("topic"))  then  ifPresent() to only send when we can
            return null;
        }
    }




}