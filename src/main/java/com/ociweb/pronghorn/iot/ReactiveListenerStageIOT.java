package com.ociweb.pronghorn.iot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.HTTPResponseListener;
import com.ociweb.gl.api.RestListener;
import com.ociweb.gl.api.ShutdownListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.gl.impl.schema.MessageSubscription;
import com.ociweb.gl.impl.stage.ReactiveListenerStage;
import com.ociweb.iot.hardware.HardwareConnection;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.impl.SerialDataSchema;
import com.ociweb.iot.hardware.impl.SerialInputSchema;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.ListenerFilterIoT;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.iot.maker.SerialListener;
import com.ociweb.iot.maker.SerialReader;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.network.schema.HTTPRequestSchema;
import com.ociweb.pronghorn.network.schema.NetResponseSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.ma.MAvgRollerLong;

public class ReactiveListenerStageIOT extends ReactiveListenerStage<HardwareImpl> implements ListenerFilterIoT {

  
    private static final Logger logger = LoggerFactory.getLogger(ReactiveListenerStageIOT.class); 
    
    protected MAvgRollerLong[] rollingMovingAveragesAnalog;
    protected MAvgRollerLong[] rollingMovingAveragesDigital;    
    private boolean startupCompleted;
    
    protected int[] oversampledAnalogValues;

    private static final int MAX_PORTS = 10;
    
    //for analog values returns the one with the longest run within the last n samples
    protected static final int OVERSAMPLE = 3; //  (COUNT), SAMPLE1, ... SAMPLEn
    protected static final int OVERSAMPLE_STEP = OVERSAMPLE+1;
    
    protected int[] lastDigitalValues;
    protected long[] lastDigitalTimes;
    
    protected boolean[] sendEveryAnalogValue;
    protected boolean[] sendEveryDigitalValue;
    
    
    protected int[] lastAnalogValues;
    protected long[] lastAnalogTimes;

    
    /////////////////////
    //Listener Filters
    /////////////////////    
    private Port[] includedPorts;//if null then all values are accepted
    private Port[] excludedPorts;//if null then no values are excluded
    private int[] includedI2Cs;//if null then all values are accepted
    private int[] excludedI2Cs;//if null then no values are excluded
		
    /////////////////////
    private Number stageRate;
    
    private SerialReader serialStremReader; //must be held as we accumulate serial data.
    
    public ReactiveListenerStageIOT(GraphManager graphManager, Object listener, 
    		                        Pipe<?>[] inputPipes, 
    		                        Pipe<?>[] outputPipes, 
    		                        HardwareImpl hardware, int parallelInstance) {

        
        super(graphManager, listener, inputPipes, outputPipes, hardware, parallelInstance);

        this.builder = hardware;
                   
        //allow for shutdown upon shutdownRequest we have new content
        GraphManager.addNota(graphManager, GraphManager.PRODUCER, GraphManager.PRODUCER, this);
                
    }

    private void setupMovingAverages(HardwareImpl hardware, MAvgRollerLong[] target, HardwareConnection[] con) {
        int i = con.length;
        while (--i >= 0) {            
              target[hardware.convertToPort(con[i].register)] = new MAvgRollerLong(con[i].movingAverageWindowMS/con[i].responseMS);
        }        
    }

    
    protected int findStableReading(int tempValue, int connector) { 
        return findMedian(updateRunLenghtOfActiveValue(tempValue, connector));
    }

    
    private int findMedian(int offset) {
        assert(3 == OVERSAMPLE);//ONLY WORKS FOR 3
        
        int a = oversampledAnalogValues[offset+1];
        int b = oversampledAnalogValues[offset+2];
        int c = oversampledAnalogValues[offset+3];
        
        //TODO: what if we returned the floor?
        
        if (a>b) {
            if (b>c) {
                return b;
            } else {
                return c;
            }
        } else {
            //b > a
            if (a>c) {
                return a;
            } else {
                return c;
            }
        }
        
    }

    private int updateRunLenghtOfActiveValue(int tempValue, int connector) {

        //store this value with the oversamples
        int offset = connector*OVERSAMPLE_STEP;
        int pos =       oversampledAnalogValues[offset];                           
        
        if (--pos<=0) {
            pos = OVERSAMPLE;
        }
        oversampledAnalogValues[offset]     = pos;
        oversampledAnalogValues[offset+pos] = tempValue;
        return offset;
    }
    
    
    @Override
    public void startup() {
    	
        //Init all the moving averages to the right size
        rollingMovingAveragesAnalog = new MAvgRollerLong[MAX_PORTS];
        rollingMovingAveragesDigital = new MAvgRollerLong[MAX_PORTS];
        
        HardwareConnection[] analogInputs = builder.getAnalogInputs();
        HardwareConnection[] digitalInputs = builder.getDigitalInputs();
        
		setupMovingAverages(builder, rollingMovingAveragesAnalog, analogInputs);
              
        setupMovingAverages(builder, rollingMovingAveragesDigital, builder.getDigitalInputs());
          
        lastDigitalValues = new int[MAX_PORTS];
        lastAnalogValues = new int[MAX_PORTS];
        
        sendEveryAnalogValue = new boolean[MAX_PORTS];
        sendEveryDigitalValue = new boolean[MAX_PORTS];
        
        int a = analogInputs.length;
        while (--a>=0) {        	
        	HardwareConnection con = analogInputs[a];
        	//System.out.println("seems wrong to covert this: "+con.register);
        	sendEveryAnalogValue[builder.convertToPort(con.register)] = con.sendEveryValue;
        }
        
        int d = digitalInputs.length;
        while (--d>=0) {        	
        	HardwareConnection con = digitalInputs[d];
        	//System.out.println("seems wrong to covert this: "+con.register);
        	sendEveryDigitalValue[builder.convertToPort(con.register)] = con.sendEveryValue;        	
        }
        
        
        lastDigitalTimes = new long[MAX_PORTS];
        lastAnalogTimes = new long[MAX_PORTS];
                    
        oversampledAnalogValues = new int[MAX_PORTS*OVERSAMPLE_STEP];
        
        stageRate = (Number)graphManager.getNota(graphManager, this.stageId,  GraphManager.SCHEDULE_RATE, null);
        
        timeProcessWindow = (null==stageRate? 0 : (int)(stageRate.longValue()/MS_to_NS));
        
        
        
        //Do last so we complete all the initializations first
        super.startup();
    }

    @Override
    public void run() {
        
    	if (shutdownRequsted.get()) {
    		if (!shutdownCompleted) {
    			
    			if (listener instanceof ShutdownListener) {    				
    				if (((ShutdownListener)listener).acceptShutdown()) {
    					int remaining = liveShutdownListeners.decrementAndGet();
    					assert(remaining>=0);
    					requestShutdown();
    					return;
    				}
    				//else continue with normal run processing
    				
    			} else {
    				//this one is not a listener so we must wait for all the listeners to close first
    				
    				if (0 == liveShutdownListeners.get()) {    					
    					requestShutdown();
    					return;
    				}
    				//else continue with normal run processing.
    				
    			}
    		} else {
    			assert(shutdownCompleted);
    			assert(false) : "run should not have been called if this stage was shut down.";
    			return;
    		}
    	}
    	
        if (timeEvents) {         	
			processTimeEvents((TimeListener)listener, timeTrigger);            
		}
        
        //TODO: replace with linked list of processors?, NOTE each one also needs a length bound so it does not starve the rest.
        
        int p = inputPipes.length;
        //logger.trace("input pipes "+p);
        while (--p >= 0) {
            //TODO: this solution works but smells, a "process" lambda added to the Pipe may be a better solution? Still thinking....

        	//logger.trace(inputPipes[p].schemaName(inputPipes[p]));
        	
            if (Pipe.isForSchema((Pipe<SerialInputSchema>)inputPipes[p], SerialInputSchema.class)) {
            	//logger.trace("checking for  consumeSerialMessage");
            	consumeSerialMessage((SerialListener)listener, (Pipe<SerialInputSchema>) inputPipes[p]);
            } else if (Pipe.isForSchema((Pipe<GroveResponseSchema>)inputPipes[p], GroveResponseSchema.class)) {
                consumeResponseMessage(listener, (Pipe<GroveResponseSchema>) inputPipes[p]);
            } else if (Pipe.isForSchema((Pipe<I2CResponseSchema>)inputPipes[p], I2CResponseSchema.class)) {
            	//listener may be analog or digital if we are using the grovePi board            	
            	consumeI2CMessage(listener, (Pipe<I2CResponseSchema>) inputPipes[p]);
            } else if (Pipe.isForSchema((Pipe<MessageSubscription>)inputPipes[p], MessageSubscription.class)) {                
                consumePubSubMessage(listener, (Pipe<MessageSubscription>) inputPipes[p]);
            } else if (Pipe.isForSchema((Pipe<NetResponseSchema>)inputPipes[p], NetResponseSchema.class)) {
               //should only have this pipe if listener is also instance of HTTPResponseListener
               //HTTP response from our request earlier
               consumeNetResponse((HTTPResponseListener)listener, (Pipe<NetResponseSchema>) inputPipes[p]);
            } else if (Pipe.isForSchema((Pipe<HTTPRequestSchema>)inputPipes[p], HTTPRequestSchema.class)) {            	
               //HTTP reqeust to our internal server from the outside
               consumeRestRequest((RestListener)listener, (Pipe<HTTPRequestSchema>) inputPipes[p]);                           
            } else 
            {   // HTTPRequestSchema
                logger.error("unrecognized pipe sent to listener of type {} ", Pipe.schemaName(inputPipes[p]));
            }
        }
        
        
    }
        
    
    protected void consumeI2CMessage(Object listener, Pipe<I2CResponseSchema> p) {

        while (PipeReader.tryReadFragment(p)) {                
                    
                    int msgIdx = PipeReader.getMsgIdx(p);
                    switch (msgIdx) {   
                        case I2CResponseSchema.MSG_RESPONSE_10:
                        	processI2CMessage(listener, p);
                            break;
                        case -1:
                            
                            requestShutdown();
                            PipeReader.releaseReadLock(p);
                            return;
                           
                        default:
                            throw new UnsupportedOperationException("Unknown id: "+msgIdx);
                        
                    }
                    //done reading message off pipe
                    PipeReader.releaseReadLock(p);
        }
    }

	protected void processI2CMessage(Object listener, Pipe<I2CResponseSchema> p) {

			int addr = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11);
			long time = PipeReader.readLong(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13);
			int register = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14);

			byte[] backing = PipeReader.readBytesBackingArray(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int position = PipeReader.readBytesPosition(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int length = PipeReader.readBytesLength(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int mask = PipeReader.readBytesMask(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
		    
		    commonI2CEventProcessing((I2CListener) listener, addr, register, time, backing, position, length, mask);

	}


	
	private void consumeSerialMessage(SerialListener serial, Pipe<SerialInputSchema> p) {

		while (PipeReader.tryReadFragment(p)) {
		    int msgIdx = PipeReader.getMsgIdx(p);
		    int consumed = 0;
		    switch(msgIdx) {
		        case SerialInputSchema.MSG_CHUNKEDSTREAM_1:
		        	if (null==serialStremReader) {
		        		serialStremReader = (SerialReader)PipeReader.inputStream(p, SerialInputSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
		        	} else {
		        		serialStremReader.accumHighLevelAPIField(SerialInputSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
		        	}		 
		        	//let the behavior consume some or all of the serial data
		        	int originalPosition = serialStremReader.absolutePosition();
		        	
		        	//System.err.println("reading from position "+originalPosition);
		        	consumed = serial.message(serialStremReader);
		        	//System.err.println("reading total size "+consumed+" now pos "+serialStremReader.absolutePosition());
		        	
		        	//set position for the next call to ensure we are aligned after the consumed bytes
		        	//the behavior could read past consumed and choose not to consume it yet.
		        	serialStremReader.absolutePosition(originalPosition+consumed);
		        	assert(consumed>=0) : "can not consume negative value";
		        	
		        break;
		        case -1:
		            requestShutdown();
		        break;
		    }
		    PipeReader.readNextWithoutReleasingReadLock(p);
		    PipeReader.releaseAllPendingReadLock(p, consumed);
		}
		
	}


    

    protected void consumeResponseMessage(Object listener, Pipe<GroveResponseSchema> p) {
        while (PipeReader.tryReadFragment(p)) {                
            
            int msgIdx = PipeReader.getMsgIdx(p);
            switch (msgIdx) {   

                case GroveResponseSchema.MSG_ANALOGSAMPLE_30:
                    if (listener instanceof AnalogListener) {                        
                        commonAnalogEventProcessing(Port.ANALOGS[PipeReader.readInt(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_CONNECTOR_31)],
                        				            PipeReader.readLong(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_TIME_11), 
                        				            PipeReader.readInt(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_VALUE_32), 
                        				            (AnalogListener)listener);
                        
                    }   
                break;               
                case GroveResponseSchema.MSG_DIGITALSAMPLE_20:
                    
                    if (listener instanceof DigitalListener) {
                        commonDigitalEventProcessing(Port.DIGITALS[PipeReader.readInt(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_CONNECTOR_21)], 
                        		                     PipeReader.readLong(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_TIME_11), 
                        		                     PipeReader.readInt(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_VALUE_22), 
                        		                     (DigitalListener)listener);
                    }   
                break; 
                case GroveResponseSchema.MSG_ENCODER_70:
                    
                    if (listener instanceof RotaryListener) {    
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_CONNECTOR_71);
                        long time = PipeReader.readLong(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_TIME_11);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_VALUE_72);
                        int delta = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_DELTA_73);
                        int speed = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_SPEED_74);
                        long duration = PipeReader.readLong(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_PREVDURATION_75);
                        
                        ((RotaryListener)listener).rotaryEvent(Port.DIGITALS[connector], time, value, delta, speed);
                                            
                    }   
                break;
                case -1:
                {    
                    requestShutdown();
                    PipeReader.releaseReadLock(p);
                    return;
                }   
                default:
                    throw new UnsupportedOperationException("Unknown id: "+msgIdx);
            }               
            
            //done reading message off pipe
            PipeReader.releaseReadLock(p);
        }
    }
    

	protected void commonI2CEventProcessing(I2CListener listener, int addr, int register, long time, byte[] backing, int position, int length, int mask) {
		if (isIncluded(addr, includedI2Cs) && isNotExcluded(addr, excludedI2Cs)) {
			listener.i2cEvent(addr, register, time, backing, position, length, mask);
		}
	}
	    
    
	protected void commonDigitalEventProcessing(Port port, long time, int value, DigitalListener dListener) {
		
		if (isIncluded(port, includedPorts) && isNotExcluded(port, excludedPorts)) {

			if (sendEveryDigitalValue[port.port]) {
				dListener.digitalEvent(port, time, 0==lastDigitalTimes[port.port] ? -1 : time-lastDigitalTimes[port.port], value);				
				if(value!=lastDigitalValues[port.port]){  
					lastDigitalValues[port.port] = value;
			    	lastDigitalTimes[port.port] = time;
				}
				
			} else {			
				if(value!=lastDigitalValues[port.port]){  
					dListener.digitalEvent(port, time, 0==lastDigitalTimes[port.port] ? -1 : time-lastDigitalTimes[port.port], value);
				    lastDigitalValues[port.port] = value;
				    lastDigitalTimes[port.port] = time;
				}
			}
		}
	}

	protected void commonAnalogEventProcessing(Port port, long time, int value, AnalogListener aListener) {
		
		if (isIncluded(port, includedPorts) && isNotExcluded(port, excludedPorts)) {
			
			int runningValue = sendEveryAnalogValue[port.port] ? value : findStableReading(value, port.port);             
			
			//logger.debug(port+" send every value "+sendEveryAnalogValue[port.port]);
			
			MAvgRollerLong.roll(rollingMovingAveragesAnalog[port.port], runningValue);                                                
			
			int mean = runningValue;
			if (MAvgRollerLong.isValid(rollingMovingAveragesAnalog[port.port])) {
				mean = (int)MAvgRollerLong.mean(rollingMovingAveragesAnalog[port.port]);
			}
			
			if (sendEveryAnalogValue[port.port]) {
				//set time first so this is 0 the moment it shows up
				//since we send every read we can send the age as greater and geater values as long as it does not change.
				if(runningValue!=lastAnalogValues[port.port]){ 
					lastAnalogTimes[port.port] = time;   
					lastAnalogValues[port.port] = runningValue;
				}
				aListener.analogEvent(port, time, 0==lastAnalogTimes[port.port] ? Long.MAX_VALUE : time-lastAnalogTimes[port.port], mean, runningValue);
				
			} else {								
				if(runningValue!=lastAnalogValues[port.port]){ 
										
					//the duration here is the duration of how long the previous value was held.
					aListener.analogEvent(port, time, 0==lastAnalogTimes[port.port] ? Long.MAX_VALUE : time-lastAnalogTimes[port.port], mean, runningValue);
				   
					lastAnalogValues[port.port] = runningValue;
				    lastAnalogTimes[port.port] = time;
				}
			}
		}
	}
	
	@Override
	public ListenerFilterIoT includePorts(Port ... ports) {
		if (!startupCompleted && (listener instanceof AnalogListener || listener instanceof DigitalListener)) {
			includedPorts = ports;
			return this;
		} else {
			if (startupCompleted) {
	    		throw new UnsupportedOperationException("ListenerFilters may only be set before startup is called.  Eg. the filters can not be changed at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of AnalogLister or DigitalListener in order to call this method.");
	    	}
		}
	}

	@Override
	public ListenerFilterIoT excludePorts(Port ... ports) {
		if (!startupCompleted && (listener instanceof AnalogListener || listener instanceof DigitalListener)) {
			excludedPorts = ports;
			return this;
		} else {
			if (startupCompleted) {
	    		throw new UnsupportedOperationException("ListenerFilters may only be set before startup is called.  Eg. the filters can not be changed at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of AnalogLister or DigitalListener in order to call this method.");
	    	}
		}
	}

	@Override
	public ListenerFilterIoT includeI2CConnections(int ... addresses) {
		if (!startupCompleted && listener instanceof I2CListener) {
			includedI2Cs = addresses;
			return this;
		} else {
			if (startupCompleted) {
	    		throw new UnsupportedOperationException("ListenerFilters may only be set before startup is called.  Eg. the filters can not be changed at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of DigitalLister in order to call this method.");
	    	}
		}
	}

	@Override
	public ListenerFilterIoT excludeI2CConnections(int... addresses) {
		if (!startupCompleted && listener instanceof I2CListener) {
			excludedI2Cs = addresses;
			return this;
	    } else {
	    	if (startupCompleted) {
	    		throw new UnsupportedOperationException("ListenerFilters may only be set before startup is called.  Eg. the filters can not be changed at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of DigitalLister in order to call this method.");
	    	}
	    }
	}
	
	private <E extends Enum<E>> long[] buildMaskArray(E[] state) {
		int maxOrdinal = findMaxOrdinal(state);
		int a = maxOrdinal >> 6;
		int b = maxOrdinal & 0x3F;		
		int longsCount = a+(b==0?0:1);
		
		long[] array = new long[longsCount+1];
				
		int i = state.length;
		while (--i>=0) {			
			int ordinal = state[i].ordinal();			
			array[ordinal>>6] |=  1L << (ordinal & 0x3F);			
		}
		return array;
	}

	private <E extends Enum<E>> int findMaxOrdinal(E[] state) {
		int maxOrdinal = -1;
		int i = state.length;
		while (--i>=0) {
			maxOrdinal = Math.max(maxOrdinal, state[i].ordinal());
		}
		return maxOrdinal;
	}

    
    
}
