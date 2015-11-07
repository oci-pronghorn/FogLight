package com.ociweb.device.grove.schema;

import com.ociweb.pronghorn.pipe.RawDataSchema;

public class GroveI2CSchema extends RawDataSchema {
    
    public static final GroveI2CSchema instance = new GroveI2CSchema();
    
    private GroveI2CSchema() {
        super();
    }
}
