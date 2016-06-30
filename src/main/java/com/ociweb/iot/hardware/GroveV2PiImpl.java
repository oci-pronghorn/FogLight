package com.ociweb.iot.hardware;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.impl.grovepi.GrovePiConstants;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.PiCommandChannel;
import com.ociweb.pronghorn.TrafficCopStage;
import com.ociweb.pronghorn.iot.schema.AcknowledgeSchema;
import com.ociweb.pronghorn.iot.schema.GoSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;


public class GroveV2PiImpl extends Hardware {

	private final int SLEEP_RATE_NS = 20000000;

	private static final Logger logger = LoggerFactory.getLogger(GroveV2PiImpl.class);
	
	private GraphManager gm;
	

	public GroveV2PiImpl(GraphManager gm) {
		this.gm = gm;
	}

//	public GroveV2PiImpl(boolean publishTime, boolean configI2C, HardConnection[] encoderInputs,
//			HardConnection[] digitalInputs, HardConnection[] digitalOutputs, HardConnection[] pwmOutputs, HardConnection[] analogInputs, GraphManager gm) {
//		super(publishTime, configI2C, encoderInputs, digitalInputs, digitalOutputs, pwmOutputs, analogInputs);
//	}

	@Override
	public void buildStages(Pipe<GroveRequestSchema>[] requestPipes, Pipe<I2CCommandSchema>[] i2cPipes, 
			Pipe<GroveResponseSchema>[] responsePipes, Pipe<GoSchema>[] orderPipes) {
		
		if(i2cPipes == null){
			throw new UnsupportedOperationException("Pi must have I2C enabled to work. Add c.useI2C(); to specifyConnections method.");
		}
		PipeConfig<GoSchema> goPipesConfig = new PipeConfig<GoSchema>(GoSchema.instance, 64, 1024);
		PipeConfig<AcknowledgeSchema> ackPipesConfig = new PipeConfig<AcknowledgeSchema>(AcknowledgeSchema.instance, 64, 1024);
		PipeConfig<RawDataSchema> I2CToListenerConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
		PipeConfig<RawDataSchema> adInToListenerConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
		
		
		Pipe<GoSchema> adGoPipe = new Pipe<GoSchema>(goPipesConfig);
		Pipe<GoSchema> i2cGoPipe = new Pipe<GoSchema>(goPipesConfig);
		//Pipe<GoSchema> listenerGoPipe = new Pipe<GoSchema>(goPipesConfig);
		Pipe<AcknowledgeSchema> i2cAckPipe = new Pipe<AcknowledgeSchema>(ackPipesConfig);
		Pipe<AcknowledgeSchema> adAckPipe = new Pipe<AcknowledgeSchema>(ackPipesConfig);
		//Pipe<RawDataSchema> I2CToListener = new Pipe<RawDataSchema>(I2CToListenerConfig);
		//Pipe<RawDataSchema> adInToListener = new Pipe<RawDataSchema>(adInToListenerConfig);
		
		I2CJFFIStage i2cJFFIStage = new I2CJFFIStage(this.gm, i2cGoPipe, i2cPipes, i2cAckPipe, this);
		//AnalogDigitalInputStage adInputStage = new AnalogDigitalInputStage(this.gm, adInToListener, listenerGoPipe, this); //TODO: Probably needs an ack Pipe
		AnalogDigitalOutputStage adOutputStage = new AnalogDigitalOutputStage(this.gm, requestPipes, adGoPipe, adAckPipe, this);
		TrafficCopStage trafficCopStage = new TrafficCopStage(this.gm, orderPipes, PronghornStage.join(adAckPipe, i2cAckPipe), PronghornStage.join(adGoPipe, i2cGoPipe));
		System.out.println("GrovePi Stage setup successful");
		
	}

	@Override
	public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<GoSchema> orderPipe) {
		return new PiCommandChannel(pipe, i2cPayloadPipe, orderPipe);
	}
	
	public byte getI2CConnector(){
		return 1;
	}
	public void coldSetup() {
		//usedLines = buildUsedLines();
		//GrovePiGPIO.ensureAllLinuxDevices(usedLines);
	}

	public void cleanup() {
		//GrovePiGPIO.removeAllLinuxDevices(usedLines);
	}

	public void i2cDataIn() {
		//			GrovePiGPIO.configI2CDataIn();
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public void i2cDataOut() {
		//GrovePiGPIO.configI2CDataOut();
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public void i2cClockIn() {
		//GrovePiGPIO.configI2CClockIn();
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public void i2cClockOut() {
		//GrovePiGPIO.configI2CClockOut();
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public boolean i2cReadAck() {
		//			boolean ack;
		//			int voltage = analogRead(GrovePiConstants.DATA_RAW_VOLTAGE);
		//			ack = voltage<GrovePiConstants.HIGH_LINE_VOLTAGE_MARK;
		//			if (!ack) {    
		//				System.err.println("ack value "+ack+" "+Integer.toBinaryString(voltage));
		//			}
		//			return ack;
		System.out.println("Pure Java I2c not currently supported on Pi");
		return true;
	}

	@Override
	public boolean i2cReadClockBool() {
		//return i2cReadClock() == 0; //TODO: 0 or 1 for HI?
		System.out.println("Pure Java I2c not currently supported on Pi");
		return false;
	}

	public void beginPinConfiguration() {
		//super.beginPinConfiguration();        
	}

	public void endPinConfiguration() {
		//super.endPinConfiguration();
	}

	public int digitalRead(int connector) { 
		System.out.println("GPIO not currently supported on Pi");
		return 0;
	}

	//TODO: Since there's no ADC built into the Pi, we can only read HI or LO.
	public int analogRead(int connector) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void analogWrite(int connector, int value) {
		throw new UnsupportedOperationException();
	}

	//Now using the JFFI stage
	public void digitalWrite(int connector, int value) {
		System.out.println("GPIO not currently supported on Pi");
	}

	//TODO: Is it right to config them as outputs before writing?
	public void i2cSetClockLow() {
		//			GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_CLOCK);
		//			GrovePiPinManager.writeValue(GrovePiPinManager.I2C_CLOCK, GrovePiPinManager.I2C_LOW, GrovePiGPIO.gpioLinuxPins);
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public void i2cSetClockHigh() {
		//			GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_CLOCK);
		//			GrovePiPinManager.writeValue(GrovePiPinManager.I2C_CLOCK, GrovePiPinManager.I2C_HIGH, GrovePiGPIO.gpioLinuxPins);
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public void i2cSetDataLow() {
		//			GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_DATA);
		//			GrovePiPinManager.writeValue(GrovePiPinManager.I2C_DATA, GrovePiPinManager.I2C_LOW, GrovePiGPIO.gpioLinuxPins);
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public void i2cSetDataHigh() {
		//			GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_DATA);
		//			GrovePiPinManager.writeValue(GrovePiPinManager.I2C_DATA, GrovePiPinManager.I2C_HIGH, GrovePiGPIO.gpioLinuxPins);
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public int i2cReadData() {
		System.out.println("Pure Java I2c not currently supported on Pi");
		//return digitalRead(GrovePiPinManager.I2C_DATA);
		return -1;
	}
	public boolean i2cReadDataBool() { return false; } //TODO:

	public int i2cReadClock() {
		System.out.println("Pure Java I2c not currently supported on Pi");
		//return digitalRead(GrovePiPinManager.I2C_CLOCK);
		return -1;
	}

	static void findDup(HardConnection[] base, int baseLimit, HardConnection[] items, boolean mapAnalogs) {
		int i = items.length;
		while (--i>=0) {
			int j = baseLimit;
			while (--j>=0) {
				//TODO: Will probably have undesired side effects.
				//                if (mapAnalogs ? base[j].connection == GrovePiConstants.ANALOG_CONNECTOR_TO_PIN[items[i].connection] :  base[j]==items[i]) {
				if (mapAnalogs ? false : base[j] == items[i]) {
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
			throw new UnsupportedOperationException("Rotary encoder requires two neighboring digital inputs.");
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

		if (configI2C) {
			findDup(result,pos,GrovePiConstants.i2cPins, false);
			System.arraycopy(GrovePiConstants.i2cPins, 0, result, pos, GrovePiConstants.i2cPins.length);
			pos+=GrovePiConstants.i2cPins.length;
		}

		return result;
	}

	
}

