package com.ociweb.iot.grove.simple_analog;

import com.ociweb.pronghorn.util.ma.MAvgRollerLong;

public interface MovingAverageListener extends MovingStatsListener {
	abstract void movingAverage(double ma);
}
