package com.ociweb.device.grove;

import com.ociweb.device.grove.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;

public enum GroveTwig {

    UVSensor(){
        @Override
        public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, int intValue, int average) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_UV_20);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(intValue, responsePipe);
            Pipe.addIntValue(average, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }        
    },
    LightSensor(){
        @Override
        public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, int intValue, int average) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_LIGHT_30);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(intValue, responsePipe);
            Pipe.addIntValue(average, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }        
    },
    MoistureSensor(){
        @Override
        public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, int intValue, int average) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_MOISTURE_40);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(intValue, responsePipe);
            Pipe.addIntValue(average, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }        
    },
    Button() {
        @Override
        public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, int bitValue) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_BUTTON_50);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(bitValue, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }
        
    },
    MotionSensor(){
        @Override
        public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, int bitValue) {
            int size = Pipe.addMsgIdx(responsePipe,GroveResponseSchema.MSG_MOTION_60);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(bitValue, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }        
    },
    RotaryEncoder() {
        @Override
        public void writeRotation(Pipe<GroveResponseSchema> responsePipe, int connector, int value, int delta, int speed) {            
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_ROTARY_70);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(value, responsePipe);
            Pipe.addIntValue(delta, responsePipe);            
            Pipe.addIntValue(speed, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
            
        }
        
    },
    Buzzer(),
    Relay(),
    Survo(),
    I2C();
    
    

    public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, int bitValue) {
       System.err.println(this);
       throw new UnsupportedOperationException();
    }

    public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, int intValue, int average) {
        System.err.println(this);
        throw new UnsupportedOperationException();
    }

    public void writeRotation(Pipe<GroveResponseSchema> responsePipe, int connector, int value, int delta, int speed) {
        System.err.println(this);
        throw new UnsupportedOperationException();
    }
}
