package com.ociweb.iot.hardware;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.ociweb.iot.hardware.HardConnection.ConnectionType;
import com.ociweb.iot.hardware.impl.edison.EdisonConstants;
import com.ociweb.iot.hardware.impl.edison.EdisonGPIO;
import com.ociweb.iot.hardware.impl.edison.EdisonPinManager;
import com.ociweb.pronghorn.TrafficCopStage;
import com.ociweb.pronghorn.iot.schema.AcknowledgeSchema;
import com.ociweb.pronghorn.iot.schema.GoSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.iot.hardware.Hardware;

public class GroveV3EdisonImpl extends Hardware {
	
	
	
    private HardConnection[] usedLines;
    
    public GroveV3EdisonImpl(GraphManager gm, Pipe<GroveRequestSchema> ccToAdOut, Pipe<GoSchema> orderPipe, Pipe<I2CCommandSchema> i2cPayloadPipe) {
		PipeConfig<GoSchema> goPipesConfig = new PipeConfig<GoSchema>(GoSchema.instance, 64, 1024);
		PipeConfig<AcknowledgeSchema> ackPipesConfig = new PipeConfig<AcknowledgeSchema>(AcknowledgeSchema.instance, 64, 1024);;
		PipeConfig<RawDataSchema> I2CToListenerConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);;
		PipeConfig<RawDataSchema> adInToListenerConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);;
		
		Pipe<GoSchema>[] ccToTrafficJoiner = (Pipe<GoSchema>[]) Array.newInstance(orderPipe.getClass(), 1);
        ccToTrafficJoiner[0] = orderPipe;
		Pipe<GoSchema>[] goPipes = (Pipe<GoSchema>[]) Array.newInstance(orderPipe.getClass(), 3);
		Pipe<AcknowledgeSchema> ackPipe = new Pipe<AcknowledgeSchema>(ackPipesConfig);
		Pipe<AcknowledgeSchema>[] ackPipes = (Pipe<AcknowledgeSchema>[]) Array.newInstance(ackPipe.getClass(), 2);
		Pipe<RawDataSchema> I2CToListener = new Pipe<RawDataSchema>(I2CToListenerConfig);
		Pipe<RawDataSchema> adInToListener = new Pipe<RawDataSchema>(adInToListenerConfig);
		
		I2CJFFIStage i2cJFFIStage = new I2CJFFIStage(gm, goPipes[1], i2cPayloadPipe, ackPipes[1], this); //TODO: add i2cListener pipe
		AnalogDigitalInputStage adInputStage = new AnalogDigitalInputStage(gm, adInToListener, goPipes[2], this);
		AnalogDigitalOutputStage adOutputStage = new AnalogDigitalOutputStage(gm, ccToAdOut, goPipes[0], ackPipes[0], this);
		TrafficCopStage trafficCopStage = new TrafficCopStage(gm, ccToTrafficJoiner, ackPipes, goPipes);
	}
//    public public GroveV3EdisonImpl(boolean publishTime, boolean configI2C, HardConnection[] encoderInputs,
//			HardConnection[] digitalInputs, HardConnection[] digitalOutputs, HardConnection[] pwmOutputs, HardConnection[] analogInputs, GraphManager gm) {
//		super(publishTime, configI2C, encoderInputs, digitalInputs, digitalOutputs, pwmOutputs, analogInputs);
//		PipeConfig<RawDataSchema> ccToAdOutConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
//		PipeConfig<RawDataSchema> ccToTrafficConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
//		PipeConfig<RawDataSchema> ccToI2CConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
//		PipeConfig<GoSchema> goPipesConfig = new PipeConfig<GoSchema>(GoSchema.instance, 64, 1024);
//		PipeConfig<AcknowledgeSchema> ackPipesConfig = new PipeConfig<AcknowledgeSchema>(AcknowledgeSchema.instance, 64, 1024);;
//		PipeConfig<RawDataSchema> I2CToListenerConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);;
//		PipeConfig<RawDataSchema> adInToListenerConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);;
//		
//		Pipe<RawDataSchema> ccToAdOut = new Pipe<RawDataSchema>(ccToAdOutConfig);
//		Pipe<RawDataSchema> ccToTraffic = new Pipe<RawDataSchema>(ccToTrafficConfig);
//		Pipe<RawDataSchema>[] ccToTrafficJoiner = (Pipe<RawDataSchema>[]) Array.newInstance(ccToTrafficConfig.getClass(), 1);
//        ccToTrafficJoiner[0] = ccToTraffic;
//		Pipe<RawDataSchema> ccToI2C = new Pipe<RawDataSchema>(ccToI2CConfig);
//		Pipe<GoSchema> goPipe = new Pipe<GoSchema>(goPipesConfig);
//		int length = 3;
//		Pipe<GoSchema>[] goPipes = (Pipe<GoSchema>[]) Array.newInstance(goPipe.getClass(), length);
//		Pipe<AcknowledgeSchema> ackPipe = new Pipe<AcknowledgeSchema>(ackPipesConfig);
//		length = 2; 
//		Pipe<AcknowledgeSchema>[] ackPipes = (Pipe<AcknowledgeSchema>[]) Array.newInstance(ackPipe.getClass(), length);
//		Pipe<RawDataSchema> I2CToListener = new Pipe<RawDataSchema>(I2CToListenerConfig);
//		Pipe<RawDataSchema> adInToListener = new Pipe<RawDataSchema>(adInToListenerConfig);
//		
//		this.i2cJFFIStage = new I2CJFFIStage(gm, goPipes[1], ccToI2C, ackPipes[1], I2CToListener, this);
//		this.adInputStage = new AnalogDigitalInputStage(gm, adInToListener, goPipes[2], this);
//		this.adOutputStage = new AnalogDigitalOutputStage(gm, ccToAdOut, goPipes[0], ackPipes[0], this);
//		this.trafficCopStage = new TrafficCopStage(gm, ccToTrafficJoiner, ackPipes, goPipes);
//
//	}

    public void coldSetup() {
        usedLines = buildUsedLines();
        EdisonGPIO.ensureAllLinuxDevices(usedLines);
        setToKnownStateFromColdStart();  
        
    }
    

    //only used in startup
    private void pause() {
        try {
          //  Thread.sleep(NS_PAUSE/1_000_000,NS_PAUSE%1_000_000);
            Thread.sleep(35); //timeout for SMBus
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    
    
    public void cleanup() {
        EdisonGPIO.removeAllLinuxDevices(usedLines);
    }

    
    private void setToKnownStateFromColdStart() {
        //critical for the analog connections
        EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(10);
        EdisonGPIO.gpioOutputEnablePins.setValueHigh(10);
        EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(11);
        EdisonGPIO.gpioOutputEnablePins.setValueHigh(11);
        EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(12);
        EdisonGPIO.gpioOutputEnablePins.setValueHigh(12);
        EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(13);
        EdisonGPIO.gpioOutputEnablePins.setValueHigh(13);
    }
    public void configurePinsForDigitalInput(byte connection) {
        EdisonGPIO.configDigitalInput(connection); //config for readBit
    }

    public void configurePinsForAnalogInput(byte connection) {
        EdisonGPIO.configAnalogInput(connection);  //config for readInt
    }
    
    public void configurePinsForDigitalOutput(byte connection){
    	EdisonGPIO.configDigitalOutput(connection); //config for writeBit
    }   

    @Override
    public void configurePinsForAnalogOutput(byte connection) {
        EdisonGPIO.configPWM(connection); //config for pwm
    }


    
    public void configurePinsForI2C() {
        EdisonGPIO.configI2C();
    }
    
    public void i2cDataIn() {
        EdisonGPIO.configI2CDataIn();
    }
    
    public void i2cDataOut() {
        EdisonGPIO.configI2CDataOut();
    }
    
    public void i2cClockIn() {
        EdisonGPIO.configI2CClockIn();
    }
    
    public void i2cClockOut() {
        EdisonGPIO.configI2CClockOut();
    }

    public boolean i2cReadAck() {
        return EdisonPinManager.analogRead(EdisonConstants.DATA_RAW_VOLTAGE) < EdisonConstants.HIGH_LINE_VOLTAGE_MARK;

    }
    public boolean i2cReadDataBool() {
        return EdisonPinManager.analogRead(EdisonConstants.DATA_RAW_VOLTAGE) > EdisonConstants.HIGH_LINE_VOLTAGE_MARK;
    }
    
    public boolean i2cReadClockBool() {
        return EdisonPinManager.analogRead(EdisonConstants.CLOCK_RAW_VOLTAGE) > EdisonConstants.HIGH_LINE_VOLTAGE_MARK;
    }

    public void beginPinConfiguration() {
        super.beginPinConfiguration();        
        EdisonGPIO.shieldControl.setDirectionLow(0);
    }
    
    public void endPinConfiguration() {
        EdisonGPIO.shieldControl.setDirectionHigh(0);
        super.endPinConfiguration();
    }

    public int digitalRead(int connector) {        
        return EdisonPinManager.digitalRead(connector);
    }

    public int analogRead(int connector) {
        return EdisonPinManager.analogRead(connector);
    }    

    boolean xx = false; //TODO: this is a total hack until we talk to Alex to resolve.
    
    @Override
    public void analogWrite(int connector, int value) {
        
       if (!xx) { 
           // works with this method 
           EdisonPinManager.writePWMPeriod(connector, 1_000_000); //no smaller
           xx = true;
       }
       
       EdisonPinManager.writePWMDuty(connector, value);
    }

    
	public void digitalWrite(int connector, int value) {
	    assert(0==value || 1==value);    
	    EdisonPinManager.digitalWrite(connector, value, EdisonGPIO.gpioLinuxPins);
	}
	
    public void i2cSetClockLow() {
        EdisonPinManager.writeValue(EdisonPinManager.I2C_CLOCK, EdisonPinManager.I2C_LOW, EdisonGPIO.gpioLinuxPins);
    }

    public void i2cSetClockHigh() {
        EdisonPinManager.writeValue(EdisonPinManager.I2C_CLOCK, EdisonPinManager.I2C_HIGH, EdisonGPIO.gpioLinuxPins);
    }

    public void i2cSetDataLow() {
        EdisonPinManager.writeValue(EdisonPinManager.I2C_DATA, EdisonPinManager.I2C_LOW, EdisonGPIO.gpioLinuxPins);
    }

    public void i2cSetDataHigh() {
        EdisonPinManager.writeValue(EdisonPinManager.I2C_DATA, EdisonPinManager.I2C_HIGH, EdisonGPIO.gpioLinuxPins);
    }

    public int i2cReadData() {
        return digitalRead(EdisonPinManager.I2C_DATA);
    }

    public int i2cReadClock() {
        return digitalRead(EdisonPinManager.I2C_CLOCK);
    }
    
    static void findDup(HardConnection[] base, int baseLimit, HardConnection[] items, boolean mapAnalogs) {
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

    public HardConnection[] buildUsedLines() {
        
        HardConnection[] result = new HardConnection[digitalInputs.length+
                                 multiBitInputs.length+
                                 digitalOutputs.length+
                                 pwmOutputs.length+
                                 analogInputs.length+
                                 (configI2C?2:0)];
        
        int pos = 0;
        System.arraycopy(digitalInputs, 0, result, pos, digitalInputs.length);
        pos+=digitalInputs.length;
        
        if (0!=(multiBitInputs.length&0x1)) {
            throw new UnsupportedOperationException("Rotery encoder requires two neighboring digital inputs.");
        }
        findDup(result,pos,multiBitInputs, false);
        System.arraycopy(multiBitInputs, 0, result, pos, multiBitInputs.length);
        pos+=multiBitInputs.length;
                
        findDup(result,pos,digitalOutputs, false);
        System.arraycopy(digitalOutputs, 0, result, pos, digitalOutputs.length);
        pos+=digitalOutputs.length;
        
        findDup(result,pos,pwmOutputs, false);
        System.arraycopy(pwmOutputs, 0, result, pos, pwmOutputs.length);
        pos+=pwmOutputs.length;        
        
        findDup(result,pos,analogInputs, true);
        int j = analogInputs.length;
        while (--j>=0) {
            result[pos++] = new HardConnection(analogInputs[j].twig,(int) EdisonConstants.ANALOG_CONNECTOR_TO_PIN[analogInputs[j].connection],ConnectionType.Direct);
        }
        
        if (configI2C) {
            findDup(result,pos,EdisonConstants.i2cPins, false);
            System.arraycopy(EdisonConstants.i2cPins, 0, result, pos, EdisonConstants.i2cPins.length);
            pos+=EdisonConstants.i2cPins.length;
        }
    
        return result;
    }

}
