package com.ociweb.iot.hardware;

import static com.ociweb.pronghorn.pipe.PipeWriter.publishWrites;
import static com.ociweb.pronghorn.pipe.PipeWriter.tryWriteFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.impl.grovepi.GrovePiConstants;
import com.ociweb.iot.hardware.impl.grovepi.GrovePiGPIO;
import com.ociweb.iot.hardware.impl.grovepi.GrovePiPinManager;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GrovePiImpl extends Hardware {

	//private HardConnection[] usedLines;
	

    

	private JFFISupportStage jffiSupportStage;
	private JFFIStage jffiStage;
	
	private List<byte[]> readData;

	private static final Logger logger = LoggerFactory.getLogger(GrovePiImpl.class);

	public GrovePiImpl(GraphManager gm) {
		PipeConfig<RawDataSchema> toJffiConfig= new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
		PipeConfig<RawDataSchema> fromJffiConfig= new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
		Pipe<RawDataSchema> toJffi = new Pipe(toJffiConfig);
		Pipe<RawDataSchema> fromJffi = new Pipe(fromJffiConfig);
		this.jffiSupportStage= new JFFISupportStage(gm, fromJffi, toJffi);
		this.jffiStage = new JFFIStage(gm, fromJffi, toJffi);
		this.readData = new ArrayList<byte[]>();
	}

	public GrovePiImpl(boolean publishTime, boolean configI2C, HardConnection[] encoderInputs,
			HardConnection[] digitalInputs, HardConnection[] digitalOutputs, HardConnection[] pwmOutputs, HardConnection[] analogInputs, GraphManager gm) {
		super(publishTime, configI2C, encoderInputs, digitalInputs, digitalOutputs, pwmOutputs, analogInputs);
		PipeConfig<RawDataSchema> toJffiConfig= new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
		PipeConfig<RawDataSchema> fromJffiConfig= new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
		Pipe<RawDataSchema> toJffi = new Pipe(toJffiConfig);
		Pipe<RawDataSchema> fromJffi = new Pipe(fromJffiConfig);
		this.jffiSupportStage= new JFFISupportStage(gm, fromJffi, toJffi);
		this.jffiStage = new JFFIStage(gm, toJffi, fromJffi);
		this.readData = new ArrayList<byte[]>();
		
	}

	public void coldSetup() {
		//usedLines = buildUsedLines();
		//GrovePiGPIO.ensureAllLinuxDevices(usedLines);
	}

	public void cleanup() {
		//GrovePiGPIO.removeAllLinuxDevices(usedLines);
	}

	public void configurePinsForDigitalInput(byte connection) {
		//GrovePiGPIO.configDigitalInput(connection); //readBit
		System.out.println("No config needed");
	}

	public void configurePinsForDigitalOutput(byte connection) {
		//GrovePiGPIO.configDigitalOutput(connection);//writeBit
		System.out.println("No config needed");
	}

	public void configurePinsForAnalogInput(byte connection) {
	    System.out.println("No config needed");
	}
	
    @Override
    public void configurePinsForAnalogOutput(byte connection) {
        System.out.println("No config needed");
        
    }

	public void configurePinsForI2C() {
//		GrovePiGPIO.configI2C();
		System.out.println("No config needed");
	}

	public void i2cDataIn() {
//		GrovePiGPIO.configI2CDataIn();
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
//		boolean ack;
//		int voltage = analogRead(GrovePiConstants.DATA_RAW_VOLTAGE);
//		ack = voltage<GrovePiConstants.HIGH_LINE_VOLTAGE_MARK;
//		if (!ack) {    
//			System.err.println("ack value "+ack+" "+Integer.toBinaryString(voltage));
//		}
//		return ack;
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
		int temp = 0;
		if(connector>1000){ //for grove add 1000 to pin numbers
			connector -= 1000;
			byte readCommand[] = {0x04, 0x05, 0x00, 0x01, 0x01, (byte)connector, 0x00, 0x00};
			jffiSupportStage.writeData(readCommand);
			readData.addAll(jffiSupportStage.readData());
			byte temp2[] = readData.get(0);
			temp = temp2[0];
		}else{
			//TODO: add standard GPIO support
			temp = 0;
		}
		return temp;
	}

	//TODO: Since there's no ADC built into the Pi, we can only read HI or LO.
	public int analogRead(int connector) {
		//return GrovePiPinManager.digitalRead(connector);
		return 0; //TODO: add JFFI support
	}
	

    @Override
    public void analogWrite(int connector, int value) {
       
        //TODO: needed for pi
        // GrovePiPinManager.analogWrite(connector,value);
        
    }

	//Now using the JFFI stage
	public void digitalWrite(int connector, int value) {
		byte[] message = {0x04, 0x05, 0x00, 0x01, 0x02, (byte) connector, (byte) value, 0x00};
		jffiSupportStage.writeData(message);
		System.out.println("Digital Write is called");
	}

	//TODO: Is it right to config them as outputs before writing?
	public void i2cSetClockLow() {
//		GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_CLOCK);
//		GrovePiPinManager.writeValue(GrovePiPinManager.I2C_CLOCK, GrovePiPinManager.I2C_LOW, GrovePiGPIO.gpioLinuxPins);
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public void i2cSetClockHigh() {
//		GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_CLOCK);
//		GrovePiPinManager.writeValue(GrovePiPinManager.I2C_CLOCK, GrovePiPinManager.I2C_HIGH, GrovePiGPIO.gpioLinuxPins);
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public void i2cSetDataLow() {
//		GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_DATA);
//		GrovePiPinManager.writeValue(GrovePiPinManager.I2C_DATA, GrovePiPinManager.I2C_LOW, GrovePiGPIO.gpioLinuxPins);
		System.out.println("Pure Java I2c not currently supported on Pi");
	}

	public void i2cSetDataHigh() {
//		GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_DATA);
//		GrovePiPinManager.writeValue(GrovePiPinManager.I2C_DATA, GrovePiPinManager.I2C_HIGH, GrovePiGPIO.gpioLinuxPins);
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

//	public HardConnection[] buildUsedLines() {
//
//		
//		HardConnection[] result = new HardConnection[digitalInputs.length+
//		                                         encoderInputs.length+
//		                                         digitalOutputs.length+
//		                                         pwmOutputs.length+
//		                                         analogInputs.length+
//		                                         (configI2C?2:0)];
//
//		int pos = 0;
//		System.arraycopy(digitalInputs, 0, result, pos, digitalInputs.length);
//		pos+=digitalInputs.length;
//
//		if (0!=(encoderInputs.length&0x1)) {
//			throw new UnsupportedOperationException("Rotary encoder requires two neighboring digital inputs.");
//		}
//		findDup(result,pos,encoderInputs, false);
//		System.arraycopy(encoderInputs, 0, result, pos, encoderInputs.length);
//		pos+=encoderInputs.length;
//
//		findDup(result,pos,digitalOutputs, false);
//		System.arraycopy(digitalOutputs, 0, result, pos, digitalOutputs.length);
//		pos+=digitalOutputs.length;
//
//		findDup(result,pos,pwmOutputs, false);
//		System.arraycopy(pwmOutputs, 0, result, pos, pwmOutputs.length);
//		pos+=pwmOutputs.length;
//
//		if (configI2C) {
//			findDup(result,pos,GrovePiConstants.i2cPins, false);
//			System.arraycopy(GrovePiConstants.i2cPins, 0, result, pos, GrovePiConstants.i2cPins.length);
//			pos+=GrovePiConstants.i2cPins.length;
//		}
//
//		return result;
//	}



}