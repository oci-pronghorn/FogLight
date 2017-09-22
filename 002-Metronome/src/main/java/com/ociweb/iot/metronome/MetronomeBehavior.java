package com.ociweb.iot.metronome;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.grove.lcd_rgb.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.pipe.ChannelReader;

/*
 * Beats per minute   (build an ENUM of these so we can diplay the names on the screen.
 * 
 * Largo 40-60
 * Larghetto 60-66
 * Adagio 66-76
 * Andante 76-108
 * Moderato 108-120
 * Allegro 120-168
 * Presto 168-200
 * Prestissimo 200-208
 * 
 * 
 * 1 minute = 60_000 ms
 * 40  BPM = 1500ms
 * 300 BPM =  200ms  required (max err +-2ms)
 * 600 BPM =  100ms  nice   (max err +-1ms)
 * 
 * Test at 40, 60, 120 and 208,  the error must be < 1% 
 *   
 * 
 */

public class MetronomeBehavior implements AnalogListener, PubSubListener, StartupListener, TimeListener {

    private final FogCommandChannel tickCommandChannel;
    private final FogCommandChannel screenCommandChannel;
    
    private final String topic = "tick";
          
    private static final int REQ_UNCHANGED_MS = 120;
    
    private static final int BBM_SLOWEST     = 40; 
    private static final int BBM_FASTEST     = 208;
    
    private static final int BBM_VALUES      = 1+BBM_FASTEST-BBM_SLOWEST;
    private static final int MAX_ANGLE_VALUE = 1024;
    
    private int  requestedPBM;
    private long requestTime;
    
    private long base;
    private int beatIdx; 
    private int activeBPM;
    
    private int showingBPM;
    
    public MetronomeBehavior(FogRuntime runtime) {
        this.tickCommandChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING | FogRuntime.I2C_WRITER | FogRuntime.PIN_WRITER);
        this.screenCommandChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING | FogRuntime.I2C_WRITER | FogRuntime.PIN_WRITER);
    }

    @Override
    public void startup() {
        tickCommandChannel.subscribe(topic,this); 
        tickCommandChannel.publishTopic(topic,w->{});
        
        Grove_LCD_RGB.commandForColor(tickCommandChannel, 255, 255, 255);
        
    }

    @Override
    public void analogEvent(Port port, long time, long durationMillis, int average, int value) { 
    	requestedPBM =  BBM_SLOWEST + ((BBM_VALUES*average)/MAX_ANGLE_VALUE);   
    	requestTime = time; 
    }    

    @Override
    public boolean message(CharSequence topic, ChannelReader payload) {
            	
        if (requestedPBM>0) {

            if (activeBPM != requestedPBM && ((System.currentTimeMillis()-requestTime) > REQ_UNCHANGED_MS || activeBPM==0) ) {
            	activeBPM = requestedPBM;
                base = System.currentTimeMillis(); 
                beatIdx = 0;
            } 
                                    
            long delta = (++beatIdx*60_000)/activeBPM;
            long until = base + delta;
            tickCommandChannel.digitalPulse(IoTApp.BUZZER_PORT,500_000);     
            tickCommandChannel.blockUntil(IoTApp.BUZZER_PORT, until);
            

            if (beatIdx==activeBPM) {
            	beatIdx = 0;
            	base += 60_000; 
            }
            
        }
        tickCommandChannel.publishTopic(topic,w->{});//request next tick
        
        return true;
    }


    @Override
    public void timeEvent(long time, int iteration) {
       if (requestedPBM != showingBPM) {
                      
           String tempo;
           if (requestedPBM<108){
        	   if(requestedPBM<66){
        	       tempo = requestedPBM<60 ? "Largo" : "Larghetto";
        	   } else{
        	       tempo = requestedPBM<76 ? "Adagio" : "Andante";
        	   }
           }else{
        	   if(requestedPBM<168){
        	       tempo = requestedPBM<120 ? "Moderato" : "Allegro";
        	   } else {
        	       tempo = requestedPBM<200 ? "Presto" : "Prestissimo";
        	   }
           }
           
           String bpm = Integer.toString(requestedPBM);
           if (requestedPBM<100) {
               bpm = "0"+bpm;
           }
           
           //second channel is used so we are left waiting for one cycle of the ticks before we can update.
           if (Grove_LCD_RGB.commandForText(screenCommandChannel, bpm+"-"+tempo)) {
               showingBPM = requestedPBM;
           }
           
       }
        
    }



}
