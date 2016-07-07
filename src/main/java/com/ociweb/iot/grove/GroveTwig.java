package com.ociweb.iot.grove;

import com.ociweb.iot.hardware.IODevice;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;

public enum GroveTwig implements IODevice {

    UVSensor(){
        @Override
        public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int intValue, int average) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_ANALOGSAMPLE_30);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addLongValue(time, responsePipe);
            Pipe.addIntValue(intValue, responsePipe);
            Pipe.addIntValue(average, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }    
        
        @Override
        public boolean isInput() {
            return true;
        }
    },
    LightSensor(){
        @Override
        public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int intValue, int average) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_ANALOGSAMPLE_30);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addLongValue(time, responsePipe);
            Pipe.addIntValue(intValue, responsePipe);
            Pipe.addIntValue(average, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }   
        
        @Override
        public boolean isInput() {
            return true;
        }
    },
    MoistureSensor(){
        @Override
        public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int intValue, int average) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_ANALOGSAMPLE_30);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addLongValue(time, responsePipe);
            Pipe.addIntValue(intValue, responsePipe);
            Pipe.addIntValue(average, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }       
        
        @Override
        public boolean isInput() {
            return true;
        }
    },
    Button() {
        @Override
        public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int bitValue) {
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_DIGITALSAMPLE_20);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addLongValue(time, responsePipe);
            Pipe.addIntValue(bitValue, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }
        
        @Override
        public boolean isInput() {
            return true;
        }
        
    },
    MotionSensor(){
        @Override
        public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int bitValue) {
            int size = Pipe.addMsgIdx(responsePipe,GroveResponseSchema.MSG_DIGITALSAMPLE_20);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addLongValue(time, responsePipe);
            Pipe.addIntValue(bitValue, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
        }       
        
        @Override
        public boolean isInput() {
            return true;
        }
    },
    RotaryEncoder() {
        @Override
        public void writeRotation(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int value, int delta, int speed) {            
            int size = Pipe.addMsgIdx(responsePipe, GroveResponseSchema.MSG_ENCODER_70);
            Pipe.addIntValue(connector, responsePipe);
            Pipe.addLongValue(time, responsePipe);
            Pipe.addIntValue(value, responsePipe);
            Pipe.addIntValue(delta, responsePipe);            
            Pipe.addIntValue(speed, responsePipe);
            Pipe.publishWrites(responsePipe);
            Pipe.confirmLowLevelWrite(responsePipe, size);
            
        }
        
        @Override
        public boolean isInput() {
            return true;
        }
        
    },
    Buzzer() {
        
        @Override
        public boolean isOutput() {
            return true;
        }
    },
    LED() {
        
        @Override
        public boolean isOutput() {
            return true;
        }
        
        @Override
        public boolean isPWM() {
            return true;
        }
    },
    Relay() {
        
        @Override
        public boolean isOutput() {
            return true;
        }
    },
    Survo() {
        
        @Override
        public boolean isOutput() {
            return true;
        }
    },
    I2C() {
        
        @Override
        public boolean isInput() {
            return true;
        }
        
        @Override
        public boolean isOutput() {
            return true;
        }
    };
    
    

    public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int bitValue) {
       System.err.println(this);
       throw new UnsupportedOperationException();
    }

    public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int intValue, int average) {
        System.err.println(this);
        throw new UnsupportedOperationException();
    }

    public void writeRotation(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int value, int delta, int speed) {
        System.err.println(this);
        throw new UnsupportedOperationException();
    }
    
    public boolean isInput() {
        return false;
    }
    
    public boolean isOutput() {
        return false;
    }
    
    public boolean isPWM() {
        return false;
    }
    public boolean isI2C(){
    	return false;
    }
    public byte[] getReadMessage(){
    	return null;
    }
    
    
}
