/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.three_axis_accelerometer_16g;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import static com.ociweb.iot.grove.three_axis_accelerometer_16g.ThreeAxisAccelerometer_16g_Constants.*;

import com.ociweb.gl.api.transducer.StartupListenerTransducer;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.transducer.I2CListenerTransducer;

/**
 *
 * @author huydo
 */
public class ThreeAxisAccelerometer_16g_Transducer implements IODeviceTransducer,I2CListenerTransducer,StartupListenerTransducer {    
    private final FogCommandChannel target;
    private AccelValsListener accellistener;
    private ActTapListener acttaplistener;
    private AccelInterruptListener interrlistener;

    public ThreeAxisAccelerometer_16g_Transducer(FogCommandChannel ch, ThreeAxisAccelerometer_16gListener ... l){
        this.target = ch;
        target.ensureI2CWriting(50, 4);
        for(ThreeAxisAccelerometer_16gListener item:l){
            if(item instanceof AccelValsListener){
                this.accellistener = (AccelValsListener) item;
            }
            if(item instanceof ActTapListener){
                this.acttaplistener = (ActTapListener) item;
            }
            if(item instanceof AccelInterruptListener){
                this.interrlistener =  (AccelInterruptListener) item;
            }
        }
        
    }
    public ThreeAxisAccelerometer_16g_Transducer(FogCommandChannel ch){
        this.target = ch;
        target.ensureI2CWriting(50, 4);
    }
    
    
    public void registerListener(ThreeAxisAccelerometer_16gListener ... l){
        for(ThreeAxisAccelerometer_16gListener item:l){
            if(item instanceof AccelValsListener){
                this.accellistener = (AccelValsListener) item;
            }
            if(item instanceof ActTapListener){
                this.acttaplistener = (ActTapListener) item;
            }
            if(item instanceof AccelInterruptListener){
                this.interrlistener =  (AccelInterruptListener) item;
            }
        }
    }
    
    
    
    @Override
    public void startup() { //by default, the accelerometer start with range = +/- 2g, ODR = 800 Hz
        this.powerOn();
        this.setRange(2);
        this.setRate(800);
    }
    /**
     * Start the device in measurement mode, with auto-sleep disabled and sleep mode disabled
     */
    public void powerOn() {
        
        axWriteByte(ThreeAxisAccelerometer_16g_Constants.ADXL345_POWER_CTL,0);
        
        axWriteByte(ThreeAxisAccelerometer_16g_Constants.ADXL345_POWER_CTL,16);
        
        axWriteByte(ThreeAxisAccelerometer_16g_Constants.ADXL345_POWER_CTL,8);        
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
        axWriteByte(ADXL345_DATA_FORMAT,_s);
        
    } 
    /**
     * Sets the OFSX, OFSY and OFSZ bytes
     * x, y and z are user offset adjustments in 8-bit twos complement format with
     * a scale factor of 15.6mg/LSB
     * @param x
     * @param y
     * @param z
     */
    public void setAxisOffset(int x, int y, int z) {
        
        axWriteByte(ADXL345_OFSX,(byte) x);
        
        axWriteByte(ADXL345_OFSY,(byte) y);
        
        axWriteByte(ADXL345_OFSZ,(byte) z);
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
        axWriteByte(ADXL345_BW_RATE,_s);
        
        
    }
    /**
     * Sets the THRESH_TAP byte value
     * it should be between 0 and 255
     * the scale factor is 62.5 mg/LSB
     * A value of 0 may result in undesirable behavior
     * @param tapThreshold integer between 0 and 255
     */
    public void setTapThreshold(int tapThreshold){
        axWriteByte(ADXL345_THRESH_TAP,tapThreshold);
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
        axWriteByte(ADXL345_DUR,tapDuration);
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
        axWriteByte(ADXL345_LATENT,doubleTapLatency);
    }
    /**
     *      Sets the Window register, which contains an unsigned time value representing
the amount of time after the expiration of the latency time (Latent register)
during which a second value tap can powerOn. The scale factor is 1.25ms/LSB. A
     *value of 0 disables the double tap function. The maximum value is 255.
     * @param doubleTapWindow integer between 0 and 255
     */
    public void setDoubleTapWindow(int doubleTapWindow){
        axWriteByte(ADXL345_WINDOW,doubleTapWindow);
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
        axWriteByte(ADXL345_THRESH_ACT,activityThreshold);
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
        axWriteByte(ADXL345_THRESH_INACT,inactivityThreshold);
    }  
    /**
     *      Sets the TIME_INACT register, which contains an unsigned time value representing the
     *amount of time that acceleration must be less than the value in the THRESH_INACT
     *register for inactivity to be declared. The scale factor is 1sec/LSB. The value must
     *be between 0 and 255.
     * @param timeInactivity integer between 0 and 255
     */
    public void setTimeInactivity(int timeInactivity){
        axWriteByte(ADXL345_TIME_INACT,timeInactivity);
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
        axWriteByte(ADXL345_THRESH_FF,freeFallThreshold);
    }
    /**
     *      Sets the TIME_FF register, which holds an unsigned time value representing the minimum
     * time that the RSS value of all axes must be less than THRESH_FF to generate a free-fall
     * interrupt. The scale factor is 5ms/LSB. A value of 0 may result in undesirable behavior if
     * the free-fall interrupt is enabled. The maximum value is 255.
     * @param freeFallDuration integer between 0 and 255
     */
    public void setFreeFallDuration(int freeFallDuration){
        axWriteByte(ADXL345_TIME_FF,freeFallDuration);
    }
    
    private int ACT_INACT_CTLVal = 0b00000000;
    /**
     * Specify activity detection behavior on x,y,z
     * @param AC true = ac-coupled operation, false = dc-coupled operation
     * @param actx enable activity detection on x axis
     * @param acty enable activity detection on y axis 
     * @param actz enable activity detection on z axis
     */
    public void configureActivityDetection(boolean AC,boolean actx,boolean acty,boolean actz){
        ACT_INACT_CTLVal |= AC?0b10000000:ACT_INACT_CTLVal;
        ACT_INACT_CTLVal |= actx?0b01000000:ACT_INACT_CTLVal;
        ACT_INACT_CTLVal |= acty?0b00100000:ACT_INACT_CTLVal;
        ACT_INACT_CTLVal |= actz?0b00010000:ACT_INACT_CTLVal;
        axWriteByte(ADXL345_ACT_INACT_CTL,ACT_INACT_CTLVal);
    }
    /**
     * Specify inactivity detection behavior on x,y,z
     * @param AC true = ac-coupled operation, false = dc-coupled operation
     * @param inactx true = enable inactivity detection on x axis
     * @param inacty true = enable inactivity detection on y axis 
     * @param inactz true = enable inactivity detection on z axis
     */
    public void configureInactivityDetection(boolean AC,boolean inactx,boolean inacty,boolean inactz){
        ACT_INACT_CTLVal |= AC?0b00001000:ACT_INACT_CTLVal;
        ACT_INACT_CTLVal |= inactx?0b00000100:ACT_INACT_CTLVal;
        ACT_INACT_CTLVal |= inacty?0b00000010:ACT_INACT_CTLVal;
        ACT_INACT_CTLVal |= inactz?0b00000001:ACT_INACT_CTLVal;
        axWriteByte(ADXL345_ACT_INACT_CTL,ACT_INACT_CTLVal);
    }

    private int TAP_AXESVal = 0;
    /**
     * Specify tap detection behavior on x,y,z
     * @param suppress true = set the suppress bit
     * @param tapx true = enable tap detection on x
     * @param tapy true = enable tap detection on y
     * @param tapz true = enable tap detection on z 
     */
    public void configureTapDetection(boolean suppress,boolean tapx,boolean tapy,boolean tapz){
        TAP_AXESVal |= suppress?0b00001000:TAP_AXESVal;
        TAP_AXESVal |= tapx?0b00000100:TAP_AXESVal;
        TAP_AXESVal |= tapy?0b00000010:TAP_AXESVal;
        TAP_AXESVal |= tapz?0b00000001:TAP_AXESVal;
        axWriteByte(ADXL345_TAP_AXES,TAP_AXESVal);
    }
    private int INT_ENABLEVal = 0;
    
    public void enableSingleTapInterrupt(){
        INT_ENABLEVal |= 0b01000000;
        writeINT_ENABLE_Reg(INT_ENABLEVal);
    }
    
    public void enableDoubleTapInterrupt(){
        INT_ENABLEVal |= 0b00100000;
        writeINT_ENABLE_Reg(INT_ENABLEVal);
    }
    
    public void enableActivityInterrupt(){
        INT_ENABLEVal |= 0b00010000;
        writeINT_ENABLE_Reg(INT_ENABLEVal);
    }
    
    public void enableInactivityInterrupt(){
        INT_ENABLEVal |= 0b00001000;
        writeINT_ENABLE_Reg(INT_ENABLEVal);
    }
    
    public void enableFreeFallInterrupt(){
        INT_ENABLEVal |= 0b00000100;
        writeINT_ENABLE_Reg(INT_ENABLEVal);
    }
    
    /**
     * Write a byte to the ADXL345_INT_ENABLE register
     * Setting bits in this register to a value of 1 enables their respective functions to 
     * generate interrupts, whereas a value of 0 prevents the functions from generating interrupts.
     * The DATA_READY, watermark, and overrun bits enable only the interrupt output; the functions are always enabled. 
     * It is recommended that interrupts be configured before enabling their outputs.
     * @param _b 
     */
    public void writeINT_ENABLE_Reg(int _b){
        axWriteByte(ADXL345_INT_ENABLE,_b);
    }
    /**
     * Write a byte to the ADXL345_INT_MAP register
     * Any bits set to 0 in this register send their respective interrupts to the INT1 pin, 
     * whereas bits set to 1 send their respective interrupts to the INT2 pin. 
     * All selected interrupts for a given pin are OR’ed.
     * @param _b 
     */
    public void writeINT_MAP_Reg(int _b){
        axWriteByte(ADXL345_INT_MAP,_b);
    }
    /**
     * Write a byte to ADXL345_FIFO_CTL register
     * @param _b 
     */
    public void writeFIFO_CTL_Reg(int _b){
        axWriteByte(ADXL345_FIFO_CTL,_b);
    }
    
/**
     * Convert the 6 bytes from I2C read to the correct two's complement representation of X,Y,Z
     * @param backing circular buffer containing data from I2C read
     * @param position index of the first byte
     * @param length length of the array
     * @param mask
     * @return array of 3 X,Y,Z values ,where array[0] = X, array[1] = Y
     */
    private short[] interpretData(byte[] backing, int position, int length, int mask){
        assert(length==6) : "Non-Accelerometer data passed into the class";
        short[] temp = {0,0,0};
        //format the data from the circular buffer backing[]
        
        temp[0] = (short)(((backing[(position+1)&mask]&0xFF) << 8) | (backing[position&mask]&0xFF));
        temp[1] = (short)(((backing[(position+3)&mask]&0xFF) << 8) | (backing[(position+2)&mask]&0xFF));
        temp[2] = (short)(((backing[(position+5)&mask]&0xFF) << 8) | (backing[(position+4)&mask]&0xFF));
        
        return temp;
    }
    /**
     * write a byte to a register
     * @param register register to write to
     * @param value byte to write
     */
    private void axWriteByte(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(ADXL345_DEVICE);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }

    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        if(addr == ADXL345_DEVICE){
            if(register == ADXL345_DATAX0){
                short[] xyzVals = this.interpretData(backing, position, length, mask);
                accellistener.accelerationValues(xyzVals[0]*4, xyzVals[1]*4, xyzVals[2]*4);
            }
            if(register == ADXL345_ACT_TAP_STATUS){
                int actX = (backing[position] & 0b01000000)>>6;
                int actY = (backing[position] & 0b00100000)>>5;
                int actZ = (backing[position] & 0b00010000)>>4;
                acttaplistener.activityStatus(actX, actY, actZ);
                int tapX = (backing[position] & 0b00000100)>>2;
                int tapY = (backing[position] & 0b00000010)>>1;
                int tapZ = (backing[position] & 0b00000001); 
                acttaplistener.tapStatus(tapX, tapY, tapZ);
            }
            if(register == ADXL345_INT_SOURCE){
                int singletap = (backing[position] & 0b01000000)>>6;
                int doubletap = (backing[position] & 0b00100000)>>5;
                int activity =  (backing[position] & 0b00010000)>>4;
                int inactivity =(backing[position] & 0b00001000)>>3;
                int freefall = (backing[position] & 0b00000100)>>2;
                interrlistener.AccelInterruptStatus(singletap, doubletap, activity, inactivity, freefall);
            }
        }
    }

}
