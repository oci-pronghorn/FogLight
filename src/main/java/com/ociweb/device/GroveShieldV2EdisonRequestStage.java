package com.ociweb.device;

import com.ociweb.device.impl.EdisonGPIO;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GroveShieldV2EdisonRequestStage extends PronghornStage {

    public GroveShieldV2EdisonRequestStage(GraphManager gm, Pipe<GroveRequestSchema> requestPipe) {
        super(gm, requestPipe, NONE);
    }
    
        
    
    @Override
    public void startup() {

        
        
    }
    
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void shutdown() {
        
        
    }
    
    
//  i = config.pwmOutputs.length;
//  while (--i>=0) {
//      configPWM(config.pwmOutputs[i]); //take from pipe and write, get type and field from pipe
//      
//      script[reverseBits(sliceCount++)] = ((MASK_DO_PORT&config.pwmOutputs[i])<<SHIFT_DO_PORT) |
//                                          ((MASK_DO_JOB&DO_DATA_WRITE)<<SHIFT_DO_JOB );
//  }
    
    private void configPWM(int dPort) {
        if (dPort<3 || 4==dPort || 7==dPort || 8==dPort || dPort>11) {
            //(only 3, 5, 6, 9, 10, 11)
            throw new UnsupportedOperationException("PWM only available on 3, 5, 6, 9, 10 or 11");
        }
        
        EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(dPort);
        EdisonGPIO.gpioPullupEnablePins.setDirectionIn(dPort);
        EdisonGPIO.gpioPinModes.setDebugCurrentPinmuxMode1(dPort);
        EdisonGPIO.gpioPinMuxExt.setDirectionLow(dPort);
        
    }


    
    private void configDigitalOutput(int dPort) {       
        EdisonGPIO.gpioPinModes.setDebugCurrentPinmuxMode0(dPort);        
        //no need to map since ports happen to match the digital pins
        EdisonGPIO.gpioPullupEnablePins.setDirectionHigh(dPort); 
        EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(dPort);
        EdisonGPIO.gpioLinuxPins.setDirectionOut(dPort);     
        
        
    }
    
    
}
