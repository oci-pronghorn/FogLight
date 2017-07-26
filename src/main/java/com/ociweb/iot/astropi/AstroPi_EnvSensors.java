/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.astropi;

import com.ociweb.iot.astropi.listeners.HumidityListener;
import com.ociweb.iot.astropi.listeners.PressureListener;
import com.ociweb.iot.astropi.listeners.TemperatureListener;
import com.ociweb.iot.astropi.listeners.AstroPiListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.transducer.I2CListenerTransducer;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class AstroPi_EnvSensors implements IODeviceTransducer,I2CListenerTransducer{
    private final FogCommandChannel target;
    
    public AstroPi_EnvSensors(FogCommandChannel ch,AstroPiListener... l){
        this.target = ch;
        //target.ensureI2CWriting(1000, 1000);
        for(AstroPiListener item:l){
            if(item instanceof TemperatureListener){
                this.tempListener = (TemperatureListener) item;
            }
            if(item instanceof PressureListener){
                this.pressureListener = (PressureListener) item;
            }
            if(item instanceof HumidityListener){
                this.humidityListener = (HumidityListener) item;
            }
        
        }
    }
    
    int CTRL_REG1_PVal = 0;
    int CTRL_REG1_HUMVal = 0;
    /**
     * Power up the humidity and/or the pressure sensor
     * Set the output data rate (ODR) of the pressure sensor to 1 Hz
     * Set the output data rate (ODR) of the humidity sensor to 1 Hz
     * @param humiditySensor
     * @param pressureSensor 
     */
    public void begin(boolean humiditySensor,boolean pressureSensor){
        if(pressureSensor){
            CTRL_REG1_PVal |= AstroPi_Constants.POWER_UP_P;
            CTRL_REG1_PVal |= AstroPi_Constants.ODR0_SET_P;
            LPS25HWriteByte(AstroPi_Constants.CTRL_REG1_P,CTRL_REG1_PVal);
        }
        if(humiditySensor){
            CTRL_REG1_HUMVal |= AstroPi_Constants.POWER_UP_HUM;
            CTRL_REG1_HUMVal |= AstroPi_Constants.ODR0_SET_HUM;
            HTS221WriteByte(AstroPi_Constants.CTRL_REG1_HUM,CTRL_REG1_HUMVal);
        }
    }
    
            /**
     * Convert the 2 bytes I2C read to the correct representation of the digital value
     * @param backing circular buffer containing data from I2C read
     * @param position index of the first byte
     * @param length length of the array
     * @param mask 
     * @return The converted digital value. 
     */
    private short interpretTwoBytes(byte[] backing, int position, int length, int mask){
        //format the data from the circular buffer backing[]
        
        short temp = (short)(((backing[(position+1)&mask] & 0xFF) << 8) | (backing[(position)&mask] & 0xFF));
        
        return temp;
    }
    
                /**
     * Convert the 3 bytes I2C read to the correct representation of the digital value
     * @param backing circular buffer containing data from I2C read
     * @param position index of the first byte
     * @param length length of the array
     * @param mask 
     * @return The converted digital value. 
     */
    private int interpretThreeBytes(byte[] backing, int position, int length, int mask){
        //format the data from the circular buffer backing[]
        
        int temp = (((backing[(position+2)&mask]&0xFF) << 16) | ((backing[(position+1)&mask]&0xFF) << 8) | (backing[(position)&mask]&0xFF));
        
        if (temp >= 8388608) temp -= 2 * 8388608;
        return temp;
    }
    
    private void HTS221WriteByte(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(AstroPi_Constants.HTS221_ADDRESS);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    private void LPS25HWriteByte(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(AstroPi_Constants.LPS25H_ADDRESS);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }

    private TemperatureListener tempListener;
    private PressureListener pressureListener;
    private HumidityListener humidityListener;
    
    private int _h0_rH,_h1_rH,_T0_degC,_T1_degC,_H0_T0,_H1_T0,_T0_OUT,_T1_OUT;
    private boolean calibratePressure = true;
    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        if(addr == AstroPi_Constants.LPS25H_ADDRESS){
            if(register == AstroPi_Constants.TEMP_L_REG_P){
                short data = this.interpretTwoBytes(backing, position, length, mask);
                double temp = 42.5 + (data/480.0);
                tempListener.temperatureVal(temp);
            }
            if(register == AstroPi_Constants.PRESSURE_XL_REG){
                int data = this.interpretThreeBytes(backing, position, length, mask);
                int pressure = data/4096;
                pressureListener.pressureVal(pressure);
            }
        }
        if(addr == AstroPi_Constants.HTS221_ADDRESS){
            if(register == AstroPi_Constants.CALIB_START){
                if(calibratePressure){
                    _h0_rH = backing[(position)&mask]&0xFF;
                    _h1_rH = backing[(position+1)&mask]&0xFF;
                    _T0_degC = backing[(position+2)&mask]&0xFF;
                    _T1_degC = backing[(position+3)&mask]&0xFF;
                    
                    int tmp = _T0_degC;
                    _T0_degC = ((backing[(position+5)&mask]&0xFF)&0x3)<<8;
                    _T0_degC |= tmp;
                    
                    tmp = _T1_degC;
                    _T1_degC = (((backing[(position+5)&mask]&0xFF)&0xc)>>2)<<8;
                    _T1_degC |= tmp;
                    
                    _H0_T0 = backing[(position+6)&mask]&0xFF;
                    _H0_T0 |= (backing[(position+7)&mask]&0xFF)<<8;
                    
                    _H1_T0 = backing[(position+10)&mask]&0xFF;
                    _H1_T0 |= (backing[(position+11)&mask]&0xFF)<<8;
                    
                    _T0_OUT = backing[(position+12)&mask]&0xFF;
                    _T0_OUT = (backing[(position+13)&mask]&0xFF)<<8;
                    
                    _T1_OUT = backing[(position+14)&mask]&0xFF;
                    _T1_OUT = (backing[(position+15)&mask]&0xFF)<<8;
                    System.out.println("Pressure sensor calibration complete.");
                    calibratePressure = false;
                }
            }
            if(register == AstroPi_Constants.HUMIDITY_L_REG){
                if(!calibratePressure){
                    int data = this.interpretTwoBytes(backing, position, length, mask);
                    double hum = (_h1_rH - _h0_rH)/2.0;
                    double h_temp = (double)((data - _H0_T0) * hum) / (double)(_H1_T0 - _H0_T0);
                    hum =  _h0_rH / 2.0;
                    humidityListener.humidityVal(hum+h_temp);
                }
            }
            if(register == AstroPi_Constants.TEMP_L_REG_HUM){
                if(!calibratePressure){
                    int data =  this.interpretTwoBytes(backing, position, length, mask);
                    double deg = (double)((_T1_degC) - (_T0_degC))/8.0;
                    double t_temp = (double)((data - _T0_OUT) * deg) / (double)(_T1_OUT - _T0_OUT);
                    deg    = (double)(_T0_degC) / 8.0;     // remove x8 multiple
                    humidityListener.tempFromHumidityVal(deg+t_temp);
                }
            }
        }
    }
    
}
