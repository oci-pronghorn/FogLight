package com.ociweb.device.testApps;

import java.util.concurrent.TimeUnit;

import com.ociweb.device.Connect;
import com.ociweb.device.GroveRequestSchema;
import com.ociweb.device.GroveResponseSchema;
import com.ociweb.device.GroveShieldV2EdisonResponseStage;
import com.ociweb.device.GroveShieldV2EdisonStageConfiguration;
import com.ociweb.device.GroveTwig;
import com.ociweb.device.impl.EdisonGPIO;

import static com.ociweb.device.GroveTwig.*;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;

public class GroveShieldTestApp {


    //TODO: refactor as Request/Response NOT input output
    private static final PipeConfig<GroveResponseSchema> responseConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 30, 10);
    private static final PipeConfig<GroveRequestSchema> requestConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, 30, 10);
    
    //TODO: Need an easy way to build this up, perhaps a fluent API.        
    private static final GroveShieldV2EdisonStageConfiguration config = new GroveShieldV2EdisonStageConfiguration(
            false, //publish time 
            false,  //turn on I2C
            new Connect[] {new Connect(RotaryEncoder,2),new Connect(RotaryEncoder,3)}, //rotary encoder 
            new Connect[] {new Connect(Button,0) ,new Connect(MotionSensor,8)}, //7 should be avoided it can disrupt WiFi, button and motion 
            new Connect[] {}, //for requests like do the buzzer on 4
            new Connect[]{},  //for PWM requests //(only 3, 5, 6, 9, 10, 11)
            new Connect[] {new Connect(MoistureSensor,1), 
                           new Connect(LightSensor,2), 
                           new Connect(UVSensor,3)
                          }); //for analog sensors A0, A1, A2, A3
    
    
    
    public static void main(String[] args) {
        
        GraphManager gm = new GraphManager();
                    
        Connect[] usedLines = GroveShieldV2EdisonStageConfiguration.buildUsedLines(config); 
        
        EdisonGPIO.ensureAllLinuxDevices(usedLines); 
                        
        buildGraph(gm, config);
        
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

    protected static void buildGraph(GraphManager gm, final GroveShieldV2EdisonStageConfiguration config) {
        
        Pipe<GroveResponseSchema> responsePipe = new Pipe<GroveResponseSchema>(GroveShieldTestApp.responseConfig);
        
        GroveShieldV2EdisonResponseStage groveStage = new GroveShieldV2EdisonResponseStage(gm, responsePipe, config);       
        
        ConsoleJSONDumpStage<GroveResponseSchema>  dump = new ConsoleJSONDumpStage<>(gm, responsePipe);
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 100*1000*1000, dump);
        
    }
    
    
}
