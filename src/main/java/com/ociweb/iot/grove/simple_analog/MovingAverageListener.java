package com.ociweb.iot.grove.simple_analog;

public interface MovingAverageListener extends MovingStatsListener {
	abstract void movingAverage(double ma);
}
