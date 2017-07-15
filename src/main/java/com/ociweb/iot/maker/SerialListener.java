package com.ociweb.iot.maker;

import com.ociweb.gl.api.Behavior;
import com.ociweb.pronghorn.pipe.BlobReader;

public interface SerialListener extends Behavior {

	public int message(BlobReader reader);
	
}
