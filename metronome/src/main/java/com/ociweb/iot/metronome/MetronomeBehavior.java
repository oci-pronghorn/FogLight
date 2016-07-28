package com.ociweb.iot.metronome;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.iot.maker.StartupListener;
import com.ociweb.iot.maker.TimeListener;

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

//importance of interface. 
//multiple inheretiance (issue, language have in general),overlap of overlap 
public class MetronomeBehavior implements AnalogListener, PubSubListener, StartupListener, TimeListener {

    private final CommandChannel tickCommandChannel;
    private final CommandChannel screenCommandChannel;
    
    private final String topic = "tick";
          
    
    private static final int BBM_SLOWEST     = 40; // the  private static final int 
    private static final int BBM_FASTEST     = 208;
    
    private static final int BBM_VALUES      = 1+BBM_FASTEST-BBM_SLOWEST;
    private static final int MAX_ANGLE_VALUE = 1024;
    
    private long base;
    private int beatIdx; 
    private int activeBPM;
    
    private long timeOfNewValue;
    private int tempBPM;
    private int showingBPM;
    
    public MetronomeBehavior(DeviceRuntime runtime) {
        this.tickCommandChannel = runtime.newCommandChannel();
        this.screenCommandChannel = runtime.newCommandChannel();
    }
    //this.commandChannel, as a parameter of sth else. you will give it to sb as incomplete stage
// pass this in sth in the constructor 
    @Override
    public void startup() {
        tickCommandChannel.subscribe(topic,this); //take this because it is a pub listen/ current object. valid for use 
        tickCommandChannel.openTopic(topic).publish();
        
        Grove_LCD_RGB.commandForColor(tickCommandChannel, 255, 255, 255);
        
    }

    //we will talk about override
    @Override
    public void analogEvent(int connector, long time, int average, int value) {
  
    //later in the deck, do the publish subsribe 
            int newBPM =  BBM_SLOWEST + ((BBM_VALUES*value)/MAX_ANGLE_VALUE);       //math value, long, int, beat at the right (primitive work) order of operation      
            if (newBPM != tempBPM) {                
            	
            	timeOfNewValue = System.currentTimeMillis();
            	tempBPM = newBPM;
            	
            } else {
            	if (System.currentTimeMillis()-timeOfNewValue>333) {
            		if (tempBPM != activeBPM) {
            			
            			activeBPM = tempBPM;
            			base = 0; //reset signal  
            			
            		}            		
            	}            	
            } 
    }    

    @Override
    public void message(CharSequence topic, PayloadReader payload) {
        
    	
        if (activeBPM>0) {

            if (0==base) {
                base = System.currentTimeMillis(); //this is a standard java they should know. 1970 UMT
                beatIdx = 0;
            }                
                                    
            long delta = (++beatIdx*60_000)/activeBPM;//will multiple the pre incremental value if do after 
            long until = base + delta;
            
            tickCommandChannel.digitalPulse(IoTApp.BUZZER_CONNECTION);     
            tickCommandChannel.blockUntil(IoTApp.BUZZER_CONNECTION, until); //mark connection as blocked until
            

            if (beatIdx==activeBPM) {
            	beatIdx = 0;
            	base += 60_000; //will talk about the operator 
            }
            
        }
        tickCommandChannel.openTopic(topic).publish();//request next tick
        
    }


    @Override
    public void timeEvent(long time) {
       if (tempBPM != showingBPM) {
                      
           String tempo;
           if (tempBPM<108){
        	   if(tempBPM<66){
        	       tempo = tempBPM<60 ? "Largo" : "Larghetto";
        	   } else{
        	       tempo = tempBPM<76 ? "Adagio" : "Andante";
        	   }
           }else{
        	   if(tempBPM<168){
        	       tempo = tempBPM<120 ? "Moderato" : "Allegro";
        	   } else {
        	       tempo = tempBPM<200 ? "Presto" : "Prestissimo";
        	   }
           }
           
           String bpm = Integer.toString(tempBPM);
           if (tempBPM<100) {
               bpm = "0"+bpm;
           }
           
           //second channel is used so we are left waiting for one cycle of the ticks before we can update.
           if (Grove_LCD_RGB.commandForText(screenCommandChannel, bpm+"-"+tempo)) {
               showingBPM = tempBPM;
           }
           
       }
        
    }



}
