/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.I2C_ADC;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;

/**
 *
 * @author huydo
 */
public class I2C_ADC_Facade implements IODeviceFacade{
    FogCommandChannel target;
    
    public I2C_ADC_Facade(FogCommandChannel ch){
        this.target = ch;
    }
    
    public short intepretData(byte[] backing, int position, int length, int mask){
        //format the data from the circular buffer backing[]
        
        short temp = (short)(((backing[(position+1)&mask]) << 8) | (backing[position&mask]));
        
        return temp;
    }
    
}
