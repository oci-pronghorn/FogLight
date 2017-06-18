package com.ociweb.iot.hardware.impl;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;

public class SerialInputSchema extends SerialDataSchema<SerialInputSchema> {

	public final static FieldReferenceOffsetManager FROM = SerialDataSchema.FROM;
	public static final SerialInputSchema instance = new SerialInputSchema();
	
}
