package com.ociweb.pronghorn.iot;

import java.util.Arrays;

import com.ociweb.iot.grove.AnalogDigitalTwig;
import com.ociweb.iot.grove.I2CGroveTwig;
import com.ociweb.iot.hardware.HardwareConnection;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.hardware.impl.Util;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.math.PMath;
import com.ociweb.pronghorn.util.math.ScriptedSchedule;

public class ReadDeviceInputStage extends PronghornStage {

	private static final short activeBits = 4; //we have a max of 16 physical ports to use on the groveShield
	private static final short activeSize = (short)(1<<activeBits);
	private static final short activeIdxMask = (short)activeSize-1;
	private short activeIdx;

	//script defines which port must be read or write on each cycle
	//when the rotary encoder is used it is checked on every cycle


	private int[][]    movingAverageHistory;

	private int[]       rotaryRolling;
	private int[]       rotationState;
	private long[]      rotationLastCycle;

	//for devices that must poll frequently
	private int[]       frequentScriptConn;
	private IODevice[] frequentScriptTwig;
	private int[]       frequentScriptLastPublished;
	private int         frequentScriptLength = 0;

	private long        cycles = 0;
	
	private int inProgressIdx = 0;
	private int scheduleIdx = 0;
   
	protected static final long MS_TO_NS = 1_000_000;

	private final Pipe<GroveResponseSchema> responsePipe;    
	final HardwareImpl hardware;
	private final ScriptedSchedule schedule;
	private HardwareConnection[] adConnections;

	private long blockStartTime = 0;
	private Number rate;
	
	public ReadDeviceInputStage(GraphManager graphManager, Pipe<GroveResponseSchema> resposnePipe, HardwareImpl hardware) {
		super(graphManager, NONE, resposnePipe);

		this.responsePipe = resposnePipe;
		this.hardware = hardware;

		this.adConnections = hardware.combinedADConnections();
		this.schedule = hardware.buildADPollSchedule();
		
		System.out.println("Direct schedule: "+this.schedule);
		
		assert(null!=schedule) : "should not have been called, there are no inputs configured";

		if (null != this.schedule) {
			assert(0==(this.schedule.commonClock%10)) : "must be divisible by 10";
			GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, (this.schedule.commonClock)/10L , this); 
		}
		
		GraphManager.addNota(graphManager, GraphManager.PRODUCER, GraphManager.PRODUCER, this);   
				
		rate = (Number)graphManager.getNota(graphManager, this.stageId,  GraphManager.SCHEDULE_RATE, null);
	}


	@Override
	public void startup() {
		//polling thread must be of the highest priority
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		int j = hardware.maxAnalogMovingAverage()-1;
		movingAverageHistory = new int[j][]; 
		while (--j>=0) {
			movingAverageHistory[j] = new int[activeSize];            
		}
		
		rotaryRolling = new int[activeSize];
		Arrays.fill(rotaryRolling, 0xFFFFFFFF);
		rotationState = new int[activeSize];
		rotationLastCycle = new long[activeSize];

		//for devices that must poll frequently
		frequentScriptConn = new int[activeSize];
		
		//TODO: what to do with this? Used to be GroveTwig[] 
		frequentScriptTwig = new IODevice[activeSize];
		frequentScriptLastPublished = new int[activeSize];

		//before we setup the pins they must start in a known state
		//this is required for the ATD converters (eg any analog port usage)

		
	//TODO: ADD Schedule of the selected inputs and their speeds.	
	//	int[] schedulePeriods = new int[inputs.length];
		

		byte sliceCount = 0;

		//configure each sensor

		//hardware.beginPinConfiguration();

		int i;

		i = hardware.getDigitalInputs().length;
		while (--i>=0) {
			//config.configurePinsForDigitalInput(config.digitalInputs[i].connection);
			
				IODevice twig = hardware.getDigitalInputs()[i].twig;

				if (twig == AnalogDigitalTwig.RotaryEncoder) {
					frequentScriptConn[frequentScriptLength] = hardware.getDigitalInputs()[i].register; //just the low address
					frequentScriptTwig[frequentScriptLength] = twig;                           
					frequentScriptLength++; 
				} else if (twig == AnalogDigitalTwig.Button) {                    
					frequentScriptConn[frequentScriptLength] = hardware.getDigitalInputs()[i].register;
					frequentScriptTwig[frequentScriptLength] = twig;                           
					frequentScriptLength++; 
				} else {                               
					int idx = Util.reverseBits(sliceCount++);                   
				}
				System.out.println("configured "+twig+" on connection "+hardware.getDigitalInputs()[i].register);
			         
		}                   

		//hardware.endPinConfiguration(); //TODO: questionalble, should move else where.

		blockStartTime = hardware.nanoTime();//critical Pronghorn contract ensure this start is called by the same thread as run
	}


	@Override
	public void run() {
		
		do{
		    long waitTime = blockStartTime - hardware.nanoTime();
     		if(waitTime>0){
     			if (null==rate || (waitTime > 2*rate.longValue())) {				
     				return; //Enough time has not elapsed to start next block on schedule
     			} else {
     				while (hardware.nanoTime()<blockStartTime){
     					Thread.yield();
     					if (Thread.interrupted()) {
     						requestShutdown();
     						return;
     					}
     				}    				
     			}
     		}
     		
     		
			inProgressIdx = schedule.script[scheduleIdx];			
			if(inProgressIdx != -1) {
				
				if (!Pipe.hasRoomForWrite(responsePipe)) {
					return;//try again later, no room on output pipe.
				}

				HardwareConnection hc = adConnections[inProgressIdx];
				int connector = hc.register;
				
				
				if (AnalogDigitalTwig.RotaryEncoder == hc.twig) {
					assert (hc.twig.pinsUsed()==2);
					//rotary encoder
					//low level write
					readRotaryEncoder(connector, Port.DIGITALS[connector], hardware.currentTimeMillis()); //TODO: hack for now, needs more testing.
				} else if (1==hc.twig.range()) {
					//digital read
					int fieldValue = hardware.read(Port.DIGITALS[connector]);
					//low level write
					writeBit(responsePipe, connector, hardware.currentTimeMillis(), fieldValue);
										
				} else {
					//analog read
					int i = hc.twig.pinsUsed();
					while (--i>=0) {
						//int intValue = hardware.read(Port.DIGITALS[connector]); //was this way before
						int intValue = hardware.read(Port.ANALOGS[connector+i]);
   				    
						//low level write
						writeInt(responsePipe, connector+i, hardware.currentTimeMillis(), intValue);
					}
				}
								
			}
			//since we exit early if the pipe is full we must not move this forward until now at the bottom of the loop.
			scheduleIdx = (scheduleIdx+1) % schedule.script.length;
		}while(inProgressIdx != -1);
		blockStartTime += schedule.commonClock;
		
	}


	private void readRotaryEncoder(int j, Port port, long timeMS) {
		byte rotaryPoll=3;
		int maxCycles = 80; //what if stuck in middle must detect.
		do {
			//TODO: how do we know we have these two on the same clock?
			int r1  = hardware.read(port); 
			int r2  = hardware.read(Port.DIGITALS[port.port+1]); 

			rotaryPoll = (byte)((r1<<1)|r2);

			if (doesNotMatchLastPollValue(rotaryPoll, rotaryRolling[j])) {
				rotaryRolling[j] = (rotaryRolling[j]<<2) | rotaryPoll; 

				byte value = Util.rotaryMap[0xFF & rotaryRolling[j]];
				rotationState[j] = rotationState[j]+value;

				//debug                 
				//                 if (rotaryPoll==3 && 0==rotaryMap[0xFF&rotaryRolling]) {
				//                     
				//                     System.out.println("  "+Integer.toBinaryString(0xFF&rotaryRolling));                     
				//                     
				//                 }

			}
		} while ((rotaryPoll!=0x3 ) && --maxCycles>=0); //TODO: keep going until we get 111111 ??


		if (0==maxCycles) {
			System.err.println("check rotary encoder, may be stuck between states.");
		}

		if (frequentScriptLastPublished[j]!=rotationState[j] && Pipe.hasRoomForWrite(responsePipe)) {
			int speed = (int)Math.min( (cycles - rotationLastCycle[j]), Integer.MAX_VALUE);
			writeRotation(responsePipe, port.port, hardware.currentTimeMillis(), rotationState[j], rotationState[j]-frequentScriptLastPublished[j], speed);

			frequentScriptLastPublished[j] = rotationState[j];
			rotationLastCycle[j] = cycles;
		}
	}


	private void writeRotation(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int value, int delta, int speed) {
        int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_ENCODER_70);
        Pipe.addIntValue(connector, responsePipe);
        Pipe.addLongValue(time, responsePipe);
        Pipe.addIntValue(value, responsePipe);
        Pipe.addIntValue(delta, responsePipe);            
        Pipe.addIntValue(speed, responsePipe);
        
        long duration = 0;
        Pipe.addLongValue(duration, responsePipe);        
        
        Pipe.publishWrites(responsePipe);
        Pipe.confirmLowLevelWrite(responsePipe, size);
    }


   	

	private void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int bitValue) {
	    
	    int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_DIGITALSAMPLE_20);
        Pipe.addIntValue(connector, responsePipe);
        Pipe.addLongValue(time, responsePipe);
        Pipe.addIntValue(bitValue, responsePipe);
        
        long duration = 0;
        Pipe.addLongValue(duration, responsePipe);        
        
        Pipe.publishWrites(responsePipe);
        Pipe.confirmLowLevelWrite(responsePipe, size);
    }


 

	private void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int intValue) {
	    int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_ANALOGSAMPLE_30);
        Pipe.addIntValue(connector, responsePipe);
        Pipe.addLongValue(time, responsePipe);
        Pipe.addIntValue(intValue, responsePipe);
        
        long duration = 0;
        Pipe.addLongValue(duration, responsePipe);            
        
        Pipe.publishWrites(responsePipe);
        Pipe.confirmLowLevelWrite(responsePipe, size);
        
    }


	private static final boolean doesNotMatchLastPollValue(byte rotaryPoll, int rotaryRolling) {
		return rotaryPoll != (0x3 & rotaryRolling);
	}

}
