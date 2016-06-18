package com.ociweb.iot.hardware;

import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;

public interface IODevice {

     public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, int bitValue);
     public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, int intValue, int average);
     public void writeRotation(Pipe<GroveResponseSchema> responsePipe, int connector, int value, int delta, int speed);
     public boolean isInput();
     public boolean isOutput();
     public boolean isPWM();
     
}
