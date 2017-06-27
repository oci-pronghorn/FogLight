/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;


import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.iot.grove.LCD_RGB.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.util.Appendables;

public class IoTBehavior implements AnalogListener{
    
    private final FogCommandChannel channel;
    
    private final int fullTank = 25;
    
    public IoTBehavior(FogRuntime runtime) {
        
        channel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
        
    }
    
    @Override
    public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
        if (value>fullTank) {
            System.out.println("Check equipment, tank is deeper than expected");
        } else {
            int remainingDepth = fullTank-value;
            
            StringBuilder builder = new StringBuilder();
            Appendables.appendFixedDecimalDigits(builder, remainingDepth, 100);
            
            builder.append("cm \n");
            builder.append("depth");
            
            Grove_LCD_RGB.commandForColor(channel, 200, 200, 180);
            Grove_LCD_RGB.commandForText(channel, builder);
            
            
        }
        
        
        
    }
    
}
