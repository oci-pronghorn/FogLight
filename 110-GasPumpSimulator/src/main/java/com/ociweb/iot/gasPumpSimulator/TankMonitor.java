package com.ociweb.iot.gasPumpSimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Port;

public class TankMonitor implements AnalogListener {

	private final CommandChannel commandChannel;
  private final String topic;
  private final int fullTank;
  private final int radiusMM = 42;
  private final int radiusSquared = radiusMM*radiusMM;;
  private final double radiusSquaredPi = Math.PI*radiusSquared;
  private final String fuelName;
  private final Logger logger = LoggerFactory.getLogger(TankMonitor.class);
	private int lastValue = Integer.MIN_VALUE;

	public TankMonitor(DeviceRuntime runtime, String topic, int fullTank, String fuelName) {

		this.commandChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
		this.topic = topic;
		this.fuelName = fuelName;
		this.fullTank = fullTank;

	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {

		if (value>fullTank) {
			logger.trace("check equipment, tank {} is deeper than expected {} ",value,fullTank);
			value = fullTank;//default value for the error case
		}

		//sample must be stable for 2 seconds before we consider it safe for use
		if (durationMillis > 2000 && (value!=lastValue)) {

			int volumeCM = computeVolumeCM2(value);

			commandChannel.openTopic(topic, w->{
				w.writeLong(time); //local time, may be off, do check the os
				w.writeInt(volumeCM);
				w.writeUTF(fuelName);
				
				w.publish();
				
			});
			lastValue = value;
		}

	}

	private int computeVolumeCM2(int value) {
		int remainingDepthMM =  10*(fullTank-value);
		double volumeMM2 = radiusSquaredPi * remainingDepthMM;
		int volumeCM = (int) Math.rint(volumeMM2/100d);
		return volumeCM;
	}


}
