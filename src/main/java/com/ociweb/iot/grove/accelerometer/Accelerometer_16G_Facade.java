/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.accelerometer;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import static com.ociweb.iot.grove.accelerometer.Accelerometer_16G_Constants.*;
import com.ociweb.iot.maker.IODeviceFacade;

/**
 *
 * @author huydo
 */
public class Accelerometer_16G_Facade implements IODeviceFacade {
    FogCommandChannel target;
    public static final int ADXL345_DEVICE    = 0x53;
    
    public Accelerometer_16G_Facade(FogCommandChannel ch){
        this.target = ch;
    }
    /**
     * Start the device in measurement mode, with auto-sleep disabled and sleep mode disabled
     */
    public void begin() {
        
        writeSingleByteToRegister(Accelerometer_16G_Constants.ADXL345_POWER_CTL,0);
        
        writeSingleByteToRegister(Accelerometer_16G_Constants.ADXL345_POWER_CTL,16);
        
        writeSingleByteToRegister(Accelerometer_16G_Constants.ADXL345_POWER_CTL,8);
        
    }
    /**
     * Set the range of acceleration data to be +/- 2,4,8 or 16g
     * @param range 2,4,8 or 16. The default is 16
     */
    public void setRange(int range){
        byte _s;
        switch(range){
            case 2:
                _s = 0b00001000;
                break;
            case 4:
                _s = 0b00001001;
                break;
            case 8:
                _s = 0b00001010;
                break;
            case 16:
                _s = 0b00001011;
                break;
            default:
                _s = 0b00001011;
        }
        writeSingleByteToRegister(ADXL345_DATA_FORMAT,_s);
        
    }
    
    
    /**
     * Sets the OFSX, OFSY and OFSZ bytes
     * x, y and z are user offset adjustments in twos complement format with
     * a scale factor of 15,6mg/LSB
     * @param x
     * @param y
     * @param z
     */
    public void setAxisOffset(int x, int y, int z) {
        
        writeSingleByteToRegister(ADXL345_OFSX,(byte) x);
        
        writeSingleByteToRegister(ADXL345_OFSY,(byte) y);
        
        writeSingleByteToRegister(ADXL345_OFSZ,(byte) z);
    }
    /**
     * Set the output data rate from the accelerometer (in Hz)
     * @param rate 6,12,25,50,100,200,400,800,1600 or 3200 Hz
     */
    public void setRate(int rate){
        byte _s;
        switch(rate){
            case 3200:
                _s = ADXL345_RATE_3200;
                break;
            case 1600:
                _s = ADXL345_RATE_1600;
                break;
            case 800:
                _s = ADXL345_RATE_800;
                break;
            case 400:
                _s = ADXL345_RATE_400;
                break;
            case 200:
                _s = ADXL345_RATE_200;
                break;
            case 100:
                _s = ADXL345_RATE_100;
                break;
            case 50:
                _s = ADXL345_RATE_50;
                break;
            case 25:
                _s = ADXL345_RATE_25;
                break;
            case 12:
                _s = ADXL345_RATE_12;
                break;
            case 6:
                _s = ADXL345_RATE_6;
                break;
            default:
                _s = ADXL345_RATE_400;
        }
        writeSingleByteToRegister(ADXL345_DATA_FORMAT,_s);
        
        
    }
    /**
     * Sets the THRESH_TAP byte value
     * it should be between 0 and 255
     * the scale factor is 62.5 mg/LSB
     * A value of 0 may result in undesirable behavior
     * @param tapThreshold integer between 0 and 255
     */
    public void setTapThreshold(int tapThreshold){
        writeSingleByteToRegister(ADXL345_THRESH_TAP,tapThreshold);
    }
    
    /**
     * Sets the DUR byte
     * The DUR byte contains an unsigned time value representing the maximum time
     * that an event must be above THRESH_TAP threshold to qualify as a tap event
     * The scale factor is 625µs/LSB
     * A value of 0 disables the tap/double tap functions. Max value is 255.
     * @param tapDuration integer between 0 and 255
     */
    
    public void setTapDuration(int tapDuration){
        writeSingleByteToRegister(ADXL345_DUR,tapDuration);
    }
    
    /**
     *
     *  Sets the latency (latent register) which contains an unsigned time value
     * representing the wait time from the detection of a tap event to the start
     * of the time window, during which a possible second tap can be detected.
     * The scale factor is 1.25ms/LSB. A value of 0 disables the double tap function.
     * It accepts a maximum value of 255.
     * @param doubleTapLatency integer between 0 and 255
     */
    public void setDoubleTapLatency(int doubleTapLatency){
        writeSingleByteToRegister(ADXL345_LATENT,doubleTapLatency);
    }
    /**
     *      Sets the Window register, which contains an unsigned time value representing
     *the amount of time after the expiration of the latency time (Latent register)
     *during which a second value tap can begin. The scale factor is 1.25ms/LSB. A
     *value of 0 disables the double tap function. The maximum value is 255.
     * @param doubleTapWindow integer between 0 and 255
     */
    public void setDoubleTapWindow(int doubleTapWindow){
        writeSingleByteToRegister(ADXL345_WINDOW,doubleTapWindow);
    }
    
    /**
     *     Sets the THRESH_ACT byte which holds the threshold value for detecting activity.
     *The data format is unsigned, so the magnitude of the activity event is compared
     *with the value is compared with the value in the THRESH_ACT register. The scale
     *factor is 62.5mg/LSB. A value of 0 may result in undesirable behavior if the
     *activity interrupt is enabled. The maximum value is 255.
     * @param activityThreshold integer between 0 and 255
     */
    public void setActivityThreshold(int activityThreshold){
        writeSingleByteToRegister(ADXL345_THRESH_ACT,activityThreshold);
    }
    
    /**
     *      Sets the THRESH_INACT byte which holds the threshold value for detecting inactivity.
     * The data format is unsigned, so the magnitude of the inactivity event is compared
     * with the value is compared with the value in the THRESH_INACT register. The scale
     * factor is 62.5mg/LSB. A value of 0 may result in undesirable behavior if the
     * inactivity interrupt is enabled. The maximum value is 255.
     * @param inactivityThreshold integer between 0 and 255
     */
    public void setInactivityThreshold(int inactivityThreshold){
        writeSingleByteToRegister(ADXL345_THRESH_INACT,inactivityThreshold);
    }
    
    /**
     *      Sets the TIME_INACT register, which contains an unsigned time value representing the
     *amount of time that acceleration must be less than the value in the THRESH_INACT
     *register for inactivity to be declared. The scale factor is 1sec/LSB. The value must
     *be between 0 and 255.
     * @param timeInactivity integer between 0 and 255
     */
    public void setTimeInactivity(int timeInactivity){
        writeSingleByteToRegister(ADXL345_TIME_INACT,timeInactivity);
    }
    
    
    /**
     *      Sets the THRESH_FF register which holds the threshold value, in an unsigned format, for
     * free-fall detection. The root-sum-square (RSS) value of all axes is calculated and
     * compared with the value in THRESH_FF to determine if a free-fall event occurred. The
     * scale factor is 62.5mg/LSB. A value of 0 may result in undesirable behavior if the free-fall
     * interrupt is enabled. The maximum value is 255.
     * @param freeFallThreshold integer between 0 and 255
     */
    public void setFreeFallThreshold(int freeFallThreshold){
        writeSingleByteToRegister(ADXL345_THRESH_FF,freeFallThreshold);
    }
    /**
     *      Sets the TIME_FF register, which holds an unsigned time value representing the minimum
     * time that the RSS value of all axes must be less than THRESH_FF to generate a free-fall
     * interrupt. The scale factor is 5ms/LSB. A value of 0 may result in undesirable behavior if
     * the free-fall interrupt is enabled. The maximum value is 255.
     * @param freeFallDuration integer between 0 and 255
     */
    public void setFreeFallDuration(int freeFallDuration){
        writeSingleByteToRegister(ADXL345_TIME_FF,freeFallDuration);
    }
    /**
     * Write a byte to the ADXL345_ACT_INACT_CTL register to enable/disable ACT/INACT on X,Y or Z axis
     * Details are specified in the datasheet
     * @param _b 
     */
    public void setACT_INACT_CTL_Reg(int _b){
        writeSingleByteToRegister(ADXL345_ACT_INACT_CTL,_b);
    }
    /**
     * Write a byte to the ADXL345_TAP_AXES register to enable/disable Tap detection on X,Y or Z axis
     * Details are specified in the datasheet
     * @param _b 
     */
    public void setTAP_AXES_Reg(int _b){
        writeSingleByteToRegister(ADXL345_TAP_AXES,_b);
    }
    
    /**
     * Write a byte to the ADXL345_INT_ENABLE register
     * Setting bits in this register to a value of 1 enables their respective functions to 
     * generate interrupts, whereas a value of 0 prevents the functions from generating interrupts.
     * The DATA_READY, watermark, and overrun bits enable only the interrupt output; the functions are always enabled. 
     * It is recommended that interrupts be configured before enabling their outputs.
     * @param _b 
     */
    public void setINT_ENABLE_Reg(int _b){
        writeSingleByteToRegister(ADXL345_INT_ENABLE,_b);
    }
    /**
     * Write a byte to the ADXL345_INT_MAP register
     * Any bits set to 0 in this register send their respective interrupts to the INT1 pin, 
     * whereas bits set to 1 send their respective interrupts to the INT2 pin. 
     * All selected interrupts for a given pin are OR’ed.
     * @param _b 
     */
    public void setINT_MAP_Reg(int _b){
        writeSingleByteToRegister(ADXL345_INT_MAP,_b);
    }
    /**
     * Write a byte to ADXL345_FIFO_CTL register
     * @param _b 
     */
    public void setFIFO_CTL_Reg(int _b){
        writeSingleByteToRegister(ADXL345_FIFO_CTL,_b);
    }
    /**
     *
     * @param backing
     * @param position
     * @param length
     * @param mask
     * @return array of 3 containing the X,Y,Z acceleration measurements
     */
    public short[] intepretData(byte[] backing, int position, int length, int mask){
        assert(length==6) : "Non-Accelerometer data passed into the NunchuckTwig class";
        short[] temp = {0,0,0};
        //format the data from the circular buffer backing[]
        
        temp[0] = (short)(((backing[(position+1)&mask]) << 8) | (backing[position&mask]&0xFF));
        temp[1] = (short)(((backing[(position+3)&mask]) << 8) | (backing[(position+2)&mask]&0xFF));
        temp[2] = (short)(((backing[(position+5)&mask]) << 8) | (backing[(position+4)&mask]&0xFF));
        
        return temp;
    }
    /**
     * write a byte to a register
     * @param register register to write to
     * @param value byte to write
     */
    public void writeSingleByteToRegister(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(ADXL345_DEVICE);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
}
