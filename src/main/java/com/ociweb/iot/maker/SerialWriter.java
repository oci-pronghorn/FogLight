package com.ociweb.iot.maker;

import com.ociweb.gl.api.PayloadWriter;
import com.ociweb.iot.hardware.impl.SerialOutputSchema;
import com.ociweb.pronghorn.pipe.Pipe;

public class SerialWriter extends PayloadWriter<SerialOutputSchema>{

	protected SerialWriter(Pipe<SerialOutputSchema> p) {
		super(p);
	}

}
