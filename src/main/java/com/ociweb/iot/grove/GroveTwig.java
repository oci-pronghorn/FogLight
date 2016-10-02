package com.ociweb.iot.grove;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;

/**
 * Holds information for all standard A/D I/O twigs in the Grove starter kit.
 * Methods are necessary for interpreting new connections declared in declareConnections(Hardware c) in the maker app.
 * @see com.ociweb.iot.hardware.IODevice
 */
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
            return 100; 
        }
    },
    SoundSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
                
        public int response() {
            return 2;
        }
    },
    AngleSensor(){
        @Override
        public boolean isInput() {
            return true;
        }
        
        public int response() {
            return 40;
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
        
        public int response() {
            return 40;
        }
        
        @Override
        public int range() {
            return 1;
        }
        
    },
    MotionSensor(){
        @Override
        public boolean isInput() {
            return true;
        }

        @Override
        public int range() {
            return 1;
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
    },
    UltrasonicRanger() {
    	 @Override
         public boolean isInput() {
             return true;
         }
    	 
    	 public int range() {
    	        return 1024;
    	 }
    	    
         public int response() {
             return 200; 
         }
         
         public int scanDelay() {
         	return 1_420_000;
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
    
    public int scanDelay() {
    	return 0;
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
