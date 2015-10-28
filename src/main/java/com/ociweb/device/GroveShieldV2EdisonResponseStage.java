package com.ociweb.device;

import java.util.Arrays;

import com.ociweb.device.impl.EdisonConstants;
import com.ociweb.device.impl.EdisonGPIO;
import com.ociweb.device.impl.EdisonPinManager;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GroveShieldV2EdisonResponseStage extends PronghornStage {
        
    private static final short activeBits = 4; //we have a max of 16 physical ports to use on the groveShield
    private static final short activeSize = (short)(1<<activeBits);
    private static final short activeIdxMask = (short)activeSize-1;
    private short activeIdx;
    private int runningRotaryTotal;
    
    //script defines which port must be read or write on each cycle
    //when the rotary encoder is used it is checked on every cycle
    
    //  4   read type  (bit or integer or rotary or ...
    // 12   read/write pin
    // other id?
    private final int[]       scriptJob = new int[activeSize];
    private final GroveTwig[] scriptTwig = new GroveTwig[activeSize];
    private final int[]       scriptLastPublished = new int[activeSize];
    private final int[]       rotaryRolling = new int[activeSize];
    private final int[]       rotationState = new int[activeSize];
    private final int[]       rotationStateChangeSum = new int[activeSize];

    
    private static final short DO_NOTHING     = 0;
    private static final short DO_BIT_READ    = 1;
    private static final short DO_INT_READ    = 2;
    private static final short DO_ROTARY_READ = 3;
    
    
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
        
    private Connect[] usedLines; 
    
    private final GroveShieldV2EdisonStageConfiguration config;
    
    public GroveShieldV2EdisonResponseStage(GraphManager graphManager, Pipe<GroveResponseSchema> resposnePipe, GroveShieldV2EdisonStageConfiguration config) {
        super(graphManager, NONE, resposnePipe);
        Arrays.fill(rotaryRolling, 0xFFFFFFFF);
        this.responsePipe = resposnePipe;
        this.config = config;
        GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, 200000000, this);
        GraphManager.addNota(graphManager, GraphManager.PRODUCER, GraphManager.PRODUCER, this);
        
    }


    //reverse the low 4 bits and drop the high 4 bits
    //this allows for an even distribution of values filling each spot once
    public byte reverseBits(byte bits) {
        //could use Integer.reverse but we only have 4 bits...
        return (byte) ( (0x08&(bits<<3))|
                        (0x04&(bits<<1))|
                        (0x02&(bits>>1))|
                        (0x01&(bits>>3))  );
        
    }
    //TODO: change from port to connection
    
    @Override
    public void startup() {

        usedLines = GroveShieldV2EdisonStageConfiguration.buildUsedLines(config);
        
        ensureAllLinuxDevices(usedLines);       
        
        byte sliceCount = 0;
        
        //configure each sensor
        
        synchronized(EdisonGPIO.shieldControl) {
            EdisonGPIO.shieldControl.setDirectionLow(0);
                        
            int i;
            
            i = config.analogInputs.length;
            while (--i>=0) {
                configAnalogInput(config.analogInputs[i].connection);  //readInt
                            
                int idx = reverseBits(sliceCount++);
                scriptJob[idx] = ((MASK_DO_PORT&config.analogInputs[i].connection)<<SHIFT_DO_PORT) |
                                                    ((MASK_DO_JOB&DO_INT_READ)<<SHIFT_DO_JOB );
                scriptTwig[idx] = config.analogInputs[i].twig;
                
  
            }
            
            i = config.digitalInputs.length;
            while (--i>=0) {
                configDigitalInput(config.digitalInputs[i].connection); //readBit
                
                int idx = reverseBits(sliceCount++);
                scriptJob[idx] = ((MASK_DO_PORT&config.digitalInputs[i].connection)<<SHIFT_DO_PORT) |
                                                    ((MASK_DO_JOB&DO_BIT_READ)<<SHIFT_DO_JOB );
                scriptTwig[idx] = config.digitalInputs[i].twig;
                
            }
            
            i = config.encoderInputs.length;
            while (--i>=0) {
                configDigitalInput(config.encoderInputs[i].connection); //rotary 
                if (0!=(i&0x1)) {
                    if ((config.encoderInputs[i].connection!=(1+config.encoderInputs[i-1].connection)) ) {
                        throw new UnsupportedOperationException("Rotery encoder requires two neighboring digital inputs.");                    
                    }      
                                    
                    int idx = reverseBits(sliceCount++);
                    scriptJob[idx] = ((MASK_DO_PORT&config.encoderInputs[i].connection)<<SHIFT_DO_PORT) |
                                                        ((MASK_DO_JOB&DO_ROTARY_READ)<<SHIFT_DO_JOB );
                    scriptTwig[idx] = config.encoderInputs[i].twig;
                    
                }
            }            
                      
            
            EdisonGPIO.shieldControl.setDirectionHigh(0);
        }

        if (sliceCount>=16) {
            throw new UnsupportedOperationException("The grove base board does not support this many connections.");
        }
        
    }

    private void ensureAllLinuxDevices(Connect[] usedLines) {
        
        EdisonGPIO.shieldControl.ensureDevice(0); //tri statebyte
        EdisonGPIO.shieldControl.ensureDevice(1); //shield reset

        int j = usedLines.length;
        while (--j>=0) {                
            int i = usedLines[j].connection;
            EdisonGPIO.gpioLinuxPins.ensureDevice(i);
            EdisonGPIO.gpioOutputEnablePins.ensureDevice(i);
            EdisonGPIO.gpioPullupEnablePins.ensureDevice(i);   
            EdisonGPIO.gpioPinMux.ensureDevice(i);
            EdisonGPIO.gpioPinMuxExt.ensureDevice(i);
            EdisonGPIO.gpioPinModes.ensureDevice(i);
            
        }
    }
    
    private void configDigitalInput(int dPort) {       
        EdisonGPIO.gpioOutputEnablePins.setDirectionLow(dPort);

        //no need to map since ports happen to match the digital pins
        EdisonGPIO.gpioPullupEnablePins.setDirectionHigh(dPort);
        EdisonGPIO.gpioLinuxPins.setDirectionIn(dPort);      
    }
    
    
    public void configAnalogInput(int aPort) {
        if (aPort<0 || aPort>5) {
            throw new UnsupportedOperationException("only available on 0, 1, 2, or 3 and only 4 or 5 if I2C is not in use.");
        }
        EdisonGPIO.gpioPinMux.setDirectionHigh(EdisonConstants.ANALOG_CONNECTOR_TO_PIN[aPort]);        
        EdisonGPIO.gpioOutputEnablePins.setDirectionLow(EdisonConstants.ANALOG_CONNECTOR_TO_PIN[aPort]);
        EdisonGPIO.gpioPullupEnablePins.setDirectionIn(EdisonConstants.ANALOG_CONNECTOR_TO_PIN[aPort]);  //in       
    }
        
    
    
    @Override
    public void run() {
                
        final short doit =  (short)(++activeIdx&activeIdxMask);
        byte rotaryPoll=3;
        int maxCycles = 80; //what if stuck in middle must detect.
        do {
             //TODO: how do we know we have these two on the same clock?
             int r1  = EdisonPinManager.readBit(2, EdisonGPIO.gpioLinuxPins); //10
             int r2  = EdisonPinManager.readBit(3, EdisonGPIO.gpioLinuxPins); //10
             
             rotaryPoll = (byte)((r1<<1)|r2);
             
             if (doesNotMatchLastPollValue(rotaryPoll, rotaryRolling[doit])) {
                 rotaryRolling[doit] = (rotaryRolling[doit]<<2) | rotaryPoll; 
                                  
                 byte value = rotaryMap[0xFF & rotaryRolling[doit]];
                 rotationState[doit] = rotationState[doit]+value; 
                 
                 rotationStateChangeSum[doit] += (value|(value>>7));                 
                 
                 
                 
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
         
         
        
        int temp = scriptJob[doit];
        
        short connector = (short)((temp>>SHIFT_DO_PORT)&MASK_DO_PORT);
        short job = (short)((temp>>SHIFT_DO_JOB)&MASK_DO_JOB);
        
        int   msgId = 0;//TODO: missing all the types!
        
        
        switch(job) {
            case DO_NOTHING:
                        //System.gc(); //an idea
                break;
            case DO_BIT_READ:
                {
                      //read and xmit
                      int fieldValue = (EdisonPinManager.readBit(connector, EdisonGPIO.gpioLinuxPins)<<31) | connector;
                      if (scriptLastPublished[doit]!=fieldValue &&Pipe.hasRoomForWrite(responsePipe)) {
                          
                          scriptTwig[doit].writeBit(responsePipe, connector, fieldValue);
                          scriptLastPublished[doit]=fieldValue;
                                  
                      } else {
                          //tossing fieldValue but this could be saved to send on next call.
                      }
                }
                break;
            case DO_INT_READ:
                        //read and xmit
                         int intValue = EdisonPinManager.readInt(connector, EdisonGPIO.gpioLinuxPins);
                         if (scriptLastPublished[doit]!=intValue && Pipe.hasRoomForWrite(responsePipe)) {
                             
                             scriptTwig[doit].writeInt(responsePipe, connector, intValue);
                             scriptLastPublished[doit]=intValue;
                             
                         } else {
                             //tossing fieldValue but this could be saved to send on next call.
                         }
                
                break;
            case DO_ROTARY_READ:    
                        if (scriptLastPublished[doit]!=rotationState[doit] && Pipe.hasRoomForWrite(responsePipe)) {
                                    
                            scriptTwig[doit].writeRotation(responsePipe, connector, rotationState[doit], rotationState[doit]-scriptLastPublished[doit], rotationStateChangeSum[doit]);
                                       
                            scriptLastPublished[doit] = rotationState[doit];
                        }
                        rotationStateChangeSum[doit] = 0;//reset for next sum
                        //no need to save else since we will return the current state accumulated
                break;

            default:
                throw new UnsupportedOperationException(Integer.toString(job));
        }
        

        
        
        
        
    }

    private static final boolean doesNotMatchLastPollValue(byte rotaryPoll, int rotaryRolling) {
        return rotaryPoll != (0x3 & rotaryRolling);
    }
    
    
    @Override
    public void shutdown() {
        removeAllLinuxDevices(usedLines);   
    }

    private void removeAllLinuxDevices(Connect[] usedLines) {
        EdisonGPIO.shieldControl.removeDevice(0); //tri state
        EdisonGPIO.shieldControl.removeDevice(1); //shield reset

        //NOTE: this is overkill to create every single device we may possibly need
        //      TODO: use some flats to reduce this set to only the ones we are using
        
        int j = usedLines.length;
        while (--j>=0) {                
            int i = usedLines[j].connection;       
            EdisonGPIO.gpioLinuxPins.removeDevice(i);
            EdisonGPIO.gpioOutputEnablePins.removeDevice(i);
            EdisonGPIO.gpioPullupEnablePins.removeDevice(i); 
            EdisonGPIO.gpioPinMux.removeDevice(i);
            EdisonGPIO.gpioPinMuxExt.removeDevice(i);
            EdisonGPIO.gpioPinModes.removeDevice(i);
        }
    }
    
}
