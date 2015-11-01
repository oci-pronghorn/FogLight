package com.ociweb.device.testApps;

import static com.ociweb.device.grove.GroveTwig.*;

import java.util.concurrent.TimeUnit;

import com.ociweb.device.config.GroveShieldV2EdisonConfiguration;
import com.ociweb.device.grove.GroveConnect;
import com.ociweb.device.grove.GroveShieldV2ResponseStage;
import com.ociweb.device.grove.GroveTwig;
import com.ociweb.device.grove.schema.GroveRequestSchema;
import com.ociweb.device.grove.schema.GroveResponseSchema;
import com.ociweb.device.impl.EdisonGPIO;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;

public class GroveShieldTestApp {


    //TODO: refactor as Request/Response NOT input output
    private static final PipeConfig<GroveResponseSchema> responseConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 30, 0);
 //   private static final PipeConfig<GroveRequestSchema> requestConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, 30, 0);
    
    //TODO: Need an easy way to build this up, perhaps a fluent API.        
    private static final GroveShieldV2EdisonConfiguration config = new GroveShieldV2EdisonConfiguration(
            true, //publish time 
            false,  //turn on I2C
            new GroveConnect[] {new GroveConnect(RotaryEncoder,2),new GroveConnect(RotaryEncoder,3)}, //rotary encoder 
            new GroveConnect[] {new GroveConnect(Button,0) ,new GroveConnect(MotionSensor,8)}, //7 should be avoided it can disrupt WiFi, button and motion 
            new GroveConnect[] {}, //for requests like do the buzzer on 4
            new GroveConnect[]{},  //for PWM requests //(only 3, 5, 6, 9, 10, 11) //3 here is D3
            new GroveConnect[] {new GroveConnect(MoistureSensor,1), //1 here is A1
                           new GroveConnect(LightSensor,2) 
                        //   new GroveConnect(UVSensor,3)
                          }); //for analog sensors A0, A1, A2, A3
    
    
    
    public static void main(String[] args) {
        
        GraphManager gm = new GraphManager();
                    
        GroveConnect[] usedLines = config.buildUsedLines(); 
        
        EdisonGPIO.ensureAllLinuxDevices(usedLines); 
                        
        buildGraph(gm, config);
        
        
        //TODO: need to finish ColorMinusScheduler found in same package in Pronghorn as this scheduler
        //      then for edison use only 1 or 2 threads for doing all the work.
        ThreadPerStageScheduler scheduler = new ThreadPerStageScheduler(gm);
        
        scheduler.startup();
                
        try {
            Thread.sleep(60000*60*24); //shuts off server if you leave it running for a full day.
        } catch (InterruptedException e) {
           throw new RuntimeException(e);
        }
        
        scheduler.shutdown();

        EdisonGPIO.removeAllLinuxDevices(usedLines);

        scheduler.awaitTermination(5, TimeUnit.SECONDS);
                        
    }

    protected static void buildGraph(GraphManager gm, final GroveShieldV2EdisonConfiguration config) {
        
        Pipe<GroveResponseSchema> responsePipe = new Pipe<GroveResponseSchema>(GroveShieldTestApp.responseConfig);
        
        GroveShieldV2ResponseStage groveStage = new GroveShieldV2ResponseStage(gm, responsePipe, config);       
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 10*1000*1000, groveStage); //poll every 10 ms
        
        ConsoleJSONDumpStage<GroveResponseSchema>  dump = new ConsoleJSONDumpStage<>(gm, responsePipe);
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 100*1000*1000, dump);
        
    }
    
    
}
