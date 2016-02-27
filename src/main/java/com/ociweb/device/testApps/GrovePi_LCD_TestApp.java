package com.ociweb.device.testApps;

import java.util.concurrent.TimeUnit;

import com.ociweb.device.config.GroveConnectionConfiguration;
import com.ociweb.device.config.grovepi.GrovePiConfiguration;
import com.ociweb.device.grove.GroveConnect;
import com.ociweb.device.grove.GroveShieldV2ResponseStage;
import com.ociweb.device.grove.grovepi.GrovePiI2CStageV2;
import com.ociweb.device.grove.schema.GroveRequestSchema;
import com.ociweb.device.grove.schema.GroveResponseSchema;
import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.device.impl.Grove_LCD_RGB;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ByteArrayProducerStage;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;

/**
 * TODO:
 */
public class GrovePi_LCD_TestApp {
    private static final PipeConfig<GroveResponseSchema> responseConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 30, 0);
    private static final PipeConfig<GroveRequestSchema> requestConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, 30, 0);
    private static final PipeConfig<I2CCommandSchema> requestI2CConfig = new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, 32, 128);
        
    //TODO: Need an easy way to build this up, perhaps a fluent API.        
    public static final GroveConnectionConfiguration config = new GrovePiConfiguration(
           false, //publish time 
           true,  //turn on I2C
           new GroveConnect[] {/*new GroveConnect(RotaryEncoder,2),new GroveConnect(RotaryEncoder,3)*/}, //rotary encoder 
           new GroveConnect[] {/*new GroveConnect(Button,0) ,new GroveConnect(MotionSensor,8)*/}, //7 should be avoided it can disrupt WiFi, button and motion 
           new GroveConnect[] {}, //for requests like do the buzzer on 4
           new GroveConnect[] {}, //for PWM requests //(only 3, 5, 6, 9, 10, 11) //3 here is D3
           new GroveConnect[] {//new GroveConnect(MoistureSensor,1), //1 here is A1
                               //new GroveConnect(LightSensor,2) 
                               //new GroveConnect(UVSensor,3)
                              }); //for analog sensors A0, A1, A2, A3
    
    public static void main(String[] args) {
        
        GraphManager gm = new GraphManager();
                 
        try {
            config.coldSetup(); 
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("this is not on a Pi");
            return;
        }
        
        System.out.println("Build graph");
        
        buildGraph(gm, config);
        
        //TODO: need to finish ColorMinusScheduler found in same package in Pronghorn as this scheduler
        //      then for edison use only 1 or 2 threads for doing all the work.
        ThreadPerStageScheduler scheduler = new ThreadPerStageScheduler(gm);
        
        scheduler.startup();
                
        try {
            Thread.sleep(60000*60*24); //shuts off server if you leave it running for a full day.
        } 
        
        catch (InterruptedException e) {
           throw new RuntimeException(e);
        }
        
        scheduler.shutdown();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);
        
        //must wait until all stages are done using the configuration
        config.cleanup();                
    }

    protected static void buildGraph(GraphManager gm, final GroveConnectionConfiguration config) {
        
        Pipe<GroveResponseSchema> responsePipe = new Pipe<GroveResponseSchema>(GrovePi_LCD_TestApp.responseConfig);
        
        GroveShieldV2ResponseStage groveStage = new GroveShieldV2ResponseStage(gm, responsePipe, config);       
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 10 * 1000 * 1000, groveStage); //poll every 10 ms
         
        //ConsoleSummaryStage<GroveResponseSchema> dump = new ConsoleSummaryStage<>(gm, responsePipe);
        ConsoleJSONDumpStage<GroveResponseSchema>  dump = new ConsoleJSONDumpStage<>(gm, responsePipe);
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 500 * 1000 * 1000, dump);
        
        ///////
        //i2c testing
        if (config.configI2C) {
        
            System.out.println("startup i2c ... ");
            
            Pipe<I2CCommandSchema> i2cToBusPipe = new Pipe<I2CCommandSchema>(requestI2CConfig);
            
            int line = Grove_LCD_RGB.LCD_2LINE;
            
            //turn on Backlight
            byte[] rawData = new byte[] {
                (byte)((Grove_LCD_RGB.RGB_ADDRESS << 1)), (byte) 0, (byte) 0,
                (byte)((Grove_LCD_RGB.RGB_ADDRESS << 1)), (byte) 1, (byte) 0,
                (byte)((Grove_LCD_RGB.RGB_ADDRESS << 1)), (byte) 0x08, (byte) 0xaa,
                (byte)((Grove_LCD_RGB.RGB_ADDRESS << 1)), (byte) 4, (byte) 120,
                (byte)((Grove_LCD_RGB.RGB_ADDRESS << 1)), (byte) 3, (byte) 25,
                (byte)((Grove_LCD_RGB.RGB_ADDRESS << 1)), (byte) 2, (byte) 75
            };

            int[] chunkSizes = new int[]{3,3,3, 3,3,3};
            ByteArrayProducerStage prodStage = new ByteArrayProducerStage(gm, rawData, chunkSizes, i2cToBusPipe);
            GrovePiI2CStageV2 i2cStage = new GrovePiI2CStageV2(gm, i2cToBusPipe, config);
        }
    }
}
