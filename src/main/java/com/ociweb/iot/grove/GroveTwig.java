package com.ociweb.iot.grove;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;

public enum GroveTwig implements IODevice {

    UVSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
        public int response() {
            return 30; 
        }
    },
    LightSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
        
        public int response() {
            return 40; 
        }
    },
    SoundSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
                
        public int response() {
            return 0; //special up to 20Khz
        }
    },
    AngleSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
        
        public int response() {
            return 20;
        }
        
        public int range() {
            return 1024;
        }
    },
    MoistureSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
    },
    Button() {
        @Override
        public boolean isInput() {
            return true;
        }
        
    },
    MotionSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
    },
    RotaryEncoder() {
        @Override
        public boolean isInput() {
            return true;
        }
        
        @Override
        public int pinsUsed() {
            return 2;
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
    Servo() {
        
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
    
    public boolean isInput() {
        return false;
    }
    
    public boolean isOutput() {
        return false;
    }
    
    public int response() {
        return 20;
    }
    
    public boolean isPWM() {
        return false;
    }
    public boolean isI2C(){
    	return false;
    }
    public I2CConnection getI2CConnection(){
    	return null;
    }
    public int range() {
        return 256;
    }
    public boolean isGrove(){
    	return true;
    }
    public byte[] I2COutSetup(){
    	return null;
    }
    public boolean isValid(byte[] backing, int position, int length, int mask){
    	return true;
    }
    
    public int pinsUsed() {
        return 1;
    }
    
}
