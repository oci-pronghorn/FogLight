package com.ociweb.device;

import com.ociweb.device.impl.EdisonConstants;

public class GroveShieldV2EdisonStageConfiguration {
    public boolean publishTime;
    
        
    public boolean configI2C;    //Humidity, LCD need I2C address so..
    public Connect[] encoderInputs; //Rotary Encoder
    public Connect[] digitalInputs; //Button, Motion
    public Connect[] analogInputs;  //Light, UV, Moisture
    public Connect[] digitalOutputs;//Relay Buzzer
    public Connect[] pwmOutputs;    //Servo   //(only 3, 5, 6, 9, 10, 11)

    public GroveShieldV2EdisonStageConfiguration(boolean publishTime, boolean configI2C, Connect[] encoderInputs,
            Connect[] digitalInputs, Connect[] digitalOutputs, Connect[] pwmOutputs, Connect[] analogInputs) {
        this.publishTime = publishTime;
        this.configI2C = configI2C;
        this.encoderInputs = encoderInputs;
        this.digitalInputs = digitalInputs;
        this.digitalOutputs = digitalOutputs;
        this.pwmOutputs = pwmOutputs;
        this.analogInputs = analogInputs;
    }

    static void findDup(Connect[] base, int baseLimit, Connect[] items, boolean mapAnalogs) {
        int i = items.length;
        while (--i>=0) {
            int j = baseLimit;
            while (--j>=0) {
                if (mapAnalogs ? base[j].connection ==  EdisonConstants.ANALOG_CONNECTOR_TO_PIN[items[i].connection] :  base[j]==items[i]) {
                    throw new UnsupportedOperationException("Connector "+items[i]+" is assigned more than once.");
                }
            }
        }     
    }

    public static Connect[] buildUsedLines(GroveShieldV2EdisonStageConfiguration config) {
        
        Connect[] result = new Connect[config.digitalInputs.length+
                                 config.encoderInputs.length+
                                 config.digitalOutputs.length+
                                 config.pwmOutputs.length+
                                 config.analogInputs.length+
                                 (config.configI2C?2:0)];
        
        int pos = 0;
        System.arraycopy(config.digitalInputs, 0, result, pos, config.digitalInputs.length);
        pos+=config.digitalInputs.length;
        
        if (0!=(config.encoderInputs.length&0x1)) {
            throw new UnsupportedOperationException("Rotery encoder requires two neighboring digital inputs.");
        }
        findDup(result,pos,config.encoderInputs, false);
        System.arraycopy(config.encoderInputs, 0, result, pos, config.encoderInputs.length);
        pos+=config.encoderInputs.length;
                
        findDup(result,pos,config.digitalOutputs, false);
        System.arraycopy(config.digitalOutputs, 0, result, pos, config.digitalOutputs.length);
        pos+=config.digitalOutputs.length;
        
        findDup(result,pos,config.pwmOutputs, false);
        System.arraycopy(config.pwmOutputs, 0, result, pos, config.pwmOutputs.length);
        pos+=config.pwmOutputs.length;        
        
        findDup(result,pos,config.analogInputs, true);
        int j = config.analogInputs.length;
        while (--j>=0) {
            result[pos++] = new Connect(config.analogInputs[j].twig,EdisonConstants.ANALOG_CONNECTOR_TO_PIN[config.analogInputs[j].connection]);
        }
        
        if (config.configI2C) {
            findDup(result,pos,EdisonConstants.i2cPins, false);
            System.arraycopy(EdisonConstants.i2cPins, 0, result, pos, EdisonConstants.i2cPins.length);
            pos+=EdisonConstants.i2cPins.length;
        }
    
        return result;
    }
}