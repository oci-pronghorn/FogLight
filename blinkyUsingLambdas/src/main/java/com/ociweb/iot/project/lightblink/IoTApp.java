package com.ociweb.iot.project.lightblink;

import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.PubSubListener;

public class IoTApp implements IoTSetup {
    
    static final int LED_CONNECTION = 5;
    private static final String TOPIC = "light";
    private static final int PAUSE = 500;
           
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware c) {
        c.useConnectD(LED, LED_CONNECTION);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel blinkerChannel = runtime.newCommandChannel(); 
        final PubSubListener blinker = runtime.addPubSubListener(
                (topic,payload)->{           
                    int value = payload.readInt();
                    blinkerChannel.digitalSetValueAndBlock(LED_CONNECTION, value, PAUSE);               
                    blinkerChannel.openTopic(TOPIC).writeInt( 1==value ? 0 : 1 ).publish();
                    
                }); 
                
        final CommandChannel startupChannel = runtime.newCommandChannel(); 
        runtime.addStartupListener(
                ()->{
                	System.out.println("startup started");
                    startupChannel.subscribe(TOPIC, blinker);
                    startupChannel.openTopic(TOPIC).writeInt( 1 ).publish();
                });        
    }  
}
