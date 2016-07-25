package com.ociweb.iot.hardware.impl.grovepi;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.GroveTwig;
import com.ociweb.iot.hardware.HardConnection;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.pronghorn.iot.DefaultReactiveListenerStage;
import com.ociweb.pronghorn.iot.DexterGrovePiReactiveListenerStage;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;


public class GroveV2PiImpl extends Hardware {

	private static final Logger logger = LoggerFactory.getLogger(GroveV2PiImpl.class);

	private byte commandIndex = -1;


	public GroveV2PiImpl(GraphManager gm, I2CBacking i2cBacking) {
		super(gm, i2cBacking);
	}


	@Override
	public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> orderPipe) {
		this.commandIndex++;
		return new PiCommandChannel(gm, pipe, i2cPayloadPipe, messagePubSub, orderPipe, commandIndex);	
	}

	@Override
	public Hardware useConnectA(IODevice t, int connection) {
		return useConnectA(t,connection,-1);
	}

	@Override
	public Hardware useConnectA(IODevice t, int connection, int customRate) { //TODO: add customRate support
		if(t.isGrove()){
			connection = connection + 14; //TODO: Nathan know I did this to map analog pin numbers to the board.
										  //If you prefer, we can add it to the twigs themselves instead of here
			if (t.isInput()) {
				assert(!t.isOutput());
				byte[] temp = {0x01,0x03,(byte)connection,0x00,0x00};
				byte[] setup = {0x01, 0x05, (byte)connection,0x00,0x00};
				i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t,(byte)4,temp,(byte)3,connection,setup));
			} else {
				assert(t.isOutput());
				assert(!t.isInput());
				byte[] setup = {0x01, 0x05, (byte)connection,0x01,0x00};
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,(byte)4,null,0,connection,setup));
			}
		}else{
			throw new UnsupportedOperationException("you have tried to connect an analog device to a GPIO pin");
		}
		return this;
	}

	@Override
	public Hardware useConnectD(IODevice t, int connection) {
		return useConnectD(t,connection,-1);
	}

	@Override
	public Hardware useConnectD(IODevice t, int connection, int customRate) { //TODO: add customRate support

		if(t.isGrove()){
			if (t.isInput()) {
				assert(!t.isOutput());
				byte[] readCmd = {0x01,0x01,(byte)connection,0x00,0x00};
				byte[] setup = {0x01, 0x05, (byte)connection,0x00,0x00};
				logger.info("Digital Input Connected on "+connection);
				i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t,(byte)4,readCmd,1,connection,setup));
			} else {
				assert(t.isOutput());
				assert(!t.isInput());
				byte[] setup = {0x01, 0x05, (byte)connection,0x01,0x00};
				logger.info("Digital Output Connected on "+connection);
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,(byte)4,null,0,connection,setup));
			}
		}else{
			System.out.println("GPIO not currently supported");
		}
		return this;
	}  

	@Override
	public Hardware useConnectDs(IODevice t, int ... connections) {
		//TODO: add the special grove interrupt support to this
		if (t.getClass()== GroveTwig.RotaryEncoder.getClass()) {
			int[] temp = {2,3};
			assert(Arrays.equals(connections, temp)) : "RotaryEncoders may only be connected to ports 2 and 3 on Pi";
			
			byte[] ENCODER_READCMD = {0x01, 11, 0x00, 0x00, 0x00};
		    byte[] ENCODER_SETUP = {0x01, 16, 0x00, 0x00, 0x00};
		    byte ENCODER_ADDR = 0x04;
		    byte ENCODER_BYTESTOREAD = 2;
		    byte ENCODER_REGISTER = 2;
			growI2CConnections(i2cInputs, new I2CConnection(t, ENCODER_ADDR, ENCODER_READCMD, ENCODER_BYTESTOREAD, ENCODER_REGISTER, ENCODER_SETUP));

			logger.info("connected Pi encoder");

		} else {
			throw new UnsupportedOperationException("You may only connect a RotaryEncoder with useConnectDs on Pi");
		}
		return this;

	}  

	public void coldSetup() {
		//usedLines = buildUsedLines();
		//GrovePiGPIO.ensureAllLinuxDevices(usedLines);
	}

	public void cleanup() {
		//GrovePiGPIO.removeAllLinuxDevices(usedLines);
	}

	public void beginPinConfiguration() {
		//super.beginPinConfiguration();        
	}

	public void endPinConfiguration() {
		//super.endPinConfiguration();
	}

	public int digitalRead(int connector) { 
		throw new UnsupportedOperationException();
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

	public boolean isListeningToI2C(Object listener) {
		return listener instanceof I2CListener || listener instanceof DigitalListener || listener instanceof AnalogListener || listener instanceof RotaryListener;
	}

	public boolean isListeningToPins(Object listener) {
		return false;//TODO: we have no support for this yet
	}

    public ReactiveListenerStage createReactiveListener(GraphManager gm,  Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes) {
        return new DexterGrovePiReactiveListenerStage(gm, listener, inputPipes, outputPipes, this); 
    }

}

