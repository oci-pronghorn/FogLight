package com.ociweb.iot.hardware.impl.grovepi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.GroveTwig;
import com.ociweb.iot.hardware.HardwareConnection;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.pronghorn.iot.DexterGrovePiReactiveListenerStage;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;


public class GrovePiHardwareImpl extends HardwareImpl {

	private static final Logger logger = LoggerFactory.getLogger(GrovePiHardwareImpl.class);

	private byte commandIndex = -1;


	public GrovePiHardwareImpl(GraphManager gm, I2CBacking i2cBacking) {
		super(gm, i2cBacking);
	}


	@Override
	public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> orderPipe) {
		this.commandIndex++;
		return new PiCommandChannel(gm, this, pipe, i2cPayloadPipe, messagePubSub, orderPipe, commandIndex);	
	} 
    
	@Override
	protected void connectAnalogOutput(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if(t.isGrove()){
    		connection = GrovePiConstants.DIGITAL_PIN_TO_REGISTER[connection];
			byte[] setup = {GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte)connection,GrovePiConstants.OUTPUT,0x00};				
			if(customAverageMS > 0){
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,null,0,connection, setup, customRate, everyValue)); //TODO:i2cConnection needs to support customAverageMS
			}else{
				i2cOutputs = growI2CConnections(i2cInputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,null,0,connection, setup, customRate));	
			} //TODO:i2cConnection needs to support customAverageMS
    	}else{
    		throw new UnsupportedOperationException("you have tried to connect an analog device to a GPIO pin");
    	}
	}
    
	@Override
	protected void connectAnalogInput(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if(t.isGrove()){
    		connection = GrovePiConstants.ANALOG_PIN_TO_REGISTER[connection];
			byte[] readCmd = {GrovePiConstants.START_BYTE,GrovePiConstants.ANALOG_READ,(byte)connection,0x00,0x00};
			byte[] setup = {GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte)connection,GrovePiConstants.INPUT,0x00};				
			if(customAverageMS > 0){
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,readCmd,(byte)3,connection, setup, customRate, everyValue)); //TODO:i2cConnection needs to support customAverageMS
			}else{
				i2cOutputs = growI2CConnections(i2cInputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,readCmd,(byte)3,connection, setup, customRate));	
			}
    	}else{
    		throw new UnsupportedOperationException("you have tried to connect an analog device to a GPIO pin");
    	}
	}
    
	@Override
	protected void connectDigitalOutput(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if(t.isGrove()){
    		connection = GrovePiConstants.DIGITAL_PIN_TO_REGISTER[connection];
			byte[] setup = {GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte)connection,GrovePiConstants.OUTPUT,0x00};				
			if(customAverageMS > 0){
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,null,0,connection, setup, customRate, everyValue)); //TODO:i2cConnection needs to support customAverageMS
			}else{
				i2cOutputs = growI2CConnections(i2cInputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,null,0,connection, setup, customRate));	
			}
		}else{
    		throw new UnsupportedOperationException("GPIO not yet supported");
    	}
	}
    
	@Override
	protected void connectDigitalInput(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if(t.isGrove()){
    		connection = GrovePiConstants.DIGITAL_PIN_TO_REGISTER[connection];
			byte[] readCmd = {GrovePiConstants.START_BYTE,GrovePiConstants.DIGITAL_READ,(byte)connection,0x00,0x00};
			byte[] setup = {GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte)connection,GrovePiConstants.INPUT,0x00};	
			if(customAverageMS > 0){
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,readCmd,(byte)1,connection, setup, customRate, everyValue));
			}else{
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,readCmd,(byte)1,connection, setup, customRate));
			}
    	}else{
    		throw new UnsupportedOperationException("GPIO not yet supported");
    	}
	}

	@Override
	public int digitalRead(int connector) { 
		throw new UnsupportedOperationException("GPIO not yet supported");
	}

	@Override
	public int analogRead(int connector) {
		throw new UnsupportedOperationException("Pi has no analog capabilities");
	}


	@Override
	public void analogWrite(int connector, int value) {
		throw new UnsupportedOperationException("Pi has no analog capabilities");
	}

	//Now using the JFFI stage
	@Override
	public void digitalWrite(int connector, int value) {
		throw new UnsupportedOperationException("GPIO not yet supported");
	}
	
	@Override
	public boolean hasI2CInputs() {
		return super.hasI2CInputs()|super.hasDigitalOrAnalogInputs();
	}

	@Override
	public boolean isListeningToPins(Object listener) {
		return false;//TODO: we have no support for this yet
	}

	@Override
    public ReactiveListenerStage createReactiveListener(GraphManager gm,  Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes) {
        return new DexterGrovePiReactiveListenerStage(gm, listener, inputPipes, outputPipes, this); 
    }

}

