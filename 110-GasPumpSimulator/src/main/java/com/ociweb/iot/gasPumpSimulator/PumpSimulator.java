package com.ociweb.iot.gasPumpSimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.PayloadWriter;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.maker.StateChangeListener;

public class PumpSimulator implements DigitalListener, StateChangeListener<PumpState> {

	private Logger logger = LoggerFactory.getLogger(PumpSimulator.class);

	private final CommandChannel channel;
	private final String fuelName;
	private final int centsPerUnit;

	private final String pumpTopic;
	private final String totalTopic;
	private boolean isActive;

	private int units;
	private int totalUnits;
	private long time;

	public PumpSimulator(DeviceRuntime runtime, String pumpTopic, String totalTopic, String fuelName, int centsPerGallon) {

   	  this.channel = runtime.newCommandChannel();
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

			PayloadWriter payload = channel.openTopic(pumpTopic);

			payload.writeLong(time);
			payload.writeUTF(fuelName);
			payload.writeInt(centsPerUnit);
			payload.writeInt(units);

			payload.publish();

		}

	}


	@Override
	public void stateChange(PumpState oldState, PumpState newState) {

		//exit pump mode
		if (oldState == PumpState.Pump) {
			isActive = false;

			if (units>0) {
				totalUnits += units;
				units = 0;

				PayloadWriter payload = channel.openTopic(totalTopic);

				payload.writeLong(this.time);
				payload.writeUTF(fuelName);
				payload.writeInt(centsPerUnit);
				payload.writeInt(totalUnits);

				payload.publish();
			}
		}

		//enter pump mode
		if (newState == PumpState.Pump) {
			isActive = true;

		}

	}

}
