package com.ociweb.iot.grove;

import com.ociweb.iot.hardware.IODevice;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;

public enum GroveTwig implements IODevice {

    UVSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
    },
    LightSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
    },
    SoundSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
    },
    AngleSensor(){
        @Override
        public boolean isInput() {
            return true;
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
    },
    Nunchuck() {
    	
    	@Override
    	public boolean isInput() {
    		return true;
    	}
    	
    },
    TempHumid() {
    	
    	@Override
    	public boolean isInput(){
    		return true;
    	}
    };
    
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
    public int pwmRange() {
        return 256;
    }
    public boolean isGrove(){
    	return true;
    }
    
}
