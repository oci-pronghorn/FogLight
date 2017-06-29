package com.ociweb.iot.grove;

import static com.ociweb.iot.grove.accelerometer.Grove_Acc_Constants.ADXL345_DATAX0;
import static com.ociweb.iot.grove.accelerometer.Grove_Acc_Constants.ADXL345_DEVICE;

import com.ociweb.iot.grove.OLED.OLED_128x64.OLED_128x64_Facade;
import com.ociweb.iot.grove.OLED.OLED_96x96.OLED_96x96_Facade;
import com.ociweb.iot.grove.RTC.Grove_RTC_Constants;
import com.ociweb.iot.grove.RTC.RTC;
import com.ociweb.iot.grove.accelerometer.Accelerometer_16g;
import com.ociweb.iot.grove.accelerometer.Grove_Acc_Constants;
import static com.ociweb.iot.grove.mini_i2c_motor.Grove_Mini_I2CMotor_Constants.CH1_ADD;
import static com.ociweb.iot.grove.mini_i2c_motor.Grove_Mini_I2CMotor_Constants.FAULT_REG;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.Hardware;
import static com.ociweb.iot.grove.four_digit_display.Grove_FourDigitDisplay.*;

/**
 * Holds information for all standard Analog and Digital I/O twigs in the Grove starter kit.
 *
 * Methods are necessary for interpreting new connections declared in
 * IoTSetup declareConnections in the maker app.
 *
 * @see com.ociweb.iot.hardware.IODevice
 */


public enum GroveTwig implements IODevice {
    
    UVSensor() {
        @Override
        public boolean isInput() {
            return true;
        }
        
        @Override
        public int response() {
            return 30;
        }
        
        
    },
    LightSensor() {
        @Override
        public boolean isInput() {
            return true;
        }
        
        @Override
        public int response() {
            return 100;
        }
    },
    SoundSensor() {
        @Override
        public boolean isInput() {
            return true;
        }
        
        @Override
        public int response() {
            return 2;
        }
    },
    AngleSensor() {
        @Override
        public boolean isInput() {
            return true;
        }
        
        @Override
        public int response() {
            return 40;
        }
        @Override
        public int range() {
            return 1024;
        }
    },
    MoistureSensor() {
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
    MotionSensor() {
        @Override
        public boolean isInput() {
            return true;
        }
        
        @Override
        public int range() {
            return 1;
        }
    },
    LineFinder() {
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
        
        @Override
        public int range() {
            return 1024;
        }
        
        @Override
        public int response() {
            return 200;
        }
        
        @Override
        public int scanDelay() {
            return 1_420_000;
        }
        
    },
    ThumbJoystick(){
        @Override
        public boolean isInput(){
            return true;
        }
        @Override
        public int range(){
            return 1024;
        }
        @Override
        public int pinsUsed(){
            return 2;
        }
        public boolean isPressed(int val){
            return val == 1023;
        }
    },
    FourDigitDisplay(){
    	@Override
    	public boolean isOutput(){
    		return true;
    	}
    	
    	@Override
    	public int pinsUsed(){
    		return 2;
    	}
    	
        @Override
        public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
            byte[] read_cmd = {};
            byte[] set_up = {GROVE_TM1637_INIT, 5, 0x00,0x00}; //default to digit output 5
            byte address = 0x4;
            byte bytes_to_read = 0;
            byte reg = 0;
            return new I2CConnection(this, address, read_cmd, bytes_to_read, reg, set_up);
        }
     
        @Override
        public byte[] I2COutSetup() {
        	byte [] set_up = {GROVE_TM1637_INIT, 5, 0x00,0x00};
            return set_up;
        }
    },
    
    VibrationSensor(){
        @Override
        public boolean isInput(){
            return true;
        }
        @Override
        public int range(){
            return 1024;
        }
    },
    
    OLED_128x64(){
        @Override
        public boolean isOutput(){
            return true;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <F extends IODeviceFacade> F newFacade(FogCommandChannel... ch) {
            return (F) new OLED_128x64_Facade(ch[0]);
        }
    },
    
    OLED_96x96(){
        @Override
        public boolean isOutput(){
            return true;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <F extends IODeviceFacade> F newFacade(FogCommandChannel...ch){
            return (F) new OLED_96x96_Facade(ch[0]);
        }
    },
    ThreeAxis_Accelerometer_16G(){
        @Override
        public boolean isInput() {
            return true;
        }
        
        @Override
        public boolean isOutput() {
            return true;
        }
        
        @Override
        public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
            byte[] ACC_READCMD = {Grove_Acc_Constants.ADXL345_DATAX0};
            //byte[] ACC_SETUP = {ADXL345_POWER_CTL,0x08};
            byte[] ACC_SETUP = {};
            byte ACC_ADDR = Grove_Acc_Constants.ADXL345_DEVICE;
            byte ACC_BYTESTOREAD = 6;
            byte ACC_REGISTER = 0x07; //just an identifier
            return new I2CConnection(this, ACC_ADDR, ACC_READCMD, ACC_BYTESTOREAD, ACC_REGISTER, ACC_SETUP);
        }
        
        
        @Override
        public int response() {
            return 1000;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <F extends IODeviceFacade> F newFacade(FogCommandChannel...ch){
            return (F) new Accelerometer_16g(ch[0]);
        }
        
    },
    RTC(){
        @Override
        public boolean isInput() {
            return true;
        }
        
        @Override
        public boolean isOutput() {
            return true;
        }
        @Override
        public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
            byte[] ACC_READCMD = {Grove_RTC_Constants.TIME_REG};
            //byte[] ACC_SETUP = {ADXL345_POWER_CTL,0x08};
            byte[] ACC_SETUP = {};
            byte ACC_ADDR = Grove_RTC_Constants.DS1307_I2C_ADDRESS;
            byte ACC_BYTESTOREAD = 7;
            byte ACC_REGISTER = 0x07; //just an identifier
            return new I2CConnection(this, ACC_ADDR, ACC_READCMD, ACC_BYTESTOREAD, ACC_REGISTER, ACC_SETUP);
        }
        
        
        @Override
        public int response() {
            return 1000;
            
        }
//    @SuppressWarnings("unchecked")
//        @Override
//        public <F extends IODeviceFacade> F newFacade(FogCommandChannel...ch){
//            return (F) new RTC(ch[0]);
//        }
    },
    Mini_I2C_Motor(){
        @Override
        public boolean isInput() {
            return true;
        }
        
        @Override
        public boolean isOutput() {
            return true;
        }
        @Override
        public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
            byte[] MOTOR_READCMD = {FAULT_REG};
            byte[] MOTOR_SETUP = {};
            byte MOTOR_ADDR = CH1_ADD;
            byte MOTOR_BYTESTOREAD = 1;
            byte MOTOR_REGISTER = 0x23;  //register identifier
            return new I2CConnection(this, MOTOR_ADDR, MOTOR_READCMD, MOTOR_BYTESTOREAD, MOTOR_REGISTER, MOTOR_SETUP);
        }
        
        @Override
        public int response() {
            return 1000;
        }
    },
    
    WaterSensor(){
        @Override
        public boolean isInput(){
            return true;
        }
        
        @Override
        public int range(){
            return 1024;
        }
    },
    TouchSensor() {
        @Override()
        public boolean isInput(){
            return true;
        }
        
        @Override
        public int range(){
            return 1;
        }
        
        @Override
        public int response(){
            return 60;
        }
    };
    /**
     * @return True if this twig is an input device, and false otherwise.
     */
    public boolean isInput() {
        return false;
    }
    
    /**
     * @return True if this twig is an output device, and false otherwise.
     */
    public boolean isOutput() {
        return false;
    }
    
    /**
     * @return Response time, in milliseconds, for this twig.
     */
    public int response() {
        return 20;
    }
    
    /**
     * @return Delay, in milliseconds, for scan. TODO: What's scan?
     */
    public int scanDelay() {
        return 0;
    }
    
    /**
     * @return True if this twig is Pulse Width Modulated (PWM) device, and
     *         false otherwise.
     */
    public boolean isPWM() {
        return false;
    }
    
    /**
     * @return True if this twig is an I2C device, and false otherwise.
     */
    public boolean isI2C() {
        return false;
    }
    
    /**
     * @return The {@link I2CConnection} for this twig if it is an I2C
     *         device, as indicated by {@link #isI2C()}.
     */
    public I2CConnection getI2CConnection() {
        return null;
    }
    
    /**
     * @return The possible value range for reads from this device (from zero).
     */
    public int range() {
        return 256;
    }
    
    /**
     * @return the setup bytes needed to initialized the connected I2C device
     */
    public byte[] I2COutSetup() {
        return null;
    }
    
    /**
     * Validates if the I2C data from from the device is a valid response for this twig
     *
     * @param backing
     * @param position
     * @param length
     * @param mask
     *
     * @return fals if the bytes returned from the device were not some valid response
     */
    public boolean isValid(byte[] backing, int position, int length, int mask) {
        return true;
    }
    
    /**
     * @return The number of hardware pins that this twig uses.
     */
    public int pinsUsed() {
        return 1;
    }
    
    
    public <F extends IODeviceFacade> F newFacade(FogCommandChannel... ch) {
        return null;
    }
}
