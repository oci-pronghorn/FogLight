package com.ociweb.iot.hardware;

import static com.ociweb.pronghorn.pipe.PipeWriter.publishWrites;
import static com.ociweb.pronghorn.pipe.PipeWriter.tryWriteFragment;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.device.testApps.JFFITestStage;
import com.ociweb.iot.hardware.impl.grovepi.GrovePiConstants;
import com.ociweb.iot.hardware.impl.grovepi.GrovePiGPIO;
import com.ociweb.iot.hardware.impl.grovepi.GrovePiPinManager;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GrovePiImpl extends Hardware {

	private HardConnection[] usedLines;

	public Pipe toJFFI;
	public Pipe fromJFFI;

	private final FieldReferenceOffsetManager FROMToJFFI = Pipe.from(toJFFI); 
//	  TODO: Exception in thread "main" java.lang.NullPointerException
//    at com.ociweb.pronghorn.pipe.Pipe.from(Pipe.java:2603)
//    at com.ociweb.device.config.grovepi.GrovePiConfiguration.<init>(GrovePiConfiguration.java:35)
//    at com.ociweb.device.impl.graph.IOTDeviceRuntime.getHarwareConfig(IOTDeviceRuntime.java:65)
//    at com.ociweb.pronghorn.iot.Demo.<init>(Demo.java:20)
//    at com.ociweb.pronghorn.iot.Demo.main(Demo.java:36)

    private final DataOutputBlobWriter writer = new DataOutputBlobWriter(toJFFI);

	private final FieldReferenceOffsetManager FROMFromJFFI = Pipe.from(fromJFFI);
	private final DataInputBlobReader reader = new DataInputBlobReader(fromJFFI);

	private JFFITestStage jffiTestStage;

	private static final Logger logger = LoggerFactory.getLogger(GrovePiImpl.class);

	public GrovePiImpl(GraphManager gm) {
		super();
		this.jffiTestStage= new JFFITestStage(gm, toJFFI, fromJFFI);
	}

	public GrovePiImpl(boolean publishTime, boolean configI2C, HardConnection[] encoderInputs,
			HardConnection[] digitalInputs, HardConnection[] digitalOutputs, HardConnection[] pwmOutputs, HardConnection[] analogInputs, GraphManager gm) {
		super(publishTime, configI2C, encoderInputs, digitalInputs, digitalOutputs, pwmOutputs, analogInputs);
		this.jffiTestStage= new JFFITestStage(gm, toJFFI, fromJFFI);
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
		GrovePiGPIO.configI2C();
	}

	public void i2cDataIn() {
		GrovePiGPIO.configI2CDataIn();
	}

	public void i2cDataOut() {
		GrovePiGPIO.configI2CDataOut();
	}

	public void i2cClockIn() {
		GrovePiGPIO.configI2CClockIn();
	}

	public void i2cClockOut() {
		GrovePiGPIO.configI2CClockOut();
	}

	public boolean i2cReadAck() {
		boolean ack;
		int voltage = analogRead(GrovePiConstants.DATA_RAW_VOLTAGE);
		ack = voltage<GrovePiConstants.HIGH_LINE_VOLTAGE_MARK;
		if (!ack) {    
			System.err.println("ack value "+ack+" "+Integer.toBinaryString(voltage));
		}
		return ack;
	}

	@Override
	public boolean i2cReadClockBool() {
		return i2cReadClock() == 0; //TODO: 0 or 1 for HI?
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
			byte[] message = {0x04, 0x05, 0x01, 0x01, 0x01, (byte) connector, 0x00, 0x00}; //address, package size, returnBytes, package[]
			while (tryWriteFragment(toJFFI, RawDataSchema.MSG_CHUNKEDSTREAM_1)) {

				DataOutputBlobWriter.openField(writer);
				try {
					writer.write(message);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}

				DataOutputBlobWriter.closeHighLevelField(writer, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
				publishWrites(toJFFI);

			}
			while (PipeReader.tryReadFragment(fromJFFI)) {		
				assert(PipeReader.isNewMessage(fromJFFI)) : "This test should only have one simple message made up of one fragment";
				int msgIdx = PipeReader.getMsgIdx(fromJFFI);
				
				if(RawDataSchema.MSG_CHUNKEDSTREAM_1 == msgIdx){
					reader.openHighLevelAPIField(RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
					try {
						temp = reader.readByte();
					} catch (IOException e) {
						//address, package size, returnBytes, package[]
					}
				}
				try {
					reader.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}

				PipeReader.releaseReadLock(fromJFFI);



			} 
		}else{
			//TODO: add standard GPIO support
			temp = 0;
		}
		return temp;
	}

	//TODO: Since there's no ADC built into the Pi, we can only read HI or LO.
	public int analogRead(int connector) {
		return GrovePiPinManager.digitalRead(connector);
	}
	

    @Override
    public void analogWrite(int connector, int value) {
       
        //TODO: needed for pi
        // GrovePiPinManager.analogWrite(connector,value);
        
    }


	//Now using the JFFI stage
	public void digitalWrite(int connector, int value) {
		if(connector>1000){ //for grove add 1000 to pin numbers
			connector -= 1000;
			byte[] message = {0x04, 0x05, 0x00, 0x01, 0x02, (byte) connector, (byte) value, 0x00}; //address, package size, returnBytes, package[]
			while (tryWriteFragment(toJFFI, RawDataSchema.MSG_CHUNKEDSTREAM_1)) {
				DataOutputBlobWriter.openField(writer);
				try {
					writer.writeByte((byte)0x04);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				DataOutputBlobWriter.closeHighLevelField(writer, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
				publishWrites(toJFFI);
			}
		}else{
			//TODO: Add standard GPIO pin support
		}
	}

	//TODO: Is it right to config them as outputs before writing?
	public void i2cSetClockLow() {
		GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_CLOCK);
		GrovePiPinManager.writeValue(GrovePiPinManager.I2C_CLOCK, GrovePiPinManager.I2C_LOW, GrovePiGPIO.gpioLinuxPins);
	}

	public void i2cSetClockHigh() {
		GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_CLOCK);
		GrovePiPinManager.writeValue(GrovePiPinManager.I2C_CLOCK, GrovePiPinManager.I2C_HIGH, GrovePiGPIO.gpioLinuxPins);
	}

	public void i2cSetDataLow() {
		GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_DATA);
		GrovePiPinManager.writeValue(GrovePiPinManager.I2C_DATA, GrovePiPinManager.I2C_LOW, GrovePiGPIO.gpioLinuxPins);
	}

	public void i2cSetDataHigh() {
		GrovePiGPIO.configDigitalOutput(GrovePiPinManager.I2C_DATA);
		GrovePiPinManager.writeValue(GrovePiPinManager.I2C_DATA, GrovePiPinManager.I2C_HIGH, GrovePiGPIO.gpioLinuxPins);
	}

	public int i2cReadData() {
		return digitalRead(GrovePiPinManager.I2C_DATA);
	}
	public boolean i2cReadDataBool() { return false; } //TODO:

	public int i2cReadClock() {
		return digitalRead(GrovePiPinManager.I2C_CLOCK);
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
		                                         encoderInputs.length+
		                                         digitalOutputs.length+
		                                         pwmOutputs.length+
		                                         analogInputs.length+
		                                         (configI2C?2:0)];

		int pos = 0;
		System.arraycopy(digitalInputs, 0, result, pos, digitalInputs.length);
		pos+=digitalInputs.length;

		if (0!=(encoderInputs.length&0x1)) {
			throw new UnsupportedOperationException("Rotary encoder requires two neighboring digital inputs.");
		}
		findDup(result,pos,encoderInputs, false);
		System.arraycopy(encoderInputs, 0, result, pos, encoderInputs.length);
		pos+=encoderInputs.length;

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