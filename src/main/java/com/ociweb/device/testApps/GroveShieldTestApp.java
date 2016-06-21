package com.ociweb.device.testApps;

import java.util.concurrent.TimeUnit;

import com.ociweb.iot.grove.device.lcdrgb.LCDRGBBacklightAPI;
import com.ociweb.iot.grove.device.lcdrgb.LCDRGBBacklightSchema;
import com.ociweb.iot.grove.device.lcdrgb.LCDRGBContentAPI;
import com.ociweb.iot.hardware.GroveShieldV2EdisonImpl;
import com.ociweb.iot.hardware.GroveShieldV2MockImpl;
import com.ociweb.iot.hardware.HardConnection;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.pronghorn.iot.ReadDeviceInputStage;
import com.ociweb.pronghorn.iot.i2c.I2CSimulatedLineMonitorStage;
import com.ociweb.pronghorn.iot.i2c.PureJavaI2CStage;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CBusSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;

public class GroveShieldTestApp {
    
    private static final PipeConfig<GroveResponseSchema> responseConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 30, 0);
    private static final PipeConfig<GroveRequestSchema> requestConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, 30, 0);
    
    public static boolean CONSOLE_DEBUG = false;
    
    
    private static final PipeConfig<I2CCommandSchema> requestI2CConfig = new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, 32, 128);
    private static final PipeConfig<LCDRGBBacklightSchema> backlightPipeConfig = new PipeConfig<LCDRGBBacklightSchema>(LCDRGBBacklightSchema.instance, 100);

    private static LCDRGBBacklightAPI backlightAPI;
    private static LCDRGBContentAPI contentAPI;

    //TODO: Need an easy way to build this up, perhaps a fluent API.        
    public static final Hardware config = getConfig();
    
    private static final PipeConfig<I2CBusSchema> busconfig = new PipeConfig<I2CBusSchema>(I2CBusSchema.instance, 100000);    
    public static Pipe<I2CBusSchema> i2cBusPipe = new Pipe<I2CBusSchema>(busconfig);
    static {
        if (CONSOLE_DEBUG) {
            
            ((GroveShieldV2MockImpl)config).addOptionalI2CBusSimulationPipe(i2cBusPipe);
        }
    }
    
    
    public static Hardware getConfig() {
        
        if (CONSOLE_DEBUG) {
          //Fake configuration to mock behavior of hardware.
            return new GroveShieldV2MockImpl().useI2C();
        } else {
            return new GroveShieldV2EdisonImpl().useI2C();
        }
        
    }
    
    public static void main(String[] args) {
        
        

    //    int testAddr = 0x9F;
   // /    
     //   while (--testAddr>10) {
        
          //  System.out.println("testing "+Integer.toHexString(testAddr));
            
           // Grove_LCD_RGB.LCD_ADDRESS = testAddr;
        
            GraphManager gm = new GraphManager();
                     
            try {
                config.coldSetup(); 

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("this is not on an Edison");
                return;
            }
            
            PronghornStage stageToWatch = buildGraph(gm, config);
            
            if (CONSOLE_DEBUG) {
                new I2CSimulatedLineMonitorStage(gm, i2cBusPipe);
            }
            
            //TODO: need to finish ColorMinusScheduler found in same package in Pronghorn as this scheduler
            //      then for edison use only 1 or 2 threads for doing all the work.
            

       //     MonitorConsoleStage.attach(gm);

            ThreadPerStageScheduler scheduler = new ThreadPerStageScheduler(gm);
           // scheduler.playNice = false;
            scheduler.startup();
//
            if (null!=backlightAPI) {

                //TODO: call backs are done by lambdas of Java 8.
                //      there should be no reason for developer to use while or sleep !!!

                backlightAPI.blockingSetRGB(0xFF, 0x00, 0x00);
                backlightAPI.blockingSetRGB(0x00, 0xFF, 0x00);
                backlightAPI.blockingSetRGB(0x00, 0x00, 0xFF);

                contentAPI.blockingSetText("hello world");

//                try {
//                    Thread.sleep(60_000);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }

            GraphManager.blockUntilStageBeginsShutdown(gm, stageToWatch);                



//            //redundant request for shutdown because we know its already in the progress of shutting down.
  //          scheduler.shutdown();
    
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
            //must wait until all stages are done using the configuration
            config.cleanup();
        
      //  }
                        
    }

    protected static PronghornStage buildGraph(GraphManager gm, final Hardware config) {
        
        Pipe<GroveResponseSchema> responsePipe = new Pipe<GroveResponseSchema>(GroveShieldTestApp.responseConfig);
        
        ReadDeviceInputStage groveStage = new ReadDeviceInputStage(gm, responsePipe, config);       
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 10*1000*1000, groveStage); //poll every 10 ms
         
   //     ConsoleSummaryStage<GroveResponseSchema> dump = new ConsoleSummaryStage<>(gm, responsePipe);
        ConsoleJSONDumpStage<GroveResponseSchema>  dump = new ConsoleJSONDumpStage<>(gm, responsePipe);
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 500*1000*1000, dump);
        
        ///////
        //i2c testing
        
 //       if (config.configI2C) {
        
            System.out.println("startup i2c ... ");
            
            Pipe<LCDRGBBacklightSchema> backlightApiPipe = new Pipe<LCDRGBBacklightSchema>(backlightPipeConfig);
            
            Pipe<I2CCommandSchema> i2cToBusPipeForRGB = new Pipe<I2CCommandSchema>(requestI2CConfig);
            Pipe<I2CCommandSchema> i2cToBusPipeForLCD = new Pipe<I2CCommandSchema>(requestI2CConfig);


            Pipe[] requests = new Pipe[]{i2cToBusPipeForRGB, i2cToBusPipeForLCD};
            Pipe[] response = new Pipe[0];

            I2CCommandStage comStage = new I2CCommandStage(gm,i2cToBusPipeForRGB); //TODO: old test code delete and the class soon.

          //  LCDRGBContentAPI contentStage = new LCDRGBContentAPI(gm, i2cToBusPipeForLCD);
         //   LCDRGBBacklightAPI backlightStage = new LCDRGBBacklightAPI(gm, i2cToBusPipeForRGB);

          //  backlightAPI = backlightStage;
          //  contentAPI = contentStage;

            return new PureJavaI2CStage(gm, requests, response, config);
                    
//        }+
//        return dump;
    }
    
    
}
