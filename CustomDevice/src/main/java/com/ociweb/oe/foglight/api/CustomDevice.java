package com.ociweb.oe.foglight.api;


import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.pronghorn.pipe.ChannelReader;

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
        runtime.registerListener(new PubSubListener() {
            FogCommandChannel ch = runtime.newCommandChannel();

            @Override
            public boolean message(CharSequence charSequence, ChannelReader channelReader) {
                return true;
            }
        });
    }
          
}
