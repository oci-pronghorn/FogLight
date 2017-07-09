package com.ociweb.iot.maker;

import com.ociweb.gl.api.Behavior;

public interface SerialListener extends Behavior {

	//returns the number of bytes consumed
	public int message(SerialReader reader);
	
}
