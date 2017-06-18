package com.ociweb.iot.hardware.impl;

import com.ociweb.iot.maker.SerialReader;

public interface SerialParser {

	//objects which consume serial feedback should implement this interface
	public boolean parse(SerialReader reader);
	
}
