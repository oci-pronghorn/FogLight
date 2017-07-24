/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.real_time_clock;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import static com.ociweb.iot.grove.real_time_clock.RTC_Constants.*;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.IODeviceTransducer;
/**
 *
 * @author huydo
 */
public class RTC_Transducer implements IODeviceTransducer,I2CListener {
    private final FogCommandChannel target;
    private RTCListener listener;
    public RTC_Transducer(FogCommandChannel ch){
        this.target = ch;
    }
    public RTC_Transducer(FogCommandChannel ch,RTCListener l){
        this.target = ch;
        this.listener = l;
    }
    /**
     * Stop the clock.
     */
    public void stopClock(){
        
        writeSingleByteToRegister(TIME_REG,0x80);
        
    }
    /**
     * Set the time of the clock.
     * @param second
     * @param minute
     * @param hour
     * @param dayOfWeek Monday = 1, Tuesday = 2,.. Sunday = 7
     * @param dayOfMonth
     * @param month
     * @param year 
     */
    public void setTime(int second,int minute,int hour,int dayOfWeek,int dayOfMonth,int month,int year){
        int[] time = {0,0,0,0,0,0,0};
        year = year - 2000;
        time[0] = decToBcd(second);
        time[1] = decToBcd(minute);
        time[2] = decToBcd(hour);
        time[3] = decToBcd(dayOfWeek);
        time[4] = decToBcd(dayOfMonth);
        time[5] = decToBcd(month);
        time[6] = decToBcd(year);
        
        writeMultipleBytesToRegister(TIME_REG,time);
        
    }
    /**
     * Print the time interpreted from the backing[] array.
     * @param temp 
     */
    public void printTime(int[] temp){
        StringBuilder indicator = new StringBuilder();
        
        indicator.append("The current time is:  ");
        indicator.append(parseWeekday(temp[3]));
        indicator.append(", ");
        indicator.append(temp[5]);
        indicator.append("/");
        indicator.append(temp[4]);
        indicator.append("/");
        indicator.append(2000+temp[6]);
        
        indicator.append("  ");
        indicator.append(temp[2]);
        indicator.append(":");
        indicator.append(temp[1]);
        indicator.append(":");
        indicator.append(temp[0]);
        
        System.out.println(indicator);
    }
    
    private String parseWeekday(int day){
        switch(day){
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            case 7:
                return "Sunday";
            default:
                return "No such day";
                
        }
    }
    /**
     * Format the data from the circular buffer backing[].
     * @param backing circular buffer containing data from I2C read
     * @param position index of the first byte
     * @param length
     * @param mask
     * @return array of 7 time parameters, following this order:
     * second, minute, hour, dayofWeek, dateofMonth, month, year
     */
    
    public int[] interpretData(byte[] backing, int position, int length, int mask){
        assert(length==7) : "Non-Accelerometer data passed into the NunchuckTwig class";
        int[] temp = {0,0,0,0,0,0,0};
        
        temp[0] = bcdToDec(backing[position&mask] & 0x7F); //second
        temp[1] = bcdToDec(backing[(position+1)&mask] & 0x7F);
        temp[2] = bcdToDec(backing[(position+2)&mask]);
        temp[3] = bcdToDec(backing[(position+3)&mask]);
        temp[4] = bcdToDec(backing[(position+4)&mask]);
        temp[5] = bcdToDec(backing[(position+5)&mask]);
        temp[6] = bcdToDec(backing[(position+6)&mask]);
        
        return temp;
    }
    
    int decToBcd(int val)
    {
        return ((val/10*16) + (val%10));
    }
    
//Convert binary coded decimal to normal decimal numbers
    int bcdToDec(int val)
    {
        return ((val/16*10) + (val%16));
    }
    
    
        /**
     * write a byte to a register
     * @param register register to write to
     * @param value byte to write
     */
    public void writeSingleByteToRegister(int register, int value) {
        
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(DS1307_I2C_ADDRESS);
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    
    public void writeMultipleBytesToRegister(int register, int[] values) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(DS1307_I2C_ADDRESS);
        
        i2cPayloadWriter.writeByte(register);
        for (int i = 0; i < values.length; i++) {
            i2cPayloadWriter.writeByte(values[i]);
        }
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }

    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        listener.clockVals(this.interpretData(backing, position, length, mask));
    }
}
