package com.ociweb.iot.grove.gps;

import com.ociweb.gl.api.Behavior;

public interface GeoCoordinateListener extends Behavior {
	void coordinates(int longtitude, int lattitude);
}
