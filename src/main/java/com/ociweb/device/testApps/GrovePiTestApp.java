package com.ociweb.device.testApps;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.ociweb.device.config.GroveConnectionConfiguration;
import com.ociweb.device.config.grovepi.GrovePiConfiguration;
import com.ociweb.device.grove.GroveConnect;
import com.ociweb.device.grove.GroveShieldV2ResponseStage;
import com.ociweb.device.grove.schema.GroveResponseSchema;
import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.device.impl.Grove_LCD_RGB;
import com.ociweb.pronghorn.iot.i2c.I2CStage;
import com.ociweb.pronghorn.iot.i2c.impl.I2CStageGroveJavaBacking;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ByteArrayProducerStage;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO:
 *
 * @author Nathan Tippy
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class GrovePiTestApp {
    private static final Logger logger = LoggerFactory.getLogger(GrovePiTestApp.class);

    private static final PipeConfig<GroveResponseSchema> responseConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 30, 0);
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
        logger.info("GrovePi RGB LCD via I2C Example Starting.");
                 
        try {
            config.coldSetup(); 
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Initialization failed. This is not a Raspberry Pi.");
            return;
        }
        
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
        
        Pipe<GroveResponseSchema> responsePipe = new Pipe<GroveResponseSchema>(GrovePiTestApp.responseConfig);
        
        GroveShieldV2ResponseStage groveStage = new GroveShieldV2ResponseStage(gm, responsePipe, config);       
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 10 * 1000 * 1000, groveStage); //poll every 10 ms

        ConsoleJSONDumpStage<GroveResponseSchema>  dump = new ConsoleJSONDumpStage<>(gm, responsePipe);
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 500 * 1000 * 1000, dump);

        if (config.configI2C) {

            Pipe<I2CCommandSchema> i2cToBusPipe = new Pipe<I2CCommandSchema>(requestI2CConfig);

            //Text.
//            byte[] rawData = Grove_LCD_RGB.commandForText("GrovePi+ with\nPronghorn IoT <3");

            //Random color.
//            Random rand = new Random();
//            byte[] rawData = Grove_LCD_RGB.commandForColor(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));

            //Purple.
//            byte[] rawData = Grove_LCD_RGB.commandForColor(120, 25, 75);

            //Random string list.
            String[] randomStrings = {
                    "GrovePi+ with\nPronghorn IoT",
                    "Hello,\nPronghorn!",
                    "Embedded\nZulu Java",
                    "Hello,\nGrovePi+!",
                    "I'm sorry Dave,\nI can't do that."
            };

            //Build command.
            Random rand = new Random();
            byte[] rawData = Grove_LCD_RGB.commandForTextAndColor(randomStrings[rand.nextInt(randomStrings.length)],
                                                                  rand.nextInt(256),
                                                                  rand.nextInt(256),
                                                                  rand.nextInt(256));

            //Calculate chunk sizes; for now, we assume every chunk is 3 bytes long.
            int[] chunkSizes = new int[rawData.length / 3];
            for (int i = 0; i < chunkSizes.length; i++) chunkSizes[i] = 3;

            //Pipe that data.
            ByteArrayProducerStage prodStage = new ByteArrayProducerStage(gm, rawData, chunkSizes, i2cToBusPipe);
            I2CStage i2cStage = new I2CStage(gm, i2cToBusPipe, new I2CStageGroveJavaBacking(config));
        }
    }
}
