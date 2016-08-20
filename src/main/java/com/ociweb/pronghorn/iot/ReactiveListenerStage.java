package com.ociweb.pronghorn.iot;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.HardwareConnection;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.ListenerFilter;
import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.iot.maker.RestListener;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.iot.maker.StartupListener;
import com.ociweb.iot.maker.TimeListener;
import com.ociweb.iot.maker.StateChangeListener;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.MessageSubscription;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.ma.MAvgRollerLong;

public class ReactiveListenerStage extends PronghornStage implements ListenerFilter {

    protected final Object              listener;
    
    protected final Pipe<?>[]           inputPipes;
    protected final Pipe<?>[]           outputPipes;
        
    protected long                      timeTrigger;
    protected long                      timeRate;   
    
    protected HardwareImpl					hardware;
  
    private static final Logger logger = LoggerFactory.getLogger(ReactiveListenerStage.class); 
    
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
    protected int[] lastAnalogValues;
    protected long[] lastAnalogTimes;
    
    private final Enum[] states;
    
    /////////////////////
    //Listener Filters
    /////////////////////    
    private Port[] includedAnalogs;//if null then all values are accepted
    private Port[] excludedAnalogs;//if null then no values are excluded
    private Port[] includedDigitals;//if null then all values are accepted
    private Port[] excludedDigitals;//if null then no values are excluded
    private int[] includedI2Cs;//if null then all values are accepted
    private int[] excludedI2Cs;//if null then no values are excluded
    private long[] includedToStates;
    private long[] includedFromStates;
    private long[] excludedToStates;
    private long[] excludedFromStates;
		
    /////////////////////
    private Number stageRate;
    private final GraphManager graphManager;
    
    public ReactiveListenerStage(GraphManager graphManager, Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes, HardwareImpl hardware) {

        
        super(graphManager, inputPipes, outputPipes);
        this.listener = listener;

        this.inputPipes = inputPipes;
        this.outputPipes = outputPipes;       
        this.hardware = hardware;
        
        this.states = hardware.getStates();
        this.graphManager = graphManager;
                   
        //allow for shutdown upon shutdownRequest we have new content
        GraphManager.addNota(graphManager, GraphManager.PRODUCER, GraphManager.PRODUCER, this);
        
    }

    private void setupMovingAverages(HardwareImpl hardware, MAvgRollerLong[] target, HardwareConnection[] con) {
        int i = con.length;
        while (--i >= 0) {
            
              int ms   = con[i].movingAverageWindowMS;
              int rate = con[i].responseMS;
              System.out.println("count "+(ms/rate)+" ms"+ms+"  rate"+rate);
              
              target[hardware.convertToPort(con[i].register)] = new MAvgRollerLong(ms/rate);
              System.out.println("expecting moving average on connection "+con[i].register);
        }        
    }
    
    
    
    public void setTimeEventSchedule(long rate) {
        
        timeRate = rate;
        long now = hardware.currentTimeMillis();
        if (timeTrigger <= now) {
            timeTrigger = now + timeRate;
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
        
        setupMovingAverages(hardware, rollingMovingAveragesAnalog, hardware.getAnalogInputs());
              
        setupMovingAverages(hardware, rollingMovingAveragesDigital, hardware.getDigitalInputs());
          
        lastDigitalValues = new int[MAX_PORTS];
        lastAnalogValues = new int[MAX_PORTS];
        
        sendEveryAnalogValue = new boolean[MAX_PORTS];
        int a = hardware.getAnalogInputs().length;
        while (--a>=0) {
        	
        	HardwareConnection con = hardware.getAnalogInputs()[a];
        	
        	sendEveryAnalogValue[hardware.convertToPort(con.register)] = con.sendEveryValue;   
        	
        }
        
        lastDigitalTimes = new long[MAX_PORTS];
        lastAnalogTimes = new long[MAX_PORTS];
                    
        oversampledAnalogValues = new int[MAX_PORTS*OVERSAMPLE_STEP];
        
        stageRate = (Number)graphManager.getNota(graphManager, this.stageId,  GraphManager.SCHEDULE_RATE, null);
        
        
        //Do last so we complete all the initializations first
        if (listener instanceof StartupListener) {
        	((StartupListener)listener).startup();
        }        
        startupCompleted=true;
    }

    @Override
    public void run() {
        
        processTimeEvents(listener);
        
        //TODO: replace with linked list of processors?, NOTE each one also needs a length bound so it does not starve the rest.
        
        int p = inputPipes.length;
        
        while (--p >= 0) {
            //TODO: this solution works but smells, a "process" lambda added to the Pipe may be a better solution? Still thinking....

            Pipe<?> localPipe = inputPipes[p];

            if (Pipe.isForSchema(localPipe, GroveResponseSchema.instance)) {
                consumeResponseMessage(listener, (Pipe<GroveResponseSchema>) localPipe);
            } else
            if (Pipe.isForSchema(localPipe, I2CResponseSchema.instance)) {                
                consumeI2CMessage(listener, (Pipe<I2CResponseSchema>) localPipe);
            } else
            if (Pipe.isForSchema(localPipe, MessageSubscription.instance)) {                
                consumePubSubMessage(listener, (Pipe<MessageSubscription>) localPipe);
            } else 
            {
                logger.error("unrecognized pipe sent to listener of type {} ", Pipe.schemaName(localPipe));
            }
        }
        
        
    }

    private StringBuilder workspace = new StringBuilder();
    private PayloadReader payloadReader;
    
    private void consumePubSubMessage(Object listener, Pipe<MessageSubscription> p) {
        while (PipeReader.tryReadFragment(p)) {                
            
            int msgIdx = PipeReader.getMsgIdx(p);
            switch (msgIdx) {
                case MessageSubscription.MSG_PUBLISH_103:
                    if (listener instanceof PubSubListener) {
	                    workspace.setLength(0);
	                    CharSequence topic = PipeReader.readUTF8(p, MessageSubscription.MSG_PUBLISH_103_FIELD_TOPIC_1, workspace);               
	                    
	                    if (null==payloadReader) {
	                        payloadReader = new PayloadReader(p); 
	                    }
	                    
	                    payloadReader.openHighLevelAPIField(MessageSubscription.MSG_PUBLISH_103_FIELD_PAYLOAD_3);
	
	//                    if (! payloadReader.markSupported() ) {
	//                        logger.warn("we need mark to be suppported for payloads in pubsub and http."); //TODO: need to implement mark, urgent.                      
	//                    }
	                    
	                    ((PubSubListener)listener).message(topic, payloadReader);
                    }
                    break;
                case MessageSubscription.MSG_STATECHANGED_71:
                	if (listener instanceof StateChangeListener) {
                		
                		int oldOrdinal = PipeReader.readInt(p, MessageSubscription.MSG_STATECHANGED_71_FIELD_OLDORDINAL_8);
                		int newOrdinal = PipeReader.readInt(p, MessageSubscription.MSG_STATECHANGED_71_FIELD_NEWORDINAL_9);
                		if (isIncluded(newOrdinal, includedToStates) && isIncluded(oldOrdinal, includedFromStates) &&
                			isNotExcluded(newOrdinal, excludedToStates) && isNotExcluded(oldOrdinal, excludedFromStates) ) {			                			
                			((StateChangeListener)listener).stateChange(states[oldOrdinal], states[newOrdinal]);
                		}
						
                	}
                    break;
                case -1:
                    
                    requestShutdown();
                    PipeReader.releaseReadLock(p);
                    return;
                   
                default:
                    throw new UnsupportedOperationException("Unknown id: "+msgIdx);
                
            }
            PipeReader.releaseReadLock(p);
        }
    }
        

	private static final long MS_to_NS = 1_000_000;

	private void processTimeEvents(Object listener) {
        //if we do have a clock schedule
        if (0 != timeRate) {
            if (listener instanceof TimeListener) {
                            	
                long timeToWait = (timeTrigger-hardware.currentTimeMillis())*MS_to_NS;
                if (null!=stageRate && (timeToWait >= stageRate.longValue())) { //if its not near, leave
                	return;
                }
                while (hardware.currentTimeMillis()<timeTrigger) {
                	Thread.yield();
                	if (Thread.interrupted()) {
                		Thread.currentThread().interrupt();
                		return;
                	}
                }
                ((TimeListener)listener).timeEvent(timeTrigger);
                timeTrigger += timeRate;
                
            }
        }
    }


    private void consumeRestMessage(Object listener2, Pipe<?> p) {
        if (null!= p) {
            
            while (PipeReader.tryReadFragment(p)) {                
                
                int msgIdx = PipeReader.getMsgIdx(p);
                
                //no need to check instance of since this was registered and we have a pipe
                ((RestListener)listener).restRequest(1, null);
                
                //done reading message off pipe
                PipeReader.releaseReadLock(p);
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
		if (listener instanceof I2CListener) {
			int addr = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11);
			long time = PipeReader.readLong(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13);
			int register = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14);

			byte[] backing = PipeReader.readBytesBackingArray(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int position = PipeReader.readBytesPosition(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int length = PipeReader.readBytesLength(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int mask = PipeReader.readBytesMask(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
		    
		    commonI2CEventProcessing((I2CListener)listener, addr, register, time, backing, position, length, mask);
		   
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
		
		if (isIncluded(port, includedDigitals) && isNotExcluded(port, excludedDigitals)) {

			if(value!=lastDigitalValues[port.port]){  //TODO: add switch   
				dListener.digitalEvent(port, time, 0==lastDigitalTimes[port.port] ? -1 : time-lastDigitalTimes[port.port], value);
			    lastDigitalValues[port.port] = value;
			    lastDigitalTimes[port.port] = time;
			}
		}
	}

	protected void commonAnalogEventProcessing(Port port, long time, int value, AnalogListener aListener) {
		
		if (isIncluded(port, includedAnalogs) && isNotExcluded(port, excludedAnalogs)) {
			
			int runningValue = sendEveryAnalogValue[port.port] ? value : findStableReading(value, port.port);             
			
			MAvgRollerLong.roll(rollingMovingAveragesAnalog[port.port], runningValue);                                                
			
			int mean = runningValue;
			if (MAvgRollerLong.isValid(rollingMovingAveragesAnalog[port.port])) {
				mean = (int)MAvgRollerLong.mean(rollingMovingAveragesAnalog[port.port]);
			}
			
			if (sendEveryAnalogValue[port.port]) {
				
				aListener.analogEvent(port, time, 0==lastAnalogTimes[port.port] ? Long.MAX_VALUE : time-lastAnalogTimes[port.port], mean, runningValue);
				lastAnalogTimes[port.port] = time;   
				
			} else {								
				if(value!=lastAnalogValues[port.port]){   //TODO: add switch
					
					aListener.analogEvent(port, time, 0==lastAnalogTimes[port.port] ? Long.MAX_VALUE : time-lastAnalogTimes[port.port], mean, runningValue);
				   
					lastAnalogValues[port.port] = value;
				    lastAnalogTimes[port.port] = time;
				}
			}
		}
	}
        
    
    private boolean isNotExcluded(int newOrdinal, long[] excluded) {
    	if (null!=excluded) {
    		return 0 == (excluded[newOrdinal>>6] & (1L<<(newOrdinal & 0x3F)));			
		}
		return true;
	}

	private boolean isIncluded(int newOrdinal, long[] included) {
		if (null!=included) {			
			return 0 != (included[newOrdinal>>6] & (1L<<(newOrdinal & 0x3F)));
		}
		return true;
	}
	
	private <T> boolean isNotExcluded(T port, T[] excluded) {
		if (null!=excluded) {
			int e = excluded.length;
			while (--e>=0) {
				if (excluded[e]==port) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isNotExcluded(int a, int[] excluded) {
		if (null!=excluded) {
			int e = excluded.length;
			while (--e>=0) {
				if (excluded[e]==a) {
					return false;
				}
			}
		}
		return true;
	}
	
	private <T> boolean isIncluded(T port, T[] included) {
		if (null!=included) {
			int i = included.length;
			while (--i>=0) {
				if (included[i]==port) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	private boolean isIncluded(int a, int[] included) {
		if (null!=included) {
			int i = included.length;
			while (--i>=0) {
				if (included[i]==a) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	@Override
	public ListenerFilter includeAnalogConnections(Port ... ports) {
		if (!startupCompleted && listener instanceof AnalogListener) {
			includedAnalogs = ports;
			return this;
		} else {
			if (startupCompleted) {
	    		throw new UnsupportedOperationException("ListenerFilters may only be set before startup is called.  Eg. the filters can not be changed at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of AnalogLister in order to call this method.");
	    	}
		}
	}

	@Override
	public ListenerFilter excludeAnalogConnections(Port ... ports) {
		if (!startupCompleted && listener instanceof AnalogListener) {
			excludedAnalogs = ports;
			return this;
		} else {
			if (startupCompleted) {
	    		throw new UnsupportedOperationException("ListenerFilters may only be set before startup is called.  Eg. the filters can not be changed at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of AnalogLister in order to call this method.");
	    	}
		}
	}

	@Override
	public ListenerFilter includeDigitalConnections(Port ... ports) {
		if (!startupCompleted && listener instanceof DigitalListener) {
			includedDigitals = ports;
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
	public ListenerFilter excludeDigitalConnections(Port ... ports) {
		if (!startupCompleted && listener instanceof DigitalListener) {
			excludedDigitals = ports;
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
	public ListenerFilter addSubscription(CharSequence topic) {		
		if (!startupCompleted && listener instanceof PubSubListener) {
			hardware.addStartupSubscription(topic, System.identityHashCode(this));		
			return this;
		} else {
			if (startupCompleted) {
	    		throw new UnsupportedOperationException("Call addSubscription on CommandChanel to modify subscriptions at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of PubSubListener in order to call this method.");
	    	}
		}
	}

	@Override
	public ListenerFilter includeI2CConnections(int ... addresses) {
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
	public ListenerFilter excludeI2CConnections(int... addresses) {
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

	@Override
	public <E extends Enum<E>> ListenerFilter includeStateChangeTo(E ... state) {	
		if (!startupCompleted && listener instanceof StateChangeListener) {
			includedToStates = buildMaskArray(state);
			return this;
		} else {
			if (startupCompleted) {
	    		throw new UnsupportedOperationException("ListenerFilters may only be set before startup is called.  Eg. the filters can not be changed at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of StateChangeListener in order to call this method.");
	    	}
		}
	}

	@Override
	public <E extends Enum<E>> ListenerFilter excludeStateChangeTo(E ... state) {
		if (!startupCompleted && listener instanceof StateChangeListener) {
			excludedToStates = buildMaskArray(state);
			return this;
		} else {
			if (startupCompleted) {
	    		throw new UnsupportedOperationException("ListenerFilters may only be set before startup is called.  Eg. the filters can not be changed at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of StateChangeListener in order to call this method.");
	    	}
		}
	}

	@Override
	public <E extends Enum<E>> ListenerFilter includeStateChangeFrom(E ... state) {
		if (!startupCompleted && listener instanceof StateChangeListener) {
			includedFromStates = buildMaskArray(state);
			return this;
		} else {
			if (startupCompleted) {
	    		throw new UnsupportedOperationException("ListenerFilters may only be set before startup is called.  Eg. the filters can not be changed at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of StateChangeListener in order to call this method.");
	    	}
		}
	}

	@Override
	public <E extends Enum<E>> ListenerFilter excludeStateChangeFrom(E ... state) {
		if (!startupCompleted && listener instanceof StateChangeListener) {
			excludedFromStates = buildMaskArray(state);
			return this;
		} else {
			if (startupCompleted) {
	    		throw new UnsupportedOperationException("ListenerFilters may only be set before startup is called.  Eg. the filters can not be changed at runtime.");
	    	} else {
	    		throw new UnsupportedOperationException("The Listener must be an instance of StateChangeListener in order to call this method.");
	    	}
		}
	} 
	
	private <E extends Enum<E>> long[] buildMaskArray(E[] state) {
		int maxOrdinal = findMaxOrdinal(state);
		int a = maxOrdinal >> 6;
		int b = maxOrdinal & 0x3F;		
		int longsCount = a+(b==0?0:1);
		
		long[] array = new long[longsCount];
				
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
