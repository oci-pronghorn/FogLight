package com.ociweb.iot.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.Port;
import com.ociweb.gl.api.*;

public class PumpSimulator implements DigitalListener, StateChangeListener<PumpState> {

	private Logger logger = LoggerFactory.getLogger(PumpSimulator.class);

	private final PubSubService channel;
	private final String fuelName;
	private final int centsPerUnit;

	private final String pumpTopic;
	private final String totalTopic;
	private boolean isActive;

	private int units;
	private long time;

	public PumpSimulator(FogRuntime runtime, String pumpTopic, String totalTopic, String fuelName, int centsPerGallon) {

   	  this.channel = runtime.newCommandChannel().newPubSubService();
      this.pumpTopic = pumpTopic;
      this.totalTopic = totalTopic;
   	  this.fuelName = fuelName;
   	  this.centsPerUnit = centsPerGallon;

	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {

		if (isActive) {


			this.units += value;
			this.time = time;

			channel.publishTopic(pumpTopic,w->{
				w.writeLong(time);
				w.writeUTF(fuelName);
				w.writeInt(centsPerUnit);
				w.writeInt(units);		
			});

		}

	}


	@Override
	public boolean stateChange(PumpState oldState, PumpState newState) {

		//exit pump mode
		if (oldState == PumpState.Pump) {
			isActive = false;

			if (units>0) {

				channel.publishTopic(totalTopic, w->{
					w.writeLong(this.time);
					w.writeUTF(fuelName);
					w.writeInt(centsPerUnit);
					w.writeInt(units);
					units = 0;					
				});
			}
		}

		//enter pump mode
		if (newState == PumpState.Pump) {
			isActive = true;

		}
		return true;

	}

}
