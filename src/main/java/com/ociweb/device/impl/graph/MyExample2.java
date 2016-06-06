package com.ociweb.device.impl.graph;

import static com.ociweb.device.grove.GroveTwig.Button;
import static com.ociweb.device.grove.GroveTwig.LightSensor;
import static com.ociweb.device.grove.GroveTwig.MoistureSensor;
import static com.ociweb.device.grove.GroveTwig.MotionSensor;
import static com.ociweb.device.grove.GroveTwig.RotaryEncoder;
import static com.ociweb.device.grove.GroveTwig.UVSensor;

import com.ociweb.device.config.GroveConnectionConfiguration;
import com.ociweb.device.config.GroveShieldV2EdisonConfiguration;
import com.ociweb.device.grove.GroveConnect;

//TODO: move this class to its own maven project as an example for makers
public class MyExample2 extends IOTDeviceRuntime {

    
    protected GroveConnectionConfiguration configuration() {
        
        return new GroveShieldV2EdisonConfiguration(
                false, //publish time 
                true,  //turn on I2C
                new GroveConnect[] {new GroveConnect(RotaryEncoder,2),new GroveConnect(RotaryEncoder,3)}, //rotary encoder 
                new GroveConnect[] {new GroveConnect(Button,0) ,new GroveConnect(MotionSensor,8)}, //7 should be avoided it can disrupt WiFi, button and motion 
                new GroveConnect[] {}, //for requests like do the buzzer on 4
                new GroveConnect[]{},  //for PWM requests //(only 3, 5, 6, 9, 10, 11) //3 here is D3
                new GroveConnect[] {new GroveConnect(MoistureSensor,1), //1 here is A1
                                    new GroveConnect(LightSensor,2), 
                                    new GroveConnect(UVSensor,3)
                              }); //for analog sensors A0, A1, A2, A3
        
    }
    
    protected void init() {
        
        registerListener(new MyExample2Logic());                      
        //others can go here.
        
    }
    
    
    public class MyExample2Logic implements DigitalListener, RotaryListener {

        private final RequestAdapter requestAdapter;
        
        public MyExample2Logic() {
             requestAdapter = requestAdapterInstance();
            
        }
        
        
        @Override
        public void rotaryEvent(int connector, int value, int delta, int speed) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void digitalEvent(int equip, int connector, int value) {
            // TODO Auto-generated method stub
            
        }
        
        
    }
    
}
