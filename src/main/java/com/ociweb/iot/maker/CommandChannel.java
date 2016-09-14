package com.ociweb.iot.maker;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.NetRequestSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Pool;
import static com.ociweb.iot.maker.Port.*;

public abstract class CommandChannel {

    protected final Pipe<?>[] outputPipes;
    protected final Pipe<TrafficOrderSchema> goPipe;
    protected final Pipe<MessagePubSub> messagePubSub;
    protected final Pipe<I2CCommandSchema> i2cOutput;
    protected final Pipe<GroveRequestSchema> output;
    protected final Pipe<NetRequestSchema> httpRequest;
    
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
    private HardwareImpl hardware;
	private final int MAX_COMMAND_FRAGMENTS_SIZE;
        
    protected CommandChannel(GraphManager gm, HardwareImpl hardware,
                             Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput,  
                             Pipe<MessagePubSub> messagePubSub,  //avoid adding more and see how they can be combined.
                             Pipe<NetRequestSchema> httpRequest,
                             Pipe<TrafficOrderSchema> goPipe) {
       this.outputPipes = new Pipe<?>[]{output,i2cOutput,messagePubSub,goPipe};
       this.goPipe = goPipe;
       this.messagePubSub = messagePubSub;
       this.i2cOutput = i2cOutput;
       this.httpRequest = httpRequest;
                             
       if (Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands >= this.i2cOutput.sizeOfSlabRing) {
           throw new UnsupportedOperationException("maxCommands too large or pipe is too small, pipe size must be at least "+(Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands));
       }
       this.hardware = hardware;
       this.output = output;
       
       MAX_COMMAND_FRAGMENTS_SIZE = Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands;
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
    
    public abstract boolean block(long msDuration);
    
    public abstract boolean block(Port port, long durationMilli);
    
    public abstract boolean blockUntil(Port port, long time);
    
    public abstract boolean setValue(Port port, int value);
    public abstract boolean setValueAndBlock(Port port, int value, long durationMilli);
    
    public abstract boolean digitalPulse(Port port);
    public abstract boolean digitalPulse(Port port, long durationNanos);

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
    
    public boolean i2cIsReady() {
        
        return PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.hasRoomForFragmentOfSize(i2cOutput, MAX_COMMAND_FRAGMENTS_SIZE);
       
    }
    

    public void i2cFlushBatch() {        
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            publishGo(runningI2CCommandCount,i2cPipeIdx);
            runningI2CCommandCount = 0;

        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }



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
    
    
    public boolean httpGet(CharSequence domain, int port, CharSequence route) {
    	return httpGet(domain,port,route,(HTTPResponseListener)listener);
    	
    }
    public boolean httpGet(CharSequence host, int port, CharSequence route, HTTPResponseListener listener) {
    	//Pipe<NetRequestSchema> httpRequest
    	if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(httpRequest, NetRequestSchema.MSG_HTTPGET_100)) {
                	    
    		PipeWriter.writeInt(httpRequest, NetRequestSchema.MSG_HTTPGET_100_FIELD_PORT_1, port);
    		PipeWriter.writeUTF8(httpRequest, NetRequestSchema.MSG_HTTPGET_100_FIELD_HOST_2, host);
    		PipeWriter.writeUTF8(httpRequest, NetRequestSchema.MSG_HTTPGET_100_FIELD_PATH_3, route);
    		PipeWriter.writeInt(httpRequest, NetRequestSchema.MSG_HTTPGET_100_FIELD_LISTENER_10, System.identityHashCode(listener));
    		PipeWriter.publishWrites(httpRequest);
            
    		publishGo(1,subPipeIdx);
            
            return true;
        }        
        return false;
    	
    }
    
    public PayloadWriter httpPost(CharSequence domain, int port, CharSequence route) {
    	return httpPost(domain,port,route,(HTTPResponseListener)listener);    	
    }
    
    public PayloadWriter httpPost(CharSequence host, int port, CharSequence route, HTTPResponseListener listener) {
    	if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(httpRequest, NetRequestSchema.MSG_HTTPPOST_101)) {
                	    
    		PipeWriter.writeInt(httpRequest, NetRequestSchema.MSG_HTTPPOST_101_FIELD_PORT_1, port);
    		PipeWriter.writeUTF8(httpRequest, NetRequestSchema.MSG_HTTPPOST_101_FIELD_HOST_2, host);
    		PipeWriter.writeUTF8(httpRequest, NetRequestSchema.MSG_HTTPPOST_101_FIELD_PATH_3, route);
    		PipeWriter.writeInt(httpRequest, NetRequestSchema.MSG_HTTPPOST_101_FIELD_LISTENER_10, System.identityHashCode(listener));

            publishGo(1,subPipeIdx);
            
            PayloadWriter pw = (PayloadWriter) Pipe.outputStream(messagePubSub);    
            pw.openField(NetRequestSchema.MSG_HTTPPOST_101_FIELD_PAYLOAD_5,this);  
            return pw;
        } else {
        	return null;
        }    	
    }
    
    public boolean subscribe(CharSequence topic) {
        return subscribe(topic, (PubSubListener)listener);
    }
    
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

    public boolean unsubscribe(CharSequence topic) {
        return unsubscribe(topic, (PubSubListener)listener);
    }
    
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