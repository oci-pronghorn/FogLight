package com.ociweb.iot.maker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.GrovePiImpl;
import com.ociweb.iot.hardware.GroveShieldV2EdisonImpl;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.schema.GroveRequestSchema;
import com.ociweb.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.ReadDeviceInputStage;
import com.ociweb.pronghorn.iot.SendDeviceOutputStage;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.route.SplitterStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;

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
    
    protected IOTDeviceRuntime() {
        
        
    }

    
    protected static Hardware getHarware() {
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
    
    protected void addRotaryListener(RotaryListener listener) {
        
        Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig2x);
        collectedResponsePipes.add(pipe);
        
        ReactiveListenerStage stage = new ReactiveListenerStage(gm, listener, pipe);

    }
    
    protected void addAnalogListener(AnalogListener listener) {
       
        Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig2x);
        collectedResponsePipes.add(pipe);
        
        ReactiveListenerStage stage = new ReactiveListenerStage(gm, listener, pipe);

    }
    
    protected void addDigitalListener(DigitalListener listener) {
        
        Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig2x);
        collectedResponsePipes.add(pipe);
        
        ReactiveListenerStage stage = new ReactiveListenerStage(gm, listener, pipe);

    }
    
    protected void registerListener(Object listener) {
        
        Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig.grow2x());
        collectedResponsePipes.add(pipe);
        
        if (listener instanceof RestListener) {
            
            Pipe restPipe = null; //this holds rest requests detected from the webserver, these can come directly from router.
            
            //create new pipe
            
            //add pipe to collection for starting the server ??
            
            
            ReactiveListenerStage stage = new ReactiveListenerStage(gm, listener, pipe, restPipe);
            
        } else {
        
        
            ReactiveListenerStage stage = new ReactiveListenerStage(gm, listener, pipe);
        }
    }

    protected void start() {
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
            new SendDeviceOutputStage(gm, collectedRequestPipes.toArray(new Pipe[s]), config);
        }
        //all the registered listers are managed here.
        s = collectedResponsePipes.size();           
        Pipe<GroveResponseSchema> responsePipe = new Pipe<GroveResponseSchema>(responsePipeConfig);
        new SplitterStage<>(gm, responsePipe, collectedResponsePipes.toArray(new Pipe[s]));
        new ReadDeviceInputStage(gm,responsePipe,config);
        
    }





    
    
}
