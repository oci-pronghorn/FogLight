package com.ociweb.iot.maker;

import com.ociweb.pronghorn.pipe.BlobWriter;

public interface SerialWritable {

	void write(BlobWriter writer);
	
}
