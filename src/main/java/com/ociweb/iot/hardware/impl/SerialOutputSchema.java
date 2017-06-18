package com.ociweb.iot.hardware.impl;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;

public class SerialOutputSchema extends SerialDataSchema<SerialOutputSchema> {

	public final static FieldReferenceOffsetManager FROM = SerialDataSchema.FROM;
	public static final SerialOutputSchema instance = new SerialOutputSchema();
	
}
