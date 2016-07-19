package com.ociweb.iot.metronome;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.IOTDeviceRuntime;
import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.iot.maker.StartupListener;

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


public class MetronomeBehavior implements AnalogListener, PubSubListener, StartupListener {

    private final CommandChannel commandChannel;
    private final String topic = "tick";
          
    
    private static final int BBM_SLOWEST     = 40;
    private static final int BBM_FASTEST     = 208;
    
    private static final int BBM_VALUES      = 1+BBM_FASTEST-BBM_SLOWEST;
    private static final int MAX_ANGLE_VALUE = 1024;
    
    private long base;
    private int beatIdx; 
    private int activeBPM;
    
    public MetronomeBehavior(IOTDeviceRuntime runtime) {
        commandChannel = runtime.newCommandChannel();
    }
    

    @Override
    public void startup() {
        commandChannel.subscribe(topic,this);
        commandChannel.openTopic(topic).publish();
        
        Grove_LCD_RGB.commandForColor(commandChannel, 255, 255, 255);
        
    }

    long timeOfNewValue;
    int tempBPM;
    
    @Override
    public void analogEvent(int connector, long time, int average, int value) {

            int newBPM =  BBM_SLOWEST + ((BBM_VALUES*value)/MAX_ANGLE_VALUE);            
            if (newBPM != tempBPM) {                
            	
            	timeOfNewValue = System.currentTimeMillis();
            	tempBPM = newBPM;
            	
            	String message = " BPM "+tempBPM;
    			System.out.println(message);
    			
    			//Can not call frequenlty or we get stack trace error.
    			Grove_LCD_RGB.commandForText(commandChannel, message);
            	
            } else {
            	if (System.currentTimeMillis()-timeOfNewValue>500) {
            		if (tempBPM != activeBPM) {
            			
            			activeBPM = tempBPM;
            			base = 0; //reset signal                 
            			            			
            			
            		}
            		
            	}
            	
            }
 
    }    


    @Override
    public void message(CharSequence topic, PayloadReader payload) {
        
        commandChannel.openTopic(topic).publish();//request next tick while we get this one ready
                
        if (activeBPM>0) {
            
            if (0==base) {
                base = System.currentTimeMillis();
                beatIdx = 0;
            }                
                                    
            long until = base + ((++beatIdx*60_000L)/activeBPM);

            commandChannel.digitalPulse(IoTApp.BUZZER_CONNECTION);        
            commandChannel.blockUntil(IoTApp.BUZZER_CONNECTION, until); //mark connection as blocked until

            if (beatIdx==activeBPM) {
            	beatIdx = 0;
            	base += 60_000;
            }                     

            
        }
        
    }



}
