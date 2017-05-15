package com.ociweb.iot.gasPumpSimulator;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.PayloadWriter;
import com.ociweb.gl.impl.schema.MessagePubSub;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.DigitalListener;
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
	private long time;

	public PumpSimulator(DeviceRuntime runtime, String pumpTopic, String totalTopic, String fuelName, int centsPerGallon) {

   	  this.channel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
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

			Optional<PayloadWriter<MessagePubSub>> payload = channel.openTopic(pumpTopic);
			payload.ifPresent(w->{
				w.writeLong(time);
				w.writeUTF(fuelName);
				w.writeInt(centsPerUnit);
				w.writeInt(units);
				
				w.publish();				
			});

		}

	}


	@Override
	public void stateChange(PumpState oldState, PumpState newState) {

		//exit pump mode
		if (oldState == PumpState.Pump) {
			isActive = false;

			if (units>0) {

				Optional<PayloadWriter<MessagePubSub>> payload = channel.openTopic(totalTopic);
				payload.ifPresent(w->{
					w.writeLong(this.time);
					w.writeUTF(fuelName);
					w.writeInt(centsPerUnit);
					w.writeInt(units);
					units = 0;
					
					w.publish();					
				});
			}
		}

		//enter pump mode
		if (newState == PumpState.Pump) {
			isActive = true;

		}

	}

}
