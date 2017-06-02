package com.ociweb.iot.maker;

import com.ociweb.pronghorn.pipe.DataInputBlobReader;

public interface SerialListener {

	public boolean message(DataInputBlobReader reader);
	
}
