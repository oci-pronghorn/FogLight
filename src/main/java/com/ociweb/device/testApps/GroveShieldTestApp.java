package com.ociweb.device.testApps;

import java.util.concurrent.TimeUnit;

import com.ociweb.device.Connect;
import com.ociweb.device.GroveRequestSchema;
import com.ociweb.device.GroveResponseSchema;
import com.ociweb.device.GroveShieldV2EdisonResponseStage;
import com.ociweb.device.GroveShieldV2EdisonStageConfiguration;
import com.ociweb.device.GroveTwig;
import static com.ociweb.device.GroveTwig.*;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;

public class GroveShieldTestApp {

    public static GroveShieldTestApp instance;

    //TODO: refactor as Request/Response NOT input output
    private static final PipeConfig<GroveResponseSchema> responseConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 10, 10);
    private static final PipeConfig<GroveRequestSchema> requestConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, 10, 10);
    
    
    public static void main(String[] args) {
        instance = new GroveShieldTestApp();
        
        GraphManager gm = new GraphManager();
        instance.buildGraph(gm);
        
        ThreadPerStageScheduler scheduler = new ThreadPerStageScheduler(gm);
        scheduler.startup();
                
        try {
            Thread.sleep(60000*60*24); //shuts off server if you leave it running for a full day.
        } catch (InterruptedException e) {
           throw new RuntimeException(e);
        }
        
        scheduler.shutdown();
        
    }

    private void buildGraph(GraphManager gm) {
        
        //TODO: Need an easy way to build this up, perhaps a fluent API.        
        final GroveShieldV2EdisonStageConfiguration config = new GroveShieldV2EdisonStageConfiguration(
                false, //publish time 
                false,  //turn on I2C
                new Connect[] {new Connect(RotaryEncoder,2),new Connect(RotaryEncoder,3)}, //rotary encoder 
                new Connect[] {new Connect(Button,0),new Connect(MotionSensor,8)}, //7 should be avoided it can disrupt WiFi, button and motion 
                new Connect[] {}, //for requests like do the buzzer on 4
                new Connect[]{},  //for PWM requests //(only 3, 5, 6, 9, 10, 11)
                new Connect[] {new Connect(MoistureSensor,1), new Connect(LightSensor,2), new Connect(UVSensor,3)}); //for analog sensors A0, A1, A2, A3
        
        
       Pipe<GroveResponseSchema> responsePipe = new Pipe<GroveResponseSchema>(responseConfig);
       
       GroveShieldV2EdisonResponseStage groveStage = new GroveShieldV2EdisonResponseStage(gm, responsePipe, config);       
       ConsoleJSONDumpStage<GroveResponseSchema>  dump = new ConsoleJSONDumpStage<>(gm, responsePipe);
       
       ThreadPerStageScheduler scheduler = new ThreadPerStageScheduler(gm);              
       scheduler.startup();

       //Everything is now running
       
       try{ 
           Thread.sleep(60000*60*24); //quit after one day.           
       } catch (InterruptedException e) {
       }
       scheduler.shutdown();
       
       scheduler.awaitTermination(5, TimeUnit.SECONDS);
       
        
    }
    
    
}
