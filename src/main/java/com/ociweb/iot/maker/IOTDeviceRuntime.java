package com.ociweb.iot.maker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.device.Grove_LCD_RGB;
import com.ociweb.iot.hardware.GrovePiImpl;
import com.ociweb.iot.hardware.GroveShieldV2EdisonImpl;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.pronghorn.iot.ReadDeviceInputStage;
import com.ociweb.pronghorn.iot.SendDeviceOutputStage;
import com.ociweb.pronghorn.iot.i2c.I2CStage;
import com.ociweb.pronghorn.iot.i2c.PureJavaI2CStage;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.route.SplitterStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ByteArrayProducerStage;

public class IOTDeviceRuntime {

    /*
     * Caution: in order to make good use of ProGuard we need to make an effort to avoid using static so 
     * dependencies can be traced and kept in the jar.
     *  
     */
    private static final Logger logger = LoggerFactory.getLogger(IOTDeviceRuntime.class);
    
    protected static Hardware config;
    
    private static GraphManager gm = new GraphManager();
    private List<Pipe<GroveRequestSchema>> collectedRequestPipes = new ArrayList<Pipe<GroveRequestSchema>>();
    private List<Pipe<GroveResponseSchema>> collectedResponsePipes = new ArrayList<Pipe<GroveResponseSchema>>();
    
    
    private PipeConfig<GroveRequestSchema> requestPipeConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, 100,2000);
  
    
    private PipeConfig<GroveResponseSchema> responsePipeConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 100);
    private PipeConfig<GroveResponseSchema> responsePipeConfig2x = responsePipeConfig.grow2x();
    
    private int SLEEP_RATE_NS = 20_000_000; //we will only check for new work 50 times per second to keep CPU usage low.
    
    
    public IOTDeviceRuntime() {
        
        
    }

    
    
    
    public Hardware getHarware() {
        if (null==config) {

            String osversion  =System.getProperty("os.version");
 
            boolean isEdison = ( osversion.toLowerCase().indexOf("edison") != -1 );
            boolean isPi     = ( osversion.toLowerCase().indexOf("raspbian") != -1);
            
            if (!isEdison && !isPi) {
                logger.error("Unable to detect hardware : {}",osversion);
                System.exit(0);
            }
            
            if (isEdison) {
                config = new GroveShieldV2EdisonImpl();
            } else if (isPi) {
                config = new GrovePiImpl(gm);
            }
            
        }
        
        return config;
    }
    
    
    public RequestAdapter requestAdapterInstance() {
               
        Pipe<GroveRequestSchema> pipe = new Pipe<GroveRequestSchema>(requestPipeConfig );
        collectedRequestPipes.add(pipe);
        return new RequestAdapter(pipe);//TODO: do I need to make this an unschedlued stage? or can we make graph support this??
        
    }
    

    
    public void addRESTSignature(int i, String string) {

        // TODO accumulate all thse rest processor, when start is called then configure the server to take them all. 
        
    }
    
    public void addRotaryListener(RotaryListener listener) {
        
        Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig2x);
        collectedResponsePipes.add(pipe);
        
        ReactiveListenerStage stage = new ReactiveListenerStage(gm, listener, pipe);
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, SLEEP_RATE_NS,stage);
    }
    
    public void addAnalogListener(AnalogListener listener) {
       
        Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig2x);
        collectedResponsePipes.add(pipe);
        
        ReactiveListenerStage stage = new ReactiveListenerStage(gm, listener, pipe);
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, SLEEP_RATE_NS,stage);
    }
    
    public void addDigitalListener(DigitalListener listener) {
        
        Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig2x);
        collectedResponsePipes.add(pipe);
        
        ReactiveListenerStage stage = new ReactiveListenerStage(gm, listener, pipe);
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, SLEEP_RATE_NS,stage);
    }
    
    public void registerListener(Object listener) {
        
        Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig.grow2x());
        collectedResponsePipes.add(pipe);
        ReactiveListenerStage stage;
        
        if (listener instanceof RestListener) {
            
            Pipe restPipe = null; //this holds rest requests detected from the webserver, these can come directly from router.
            
            //create new pipe
            
            //add pipe to collection for starting the server ??
            
            
            stage = new ReactiveListenerStage(gm, listener, pipe, restPipe);
            
        } else {
        
        
            stage = new ReactiveListenerStage(gm, listener, pipe);
        }
        
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, SLEEP_RATE_NS,stage);
    }

    public void start() {
       config.coldSetup(); 
        
       buildGraph(); 
        
        //NOTE: need to consider different schedulers in the future.
       ThreadPerStageScheduler scheduler = new ThreadPerStageScheduler(gm);
       scheduler.startup();
       
       Runtime.getRuntime().addShutdownHook(new Thread() {
           public void run() {
             scheduler.shutdown();
             scheduler.awaitTermination(60, TimeUnit.MINUTES);
           }
       });
       
       
    }

    private void buildGraph() {

        //all the request pipes are passed into this single stage for modification of the hardware
        int s = collectedRequestPipes.size();   
        if (s>0) {
            GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, SLEEP_RATE_NS,
                    new SendDeviceOutputStage(gm, collectedRequestPipes.toArray(new Pipe[s]), config)
            );
        }
        //all the registered listers are managed here.
        s = collectedResponsePipes.size();           
        Pipe<GroveResponseSchema> responsePipe = new Pipe<GroveResponseSchema>(responsePipeConfig);
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, SLEEP_RATE_NS,
                new SplitterStage<>(gm, responsePipe, collectedResponsePipes.toArray(new Pipe[s]))
                );
        
      
        //Do not modfy the sleep of this object it is decided inernally by the config and devices plugged in.
        new ReadDeviceInputStage(gm,responsePipe,config);
     
        
        ///////////////////
        //////////////////
        
        
//        if (config.configI2C) {
//           config.beginPinConfiguration();
//           
//           
//           
//           config.configurePinsForI2C();
//           config.endPinConfiguration();
//           config.i2cClockOut();
//           config.i2cDataOut();
//           boolean temp = false;
//           int i;
//           i = 1100;
//           while (--i>=0) { //force hot spot to optimize this call
//               temp |= config.i2cReadClockBool();
//               config.i2cSetClockHigh();//done last
//           }
//           pause();
//           i = 1100;
//           while (--i>=0) { //force hot spot to optimize this call
//               temp |= config.i2cReadAck();
//               temp |= config.i2cReadDataBool();
//               config.i2cSetDataHigh();//done last
//           }
//           if (!temp) {
//               throw new UnsupportedOperationException();
//           }
//           pause();
//           
//           
//           
//        } else {
//            System.out.println("warning, i2s stage used but not turned on");
//        }
//        //starting in the known state where both are high
//
//          if (0==config.i2cReadClock() ) {
//              throw new RuntimeException("expected clock to be high for start");
//          }        
//          if (0==config.i2cReadData() ) {
//              throw new RuntimeException("expected data to be high for start");
//          }
        
        /////////////////////////
        /////////////////////////
        //////////
        //hack test for I2C
        config.beginPinConfiguration();
        config.configurePinsForI2C();
        config.endPinConfiguration();
        
//        
        PipeConfig<I2CCommandSchema> requestI2CConfig = new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, 32, 128);
        
        Pipe<I2CCommandSchema> i2cToBusPipe = new Pipe<I2CCommandSchema>(requestI2CConfig);

        //Text.
//        byte[] rawData = Grove_LCD_RGB.commandForText("GrovePi+ with\nPronghorn IoT <3");

        //Random color.
//        Random rand = new Random();
//        byte[] rawData = Grove_LCD_RGB.commandForColor(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));

        //Purple.
//        byte[] rawData = Grove_LCD_RGB.commandForColor(120, 25, 75);

        //Random string list.
        String[] randomStrings = {
                "Edison with\nPronghorn IoT",
                "Hello,\nPronghorn!",
                "Embedded\nZulu Java",
                "Hello,\nEdison!",
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
     //   I2CCommandStage cmd = new I2CCommandStage(gm, i2cToBusPipe);
        
      //  I2CStage i2cStage = new I2CStage(gm, i2cToBusPipe);
        PureJavaI2CStage i2cStage = new PureJavaI2CStage(gm, new Pipe[]{i2cToBusPipe},config);
        
        //TODO: we can build a command line stage to use FSYS linux (but should not be trusted they do not work so well??)
        // http://www.i-programmer.info/programming/hardware/9124-exploring-edison-i2c-bus.html?start=2 
        
        
    }



    
    
}
