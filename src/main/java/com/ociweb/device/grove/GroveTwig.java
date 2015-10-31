package com.ociweb.device.grove;

import com.ociweb.pronghorn.pipe.Pipe;

public enum GroveTwig {

    UVSensor(GroveResponseSchema.MSG_UV_20){
        @Override
        public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, int intValue) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_UV_20);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(intValue, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }        
    },
    LightSensor(GroveResponseSchema.MSG_LIGHT_30){
        @Override
        public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, int intValue) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_LIGHT_30);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(intValue, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }        
    },
    MoistureSensor(GroveResponseSchema.MSG_MOISTURE_40){
        @Override
        public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, int intValue) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_MOISTURE_40);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(intValue, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }        
    },
    Button(GroveResponseSchema.MSG_BUTTON_50) {
        @Override
        public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, int bitValue) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_BUTTON_50);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(bitValue, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }
        
    },
    MotionSensor(GroveResponseSchema.MSG_MOTION_60){
        @Override
        public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, int bitValue) {
            int size = Pipe.addMsgIdx(responsePipe,GroveResponseSchema.MSG_MOTION_60);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addIntValue(bitValue, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }        
    },
    RotaryEncoder(GroveResponseSchema.MSG_ROTARY_70) {
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
    Temp(GroveResponseSchema.MSG_TEMPRATUREANDHUMIDITY_80),
    I2C(GroveResponseSchema.MSG_UV_20);
    
    private final int messageId;
    
    private GroveTwig(int messageId) {
        this.messageId=messageId;
    }
    
        
    public int messageId() {
        return messageId;
    }

    public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, int bitValue) {
       System.err.println(this);
       throw new UnsupportedOperationException();
    }

    public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, int intValue) {
        System.err.println(this);
        throw new UnsupportedOperationException();
    }

    public void writeRotation(Pipe<GroveResponseSchema> responsePipe, int connector, int value, int delta, int speed) {
        System.err.println(this);
        throw new UnsupportedOperationException();
    }
}
