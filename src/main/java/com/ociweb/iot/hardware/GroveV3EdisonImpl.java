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
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.iot.hardware.Hardware;

public class GroveV3EdisonImpl extends Hardware {
	
	
	
    private HardConnection[] usedLines;
    private Hardware hardware;
    
    public GroveV3EdisonImpl(GraphManager gm, Pipe<GroveRequestSchema> ccToAdOut, Pipe<GoSchema> orderPipe, Pipe<I2CCommandSchema> i2cPayloadPipe) {
		PipeConfig<GoSchema> goPipesConfig = new PipeConfig<GoSchema>(GoSchema.instance, 64, 1024);
		PipeConfig<AcknowledgeSchema> ackPipesConfig = new PipeConfig<AcknowledgeSchema>(AcknowledgeSchema.instance, 64, 1024);
		PipeConfig<RawDataSchema> I2CToListenerConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
		PipeConfig<RawDataSchema> adInToListenerConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
		
		//Pipe<GoSchema>[] ccToTrafficJoiner = (Pipe<GoSchema>[]) Array.newInstance(orderPipe.getClass(), 1);
        //ccToTrafficJoiner[0] = orderPipe;
		Pipe<GoSchema> adGoPipe = new Pipe<GoSchema>(goPipesConfig);
		Pipe<GoSchema> i2cGoPipe = new Pipe<GoSchema>(goPipesConfig);
		//Pipe<GoSchema>[] goPipes = (Pipe<GoSchema>[]) Array.newInstance(orderPipe.getClass(), 3);
		Pipe<AcknowledgeSchema> i2cAckPipe = new Pipe<AcknowledgeSchema>(ackPipesConfig);
		Pipe<AcknowledgeSchema> adAckPipe = new Pipe<AcknowledgeSchema>(ackPipesConfig);
		//Pipe<AcknowledgeSchema>[] ackPipes = (Pipe<AcknowledgeSchema>[]) Array.newInstance(ackPipe.getClass(), 2);
		//Pipe<RawDataSchema> I2CToListener = new Pipe<RawDataSchema>(I2CToListenerConfig);
		//Pipe<RawDataSchema> adInToListener = new Pipe<RawDataSchema>(adInToListenerConfig);
		
		I2CJFFIStage i2cJFFIStage = new I2CJFFIStage(gm, i2cGoPipe, i2cPayloadPipe, i2cAckPipe, this); //TODO: add i2cListener pipe
		//AnalogDigitalInputStage adInputStage = new AnalogDigitalInputStage(gm, adInToListener, goPipes[2], this); //
		AnalogDigitalOutputStage adOutputStage = new AnalogDigitalOutputStage(gm, ccToAdOut,adGoPipe, adAckPipe, this);
		TrafficCopStage trafficCopStage = new TrafficCopStage(gm, PronghornStage.join(orderPipe), PronghornStage.join(i2cAckPipe, adAckPipe), PronghornStage.join(adGoPipe, i2cGoPipe));
		System.out.println("the Edison pipe setup stage is completed");
   }
    

    
    public void coldSetup() {
        usedLines = buildUsedLines();
        EdisonGPIO.ensureAllLinuxDevices(usedLines);
        hardware.beginPinConfiguration(); //TODO:Uncertain stay above/below setToKnownStateFromColdStart,Will trial and error
        setToKnownStateFromColdStart();  
		for (int i = 0; i < hardware.digitalOutputs.length; i++) {
			if(hardware.digitalOutputs[i].type.equals(ConnectionType.Direct))EdisonGPIO.configDigitalOutput(hardware.digitalOutputs[i].connection);//config for writeBit
			System.out.println("configured output "+hardware.digitalOutputs[i].twig+" on connection "+hardware.digitalOutputs[i].connection);
		}
		for (int i = 0; i < hardware.pwmOutputs.length; i++) {
			if(hardware.pwmOutputs[i].type.equals(ConnectionType.Direct)) EdisonGPIO.configPWM(hardware.pwmOutputs[i].connection); //config for pwm
		}
		for (int i = 0; i < hardware.digitalInputs.length; i++) {
			if(hardware.digitalInputs[i].type.equals(ConnectionType.Direct))EdisonGPIO.configDigitalInput(hardware.digitalInputs[i].connection); //config for readBit
		}
		for (int i = 0; i < hardware.analogInputs.length; i++) {
			if(hardware.analogInputs[i].type.equals(ConnectionType.Direct)) EdisonGPIO.configAnalogInput(hardware.analogInputs[i].connection); //config for readInt
		}
		EdisonGPIO.configI2C();
		hardware.endPinConfiguration();//Tri State set high to end configuration
    }
   
    
//    public void coldSetupI2C() {
//        usedLines = buildUsedLines();
//        EdisonGPIO.ensureAllLinuxDevices(usedLines);
//        setToKnownStateFromColdStart();  
//		for (int i = 0; i < hardware.i2c; i++) {
//			if(hardware.digitalOutputs[i].type.equals(ConnectionType.Direct))hardware.configurePinsForDigitalOutput(hardware.digitalOutputs[i].connection);
//		}
//		for (int i = 0; i < hardware.pwmOutputs.length; i++) {
//			if(hardware.pwmOutputs[i].type.equals(ConnectionType.Direct)) hardware.configurePinsForAnalogOutput(hardware.pwmOutputs[i].connection);
//		}
//		
//    }

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
    
	@Override
	public byte getI2CConnector() {
		// TODO Auto-generated method stub
		return 6;
	}

}
