package com.ociweb.iot.maker;

import com.ociweb.iot.hardware.impl.SerialInputSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.Pipe;

public class SerialReader extends DataInputBlobReader<SerialInputSchema> {

	public SerialReader(Pipe<SerialInputSchema> pipe) {
		super(pipe);
	}

}
