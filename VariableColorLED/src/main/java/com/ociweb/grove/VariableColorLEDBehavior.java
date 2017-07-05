/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.D3;

/**
 *
 * @author huydo
 */
public class VariableColorLEDBehavior implements TimeListener {
    
    private static final Port LED_PORT = D3;
    private int lightIntensity = 0;
    
    private final FogCommandChannel ledChannel;
    
    public VariableColorLEDBehavior(FogRuntime runtime){
        this.ledChannel = runtime.newCommandChannel();
    }
    
    @Override
    public void timeEvent(long time, int i) {
        lightIntensity = (int) (127* Math.sin(time/(Math.PI * 500)) + 127);
        System.out.println(lightIntensity);
        ledChannel.setValue(LED_PORT, lightIntensity);
    }
    
}
