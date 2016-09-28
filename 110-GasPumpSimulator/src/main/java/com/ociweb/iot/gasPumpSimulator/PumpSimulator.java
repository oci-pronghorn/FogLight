package com.ociweb.iot.gasPumpSimulator;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.iot.maker.StateChangeListener;
import com.ociweb.pronghorn.util.Appendables;

public class PumpSimulator implements DigitalListener, StateChangeListener<PumpState> {

	private Logger logger = LoggerFactory.getLogger(PumpSimulator.class);
	
	private final CommandChannel channel;	
	private final String topic;
	private boolean isActive;
	private PumpState fuelState;
	
	private int centiGallons;
	//TODO: refactor to have one listener for the button that redirects.
	
	public PumpSimulator(DeviceRuntime runtime, String topic, PumpState fuelState) {

   	  this.channel = runtime.newCommandChannel();
      this.topic = topic;   	  
   	  this.fuelState  = fuelState;
   	 
	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		if (isActive) {
			//pump 1/100 gallon or nothing
			centiGallons += value;
			display();
			
			//if not zero notify the payment screen?
			
		}		
	}

	private void display() {
				
		StringBuilder builder = new StringBuilder();
		try {
			int centsPerGallon = fuelState.centsPerGallon; //NOTE: could use event to update price and hold it locally.
			
			builder.append(fuelState.fuelName);
			builder.append(" $");
			Appendables.appendFixedDecimalDigits(builder, centsPerGallon/100, 1000);
			builder.append('.');
			Appendables.appendFixedDecimalDigits(builder, centsPerGallon%100, 10);
			builder.append("\n");
			builder.append("Gal ");
			Appendables.appendFixedDecimalDigits(builder, centiGallons/100, 1000);
			builder.append('.');
			Appendables.appendFixedDecimalDigits(builder, centiGallons%100, 10);
			int cents = (centiGallons*centsPerGallon)/100;
			builder.append(" $");
			Appendables.appendFixedDecimalDigits(builder, cents/100, 1000);
			builder.append('.');
			Appendables.appendFixedDecimalDigits(builder, cents%100, 10);
		
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		Grove_LCD_RGB.commandForColor(channel, 255, 255, 200);
		Grove_LCD_RGB.commandForText(channel, builder);
	}

	@Override
	public void stateChange(PumpState oldState, PumpState newState) {		
		isActive = (newState == fuelState);	
		
		
		//if we have some value immediatly pay and clear.
		//when we pay keep a running total cscreen("recipt")
		
		//long tap on recept screen to clear running total?
		
		//change state back to here?
		
	}



}
