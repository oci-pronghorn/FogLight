package com.ociweb.iot.maker;

import com.ociweb.gl.api.Behavior;

public interface SerialListener extends Behavior {

	public boolean message(SerialReader reader);
	
}
