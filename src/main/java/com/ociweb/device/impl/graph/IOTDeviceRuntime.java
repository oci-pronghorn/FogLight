package com.ociweb.device.impl.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.event.ListSelectionEvent;

import com.ociweb.device.config.GroveConnectionConfiguration;
import com.ociweb.device.grove.GroveShieldV2RequestStage;
import com.ociweb.device.grove.GroveShieldV2ResponseStage;
import com.ociweb.device.grove.schema.GroveRequestSchema;
import com.ociweb.device.grove.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.route.SplitterStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;

public class IOTDeviceRuntime {

    private GroveConnectionConfiguration config;
    private static GraphManager gm = new GraphManager();
    private static List<Pipe<GroveRequestSchema>> collectedRequestPipes = new ArrayList<Pipe<GroveRequestSchema>>();
    private static List<Pipe<GroveResponseSchema>> collectedResponsePipes = new ArrayList<Pipe<GroveResponseSchema>>();
    
    
    private static PipeConfig<GroveRequestSchema> requestPipeConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, 100);
    private static PipeConfig<GroveResponseSchema> responsePipeConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 100);
    
    
    protected IOTDeviceRuntime() {
        
        
        
    }
    
    public static RequestAdapter requestAdapterInstance() {
               
        Pipe<GroveRequestSchema> pipe = new Pipe<GroveRequestSchema>(requestPipeConfig );
        collectedRequestPipes.add(pipe);
        return new RequestAdapter(pipe);//TODO: do I need to make this an unschedlued stage? or can we make graph support this??
        
    }
    
    
    protected void registerListener(AnalogListener listener) {
       
        Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig.grow2x());
        collectedResponsePipes.add(pipe);
        
        ReactiveListenerStage stage = new ReactiveListenerStage(gm, listener, new Pipe[]{pipe});

    }
    
    protected void registerListener(Object listener) {
        
        Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig.grow2x());
        collectedResponsePipes.add(pipe);
        
        ReactiveListenerStage stage = new ReactiveListenerStage(gm, listener, new Pipe[]{pipe});

    }
    
    public static void main(String[] args) {
        
        IOTDeviceRuntime runtimeInstance = new IOTDeviceRuntime();
        
        runtimeInstance.config = runtimeInstance.configuration();
       
        runtimeInstance.init(); //user defined business logic 
        
        runtimeInstance.buildGraph(); //finish building the graph
        
        runtimeInstance.start();
        
    }

    protected void init() {
        // TODO user must override this
        
    }

    private void start() {
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
        new GroveShieldV2RequestStage(gm, collectedRequestPipes.toArray(new Pipe[s]), config);
        
        //all the registered listers are managed here.
        s = collectedResponsePipes.size();           
        Pipe<GroveResponseSchema> responsePipe = new Pipe<GroveResponseSchema>(responsePipeConfig);
        new SplitterStage<>(gm, responsePipe, collectedResponsePipes.toArray(new Pipe[s]));
        new GroveShieldV2ResponseStage(gm,responsePipe,config);
        
    }

    
    //TODO: review name and usage for simplicity.
    protected GroveConnectionConfiguration configuration() {
        
        return null;
        
    }

    
    
}
