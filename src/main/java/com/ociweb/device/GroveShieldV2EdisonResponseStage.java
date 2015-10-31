package com.ociweb.device;

import java.util.Arrays;

import com.ociweb.device.impl.EdisonGPIO;
import com.ociweb.device.impl.EdisonPinManager;
import com.ociweb.device.impl.Util;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GroveShieldV2EdisonResponseStage extends PronghornStage {
        
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
    private GroveTwig[] scriptTwig;
    private int[]       scriptLastPublished;
    private int[]       rotaryRolling;
    private int[]       rotationState;
    private long[]       rotationLastCycle;
    
    //for devices that must poll frequently
    private int[]       frequentScriptConn;
    private GroveTwig[] frequentScriptTwig;
    private int[]       frequentScriptLastPublished;
    private int         frequentScriptLength = 0;

    private long        cycles = 0;
    
    private static final short DO_NOTHING     = 0;
    private static final short DO_BIT_READ    = 1;
    private static final short DO_INT_READ    = 2;
    private static final short DO_GC          = 3;
    
    
    
    private static final short BITS_DO_PORT    = 12;
    private static final short SIZE_DO_PORT    = 1<<BITS_DO_PORT;
    private static final short MASK_DO_PORT    = SIZE_DO_PORT-1;
    private static final short SHIFT_DO_PORT   = 0x0; //LOWEST
    
    private static final short BITS_DO_JOB    = 4;
    private static final short SIZE_DO_JOB    = 1<<BITS_DO_JOB;
    private static final short MASK_DO_JOB    = SIZE_DO_JOB-1;
    private static final short SHIFT_DO_JOB   = BITS_DO_PORT;//ABOVE THE PIN
        
    
    
    private static byte[] rotaryMap = new byte[255];
    static {
        
        rotaryMap[0b01001011] = -1;
        
        rotaryMap[0b11001011] = -1;
        rotaryMap[0b11011011] = -1;
        rotaryMap[0b11010011] = -1;
        rotaryMap[0b10111011] = -1;//fast spin check
        
        rotaryMap[0b10000111] = 1;
        
        rotaryMap[0b11000111] = 1;
        rotaryMap[0b11100111] = 1;
        rotaryMap[0b11100011] = 1;
        rotaryMap[0b01110111] = 1;//fast spin check
        rotaryMap[0b01000111] = 1;//fast spin check

    }
    
    private final Pipe<GroveResponseSchema> responsePipe;    
    private final GroveShieldV2EdisonStageConfiguration config;
    
    public GroveShieldV2EdisonResponseStage(GraphManager graphManager, Pipe<GroveResponseSchema> resposnePipe, GroveShieldV2EdisonStageConfiguration config) {
        super(graphManager, NONE, resposnePipe);
        
        this.responsePipe = resposnePipe;
        this.config = config;
        GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, 10*1000*1000, this);
        GraphManager.addNota(graphManager, GraphManager.PRODUCER, GraphManager.PRODUCER, this);        
    }


    @Override
    public void startup() {
                              
        scriptConn = new int[activeSize];
        scriptTask = new int[activeSize];
        scriptTwig = new GroveTwig[activeSize];    
        scriptLastPublished = new int[activeSize];
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
        
        EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(10);
        EdisonGPIO.gpioOutputEnablePins.setValueHigh(10);
        EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(11);
        EdisonGPIO.gpioOutputEnablePins.setValueHigh(11);
        EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(12);
        EdisonGPIO.gpioOutputEnablePins.setValueHigh(12);
        EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(13);
        EdisonGPIO.gpioOutputEnablePins.setValueHigh(13);        
        
        
        byte sliceCount = 0;
        
        //configure each sensor
        
        synchronized(EdisonGPIO.shieldControl) {
            EdisonGPIO.shieldControl.setDirectionLow(0);
                        
            int i;
            
            i = config.analogInputs.length;
            while (--i>=0) {
                EdisonGPIO.configAnalogInput(config.analogInputs[i].connection);  //readInt
                            
                int idx = Util.reverseBits(sliceCount++);
                scriptConn[idx]=config.analogInputs[i].connection;
                scriptTask[idx]=DO_INT_READ;
                scriptTwig[idx] = config.analogInputs[i].twig;
                System.out.println("configured "+config.analogInputs[i].twig+" on connection "+config.analogInputs[i].connection);
  
            }
            
            i = config.digitalInputs.length;
            while (--i>=0) {
                EdisonGPIO.configDigitalInput(config.digitalInputs[i].connection); //readBit
                GroveTwig twig = config.digitalInputs[i].twig;
                
                if (twig == GroveTwig.Button) {                    
                    frequentScriptConn[frequentScriptLength] = config.digitalInputs[i].connection;
                    frequentScriptTwig[frequentScriptLength] = twig;                           
                    frequentScriptLength++; 
                } else {                               
                    int idx = Util.reverseBits(sliceCount++);
                    scriptConn[idx]=config.digitalInputs[i].connection;
                    scriptTask[idx]=DO_BIT_READ;
                    scriptTwig[idx] = twig;                    
                }
                System.out.println("configured "+twig+" on connection "+config.digitalInputs[i].connection);
                                
            }
            
            i = config.encoderInputs.length;
            while (--i>=0) {
                EdisonGPIO.configDigitalInput(config.encoderInputs[i].connection); //rotary 
                if (0!=(i&0x1)) {
                    if ((config.encoderInputs[i].connection!=(1+config.encoderInputs[i-1].connection)) ) {
                        throw new UnsupportedOperationException("Rotery encoder requires two neighboring digital inputs.");                    
                    }      
                    frequentScriptConn[frequentScriptLength] = config.encoderInputs[i].connection-1;
                    frequentScriptTwig[frequentScriptLength] = config.encoderInputs[i].twig;
                    frequentScriptLength++; 
                    System.out.println("configured "+config.encoderInputs[i].twig+" on connection "+config.encoderInputs[i].connection);
                    
                }
            }                      
            
            EdisonGPIO.shieldControl.setDirectionHigh(0);
        }

        if (sliceCount>=16) {
            throw new UnsupportedOperationException("The grove base board does not support this many connections.");
        }
        
        //not a great feature, takes up too much CPU.
        if (false && sliceCount<10) {
            int idx = Util.reverseBits(sliceCount++);
            scriptTask[idx]=DO_GC;
        }
        
    }

    @Override
    public void run() {
        cycles++; 
        
        //These are the sensors we must check on every single pass
        int j = frequentScriptLength;
        while (--j>=0) {
                        
            int connector = frequentScriptConn[j];
            switch (frequentScriptTwig[j]) {
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
            case DO_GC:
                System.gc();
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
            int r1  = EdisonPinManager.readBit(connector, EdisonGPIO.gpioLinuxPins); //10
            int r2  = EdisonPinManager.readBit(connector+1, EdisonGPIO.gpioLinuxPins); //10
            
            rotaryPoll = (byte)((r1<<1)|r2);
            
            if (doesNotMatchLastPollValue(rotaryPoll, rotaryRolling[j])) {
                rotaryRolling[j] = (rotaryRolling[j]<<2) | rotaryPoll; 
                
                byte value = rotaryMap[0xFF & rotaryRolling[j]];
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
            frequentScriptTwig[j].writeRotation(responsePipe, connector, rotationState[j], rotationState[j]-frequentScriptLastPublished[j], speed);
                       
            frequentScriptLastPublished[j] = rotationState[j];
            rotationLastCycle[j] = cycles;
        }
    }


    private void readButton(int j, int connector) {
        //read and xmit
        int fieldValue = EdisonPinManager.readBit(connector, EdisonGPIO.gpioLinuxPins);
        if (frequentScriptLastPublished[j]!=fieldValue &&Pipe.hasRoomForWrite(responsePipe)) {                        
            frequentScriptTwig[j].writeBit(responsePipe, connector, fieldValue);
            frequentScriptLastPublished[j]=fieldValue;
        } else {
            //tossing fieldValue but this could be saved to send on next call.
        }
    }


    private void intRead(final short doit) {
        {
             int connector = scriptConn[doit];
             int intValue = EdisonPinManager.readInt(connector, EdisonGPIO.gpioLinuxPins);
             if (isWithinNoise(doit, intValue) && Pipe.hasRoomForWrite(responsePipe)) {
                 
                 scriptTwig[doit].writeInt(responsePipe, connector, intValue);
                 scriptLastPublished[doit]=intValue;
                 
             } else {
                 //tossing fieldValue but this could be saved to send on next call.
             }
        }
    }

   //if change is smaller than 1/64 then do not show.
    private boolean isWithinNoise(final short doit, int intValue) {
        int lastValue = scriptLastPublished[doit];
        return Math.abs(lastValue-intValue) >= (Math.min(lastValue,intValue)>>6);
    }


    private void bitRead(final short doit) {
        {
              int connector = scriptConn[doit];
              int fieldValue = EdisonPinManager.readBit(connector, EdisonGPIO.gpioLinuxPins);
              if (isWithinNoise(doit, fieldValue) &&Pipe.hasRoomForWrite(responsePipe)) {
                  
                  scriptTwig[doit].writeBit(responsePipe, connector, fieldValue);
                  scriptLastPublished[doit]=fieldValue;
                          
              } else {
                  //tossing fieldValue but this could be saved to send on next call.
              }
        }
    }

    private static final boolean doesNotMatchLastPollValue(byte rotaryPoll, int rotaryRolling) {
        return rotaryPoll != (0x3 & rotaryRolling);
    }
    
}
