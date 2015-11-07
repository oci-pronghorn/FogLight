package com.ociweb.device.testApps;

import static com.ociweb.device.grove.GroveTwig.Button;
import static com.ociweb.device.grove.GroveTwig.LightSensor;
import static com.ociweb.device.grove.GroveTwig.MoistureSensor;
import static com.ociweb.device.grove.GroveTwig.MotionSensor;
import static com.ociweb.device.grove.GroveTwig.RotaryEncoder;

import java.util.concurrent.TimeUnit;

import com.ociweb.device.config.GroveConnectionConfiguration;
import com.ociweb.device.config.GroveShieldV2EdisonConfiguration;
import com.ociweb.device.config.GroveShieldV2MockConfiguration;
import com.ociweb.device.grove.GroveConnect;
import com.ociweb.device.grove.GroveShieldV2I2CStage;
import com.ociweb.device.grove.GroveShieldV2ResponseStage;
import com.ociweb.device.grove.schema.GroveI2CSchema;
import com.ociweb.device.grove.schema.GroveRequestSchema;
import com.ociweb.device.grove.schema.GroveResponseSchema;
import com.ociweb.device.impl.Grove_LCD_RGB;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ByteArrayProducerStage;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;
import com.ociweb.pronghorn.stage.test.ConsoleSummaryStage;

public class GroveShieldTestApp {
    
    private static final PipeConfig<GroveResponseSchema> responseConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 30, 0);
    private static final PipeConfig<GroveRequestSchema> requestConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, 30, 0);
    
    private static final PipeConfig<GroveI2CSchema> requestI2CConfig = new PipeConfig<GroveI2CSchema>(GroveI2CSchema.instance, 32, 128);
        
    //TODO: Need an easy way to build this up, perhaps a fluent API.        
    public static final GroveConnectionConfiguration config = new GroveShieldV2EdisonConfiguration(
            false, //publish time 
            true,  //turn on I2C
            new GroveConnect[] {new GroveConnect(RotaryEncoder,2),new GroveConnect(RotaryEncoder,3)}, //rotary encoder 
            new GroveConnect[] {new GroveConnect(Button,0) ,new GroveConnect(MotionSensor,8)}, //7 should be avoided it can disrupt WiFi, button and motion 
            new GroveConnect[] {}, //for requests like do the buzzer on 4
            new GroveConnect[]{},  //for PWM requests //(only 3, 5, 6, 9, 10, 11) //3 here is D3
            new GroveConnect[] {new GroveConnect(MoistureSensor,1), //1 here is A1
                                new GroveConnect(LightSensor,2) 
                        //   new GroveConnect(UVSensor,3)
                          }); //for analog sensors A0, A1, A2, A3
    
    
//    //Fake configuration to mock behavior of hardware.
//    public static final GroveConnectionConfiguration config = new GroveShieldV2MockConfiguration(
//            false, //publish time 
//            true,  //turn on I2C
//            new GroveConnect[] {new GroveConnect(RotaryEncoder,2),new GroveConnect(RotaryEncoder,3)}, //rotary encoder 
//            new GroveConnect[] {new GroveConnect(Button,0) ,new GroveConnect(MotionSensor,8)}, //7 should be avoided it can disrupt WiFi, button and motion 
//            new GroveConnect[] {}, //for requests like do the buzzer on 4
//            new GroveConnect[]{},  //for PWM requests //(only 3, 5, 6, 9, 10, 11) //3 here is D3
//            new GroveConnect[] {new GroveConnect(MoistureSensor,1), //1 here is A1
//                           new GroveConnect(LightSensor,2) 
//                        //   new GroveConnect(UVSensor,3)
//                          }); //for analog sensors A0, A1, A2, A3
    
    
    public static void main(String[] args) {
        
        GraphManager gm = new GraphManager();
                    
        config.coldSetup(); 
                        
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


        scheduler.awaitTermination(5, TimeUnit.SECONDS);
        //must wait until all stages are done using the configuration
        config.cleanup();
                        
    }

    protected static void buildGraph(GraphManager gm, final GroveConnectionConfiguration config) {
        
        Pipe<GroveResponseSchema> responsePipe = new Pipe<GroveResponseSchema>(GroveShieldTestApp.responseConfig);
        
        GroveShieldV2ResponseStage groveStage = new GroveShieldV2ResponseStage(gm, responsePipe, config);       
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 10*1000*1000, groveStage); //poll every 10 ms
         
   //     ConsoleSummaryStage<GroveResponseSchema> dump = new ConsoleSummaryStage<>(gm, responsePipe);
        ConsoleJSONDumpStage<GroveResponseSchema>  dump = new ConsoleJSONDumpStage<>(gm, responsePipe);
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 500*1000*1000, dump);
        
        ///////
        //i2c testing
        
        if (config.configI2C) {
        
            Pipe<GroveI2CSchema> i2cToBusPipe = new Pipe<GroveI2CSchema>(requestI2CConfig);
            
            //turn on Backlight
            byte[] rawData = new byte[]{Grove_LCD_RGB.RGB_ADDRESS, 0, 0,
                                        Grove_LCD_RGB.RGB_ADDRESS, 1, 0, 
                                        Grove_LCD_RGB.RGB_ADDRESS, Grove_LCD_RGB.REG_OUTPUT, (byte)0xAA, 
                                        Grove_LCD_RGB.RGB_ADDRESS, Grove_LCD_RGB.REG_RED,    (byte)0XFF, 
                                        Grove_LCD_RGB.RGB_ADDRESS, Grove_LCD_RGB.REG_GREEN,  (byte)0XFF, 
                                        Grove_LCD_RGB.RGB_ADDRESS, Grove_LCD_RGB.REG_BLUE,   (byte)0XFF   
                                        };
            int[] chunkSizes = new int[]{3,3,3,3,3,3};
            ByteArrayProducerStage prodStage = new ByteArrayProducerStage(gm, rawData, chunkSizes, i2cToBusPipe);
    
            GroveShieldV2I2CStage i2cStage = new GroveShieldV2I2CStage(gm,i2cToBusPipe,config);
        
        }
    }
    
    
}
