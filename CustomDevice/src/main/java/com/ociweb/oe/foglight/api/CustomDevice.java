package com.ociweb.oe.foglight.api;


import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class CustomDevice implements FogApp
{
    ///////////////////////
    //Connection constants 
    ///////////////////////


    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////

        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.registerListener((StartupListener) runtime::shutdownRuntime);
    }
          
}
