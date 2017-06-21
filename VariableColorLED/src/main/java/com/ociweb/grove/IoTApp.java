package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp {
           
	private static final Port LED_PORT = D3;
        private int lightIntensity = 0;
        private boolean brighter = true;
        
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.setTriggerRate(50);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        final FogCommandChannel ledChannel = runtime.newCommandChannel(DYNAMIC_MESSAGING);
           
        runtime.addTimeListener((time,instance)->{
            if (lightIntensity == 0 || brighter){                
                lightIntensity += 1;
                ledChannel.setValue(LED_PORT, lightIntensity);
                if(lightIntensity == LED.range()-1){
                    brighter = false;
                }
                System.out.println("going up "+lightIntensity);
            }else{
                lightIntensity -= 1;
                ledChannel.setValue(LED_PORT, lightIntensity);
                if(lightIntensity == 0){
                    brighter = true;
                }
                System.out.println("going down "+lightIntensity);
            }
            System.out.println(127* Math.sin(time/6.14) + 127);
            });            
    }
}

