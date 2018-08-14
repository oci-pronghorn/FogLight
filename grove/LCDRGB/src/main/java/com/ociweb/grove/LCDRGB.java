package com.ociweb.grove;


import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class LCDRGB implements FogApp
{
   


    @Override
    public void declareConnections(Hardware c) {
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
       runtime.registerListener(new LCDRGB_Behavior(runtime));
    }
          
}
