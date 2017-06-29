/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.RTC;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import static com.ociweb.iot.grove.RTC.Grove_RTC_Constants.*;
/**
 *
 * @author huydo
 */
public class RTC {
    FogCommandChannel target;
    
    public RTC(FogCommandChannel ch){
        this.target = ch;
    }
    public void startClock(){
        
        writeSingleByteToRegister(target,DS1307_I2C_ADDRESS,TIME_REG,0x00);
        target.i2cFlushBatch();
        
    }
    public void stopClock(){
        
        writeSingleByteToRegister(target,DS1307_I2C_ADDRESS,TIME_REG,0x80);
        target.i2cFlushBatch();
        
    }
    
    public void setTime(int second,int minute,int hour,int dayOfWeek,int dayOfMonth,int month,int year){
        int[] time = {0,0,0,0,0,0,0};;
        time[0] = decToBcd(second);
        time[1] = decToBcd(minute);
        time[2] = decToBcd(hour);
        time[3] = decToBcd(dayOfWeek);
        time[4] = decToBcd(dayOfMonth);
        time[5] = decToBcd(month);
        time[6] = decToBcd(year);
        
        writeMultipleBytesToRegister(target,DS1307_I2C_ADDRESS,TIME_REG,time);
        target.i2cFlushBatch();
        
    }
    
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
    
    public int[] intepretData(byte[] backing, int position, int length, int mask){
        assert(length==7) : "Non-Accelerometer data passed into the NunchuckTwig class";
        int[] temp = {0,0,0,0,0,0,0};
        //format the data from the circular buffer backing[]
        
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
    
    
    
    private boolean writeSingleByteToRegister(FogCommandChannel ch,int address, int register, int value) {
        if (!ch.i2cIsReady()) {
            return false;
        }
        
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(address);
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        ch.i2cCommandClose();
        return true;
    }
    
    private static void writeMultipleBytesToRegister(FogCommandChannel ch, int address, int register, int[] values) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(address);
        
        i2cPayloadWriter.writeByte(register);
        for (int i = 0; i < values.length; i++) {
            i2cPayloadWriter.writeByte(values[i]);
        }
        
        ch.i2cCommandClose();
    }
}
