package com.ociweb.pronghorn.iot;

import java.util.Arrays;

import com.ociweb.iot.grove.GroveTwig;
import com.ociweb.iot.hardware.HardConnection;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.hardware.impl.Util;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class ReadDeviceInputStage extends PronghornStage {

	private static final short activeBits = 4; //we have a max of 16 physical ports to use on the groveShield
	private static final short activeSize = (short)(1<<activeBits);
	private static final short activeIdxMask = (short)activeSize-1;
	private short activeIdx;

	//script defines which port must be read or write on each cycle
	//when the rotary encoder is used it is checked on every cycle

	//  4   read type  (bit or integer or rotary or ...
	// 12   read/write pin
	// other id?
	private int[]       scriptConn;
	private int[]       scriptTask; 
	private IODevice[] scriptTwig;

	private int[][]    movingAverageHistory;
	private int[]      lastPublished;

	private int[]       rotaryRolling;
	private int[]       rotationState;
	private long[]      rotationLastCycle;

	//for devices that must poll frequently
	private int[]       frequentScriptConn;
	private IODevice[] frequentScriptTwig;
	private int[]       frequentScriptLastPublished;
	private int         frequentScriptLength = 0;

	private long        cycles = 0;

	private static final short DO_NOTHING     = 0;
	private static final short DO_BIT_READ    = 1;
	private static final short DO_INT_READ    = 2;    

	private final Pipe<GroveResponseSchema> responsePipe;    
	final Hardware config;

	public ReadDeviceInputStage(GraphManager graphManager, Pipe<GroveResponseSchema> resposnePipe, Hardware config) {
		super(graphManager, NONE, resposnePipe);

		this.responsePipe = resposnePipe;
		this.config = config;

		//NOTE: rotary encoder would work best using native hardware and this rate only need
		//      be faster than 20K because the rotary and the button needs to be polled.
		//TODO: with a user space GPIO Sysfs or similar driver see of we can remove this work, will save most of the CPU.
		GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, 1_000_000, this);
		GraphManager.addNota(graphManager, GraphManager.PRODUCER, GraphManager.PRODUCER, this);        
	}


	@Override
	public void startup() {
		//polling thread must be of the highest priority
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		scriptConn = new int[activeSize];
		scriptTask = new int[activeSize];
		scriptTwig = new GroveTwig[activeSize];  

		int j = config.maxAnalogMovingAverage()-1;
		movingAverageHistory = new int[j][]; 
		while (--j>=0) {
			movingAverageHistory[j] = new int[activeSize];            
		}
		lastPublished = new int[activeSize];

		rotaryRolling = new int[activeSize];
		Arrays.fill(rotaryRolling, 0xFFFFFFFF);
		rotationState = new int[activeSize];
		rotationLastCycle = new long[activeSize];

		//for devices that must poll frequently
		frequentScriptConn = new int[activeSize];
		frequentScriptTwig = new GroveTwig[activeSize];
		frequentScriptLastPublished = new int[activeSize];

		//before we setup the pins they must start in a known state
		//this is required for the ATD converters (eg any analog port usage)


		byte sliceCount = 0;

		//configure each sensor

		config.beginPinConfiguration();

		int i;

		i = config.analogInputs.length;
		while (--i>=0) {
			//config.configurePinsForAnalogInput(config.analogInputs[i].connection);
				int idx = Util.reverseBits(sliceCount++);
				scriptConn[idx] = config.analogInputs[i].connection;

				scriptTask[idx] = DO_INT_READ;
				scriptTwig[idx] = config.analogInputs[i].twig;
				System.out.println("configured "+config.analogInputs[i].twig+" on connection "+config.analogInputs[i].connection);
			
		}

		i = config.digitalInputs.length;
		while (--i>=0) {
			//config.configurePinsForDigitalInput(config.digitalInputs[i].connection);
			
				IODevice twig = config.digitalInputs[i].twig;

				if (twig == GroveTwig.Button) {                    
					frequentScriptConn[frequentScriptLength] = config.digitalInputs[i].connection;
					frequentScriptTwig[frequentScriptLength] = twig;                           
					frequentScriptLength++; 
				} else {                               
					int idx = Util.reverseBits(sliceCount++);
					scriptConn[idx] = config.digitalInputs[i].connection;

					scriptTask[idx] = DO_BIT_READ;
					scriptTwig[idx] = twig;                    
				}
				System.out.println("configured "+twig+" on connection "+config.digitalInputs[i].connection);
			         
		}

		i = config.multiBitInputs.length;
		while (--i>=0) {
			//config.configurePinsForDigitalInput(config.multiBitInputs[i].connection);
				if (0!=(i&0x1)) {
					if ((config.multiBitInputs[i].connection!=(1+config.multiBitInputs[i-1].connection)) ) {
						throw new UnsupportedOperationException("Rotery encoder requires two neighboring digital inputs.");                    
					}      
					frequentScriptConn[frequentScriptLength] = config.multiBitInputs[i].connection-1;
					frequentScriptTwig[frequentScriptLength] = config.multiBitInputs[i].twig;
					frequentScriptLength++; 
					System.out.println("configured "+config.multiBitInputs[i].twig+" on connection "+(config.multiBitInputs[i].connection-1)+"/"+(config.multiBitInputs[i].connection));

				}
			
		}                      

		config.endPinConfiguration();

		if (sliceCount>=16) {
			throw new UnsupportedOperationException("The grove base board does not support this many connections.");
		}
	}


	@Override
	public void run() {
		cycles++; 

		//These are the sensors we must check on every single pass
		//TODO: should refactor so this is part of the Twig interface and we need not hard code.
		int j = frequentScriptLength;
		while (--j>=0) {

			int connector = frequentScriptConn[j];
			switch ((GroveTwig)frequentScriptTwig[j]) {
			case Button:
				readButton(j, connector);                    
				break;
			case RotaryEncoder:
				readRotaryEncoder(j, connector);                    
				break;
			default:
				throw new UnsupportedOperationException(frequentScriptTwig[j].toString());
			}                        
		} 

		//These are the sensors we can check less frequently and do a few on each pass
		final short doit =  (short)(++activeIdx&activeIdxMask);

		switch(scriptTask[doit]) {
		case DO_NOTHING:
			break;
		case DO_BIT_READ:
			bitRead(doit);
			break;
		case DO_INT_READ:
			intRead(doit);   
			break;
		default:
			throw new UnsupportedOperationException(Integer.toString(scriptTask[doit]));
		}

	}


	private void readRotaryEncoder(int j, int connector) {
		byte rotaryPoll=3;
		int maxCycles = 80; //what if stuck in middle must detect.
		do {
			//TODO: how do we know we have these two on the same clock?
			int r1  = config.digitalRead(connector); 
			int r2  = config.digitalRead(connector+1); 

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
			writeRotation(responsePipe, connector, System.currentTimeMillis(), rotationState[j], rotationState[j]-frequentScriptLastPublished[j], speed);

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


    private void readButton(int j, int connector) {
		//read and xmit
		int fieldValue = config.digitalRead(connector);
		if (frequentScriptLastPublished[j]!=fieldValue && Pipe.hasRoomForWrite(responsePipe)) {   
		    
			writeBit(responsePipe, connector, System.currentTimeMillis(), fieldValue);
			
			
			frequentScriptLastPublished[j]=fieldValue;
		} else {
			//tossing fieldValue but this could be saved to send on next call.
		}
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


    private void intRead(final short doit) {
		{
			int connector = scriptConn[doit];
			int maTotal = config.maxAnalogMovingAverage();  //TODO: update so each connection can have its own ma
			boolean useAverageAsTrigger = true;            //TODO: update so some connections send every value
			int intValue = config.analogRead(connector);

			int i = maTotal-1;
			long sum = 0;
			//StringBuilder b = new StringBuilder();
			while (--i>=0) {
				//b.append(" ").append(movingAverageHistory[i][doit]);
				sum += (long)movingAverageHistory[i][doit];
			}
			//b.append(" ").append(intValue);
			sum += (long)intValue;
			int avg = (int)Math.rint(sum/(float)maTotal);


			int sendTrigger = useAverageAsTrigger ? avg : intValue;

			if (lastPublished[doit] != sendTrigger && Pipe.hasRoomForWrite(responsePipe)) {
				writeInt(responsePipe, connector, System.currentTimeMillis(), intValue, avg);
				int j = maTotal-1;
				while (--j>0) {//must stop before zero because we do copy from previous lower index.
					movingAverageHistory[j][doit]=movingAverageHistory[j-1][doit];                     
				}
				movingAverageHistory[0][doit]=intValue;
				lastPublished[doit] = sendTrigger;

			} else {
				//tossing fieldValue but this could be saved to send on next call.
			}
		}
	}


	private void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int intValue, int average) {
	    int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_ANALOGSAMPLE_30);
        Pipe.addIntValue(connector, responsePipe);
        Pipe.addLongValue(time, responsePipe);
        Pipe.addIntValue(intValue, responsePipe);
        Pipe.addIntValue(average, responsePipe);
        
        long duration = 0;
        Pipe.addLongValue(duration, responsePipe);            
        
        Pipe.publishWrites(responsePipe);
        Pipe.confirmLowLevelWrite(responsePipe, size);
        
    }


    private void bitRead(final short doit) {
		{
			int connector = scriptConn[doit];
			int fieldValue = config.digitalRead(connector);
			if (lastPublished[doit]!=fieldValue &&Pipe.hasRoomForWrite(responsePipe)) {                  
				writeBit(responsePipe, connector, System.currentTimeMillis(), fieldValue);                  
				lastPublished[doit]=fieldValue;                          
			} else {
				//tossing fieldValue but this could be saved to send on next call.
			}
		}
	}

	private static final boolean doesNotMatchLastPollValue(byte rotaryPoll, int rotaryRolling) {
		return rotaryPoll != (0x3 & rotaryRolling);
	}

}
