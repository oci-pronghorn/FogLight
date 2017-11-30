package com.ociweb.iot.hardware.impl.edison;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.impl.stage.ReactiveListenerStage;
import com.ociweb.gl.impl.stage.ReactiveManagerPipeConsumer;
import com.ociweb.iot.hardware.HardwareConnection;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.HardwarePlatformType;
import com.ociweb.iot.hardware.impl.DefaultCommandChannel;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.ReactiveIoTListenerStage;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfigManager;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GroveV3EdisonImpl extends HardwareImpl {

	private final static Logger logger = LoggerFactory.getLogger(GroveV3EdisonImpl.class);
	private HardwareConnection[] usedLines;

	private static final int PIN_SETUP_TIMEOUT = 3; //in seconds
	//pwm supports the same range and duty values for multiple platforms,  The frequencies are "near" each other but not yet the same.
	private int pwmBitsShift = 12; //the absolute minimum range for Edison is 1<<12 or 4096 this prevents the user from hitting this value.

	public GroveV3EdisonImpl(GraphManager gm, String[] args, int i2cBus) {
		super(gm, args, i2cBus);
		//can't print "you are running on Edison" here because we create an unstable intance of this object upon start-up to
		//determine if we are indeed running on the Edison.
	}

	@Override
	public FogCommandChannel newCommandChannel(int features, int instance, PipeConfigManager pcm) {
		return new DefaultCommandChannel(gm, this, features, instance, pcm);
	}

	@Override
	public FogCommandChannel newCommandChannel(int instance, PipeConfigManager pcm) {
		return new DefaultCommandChannel(gm, this, 0, instance, pcm);
	}
	
	@Override
	public void coldSetup() {
		System.out.println("ColdSetup: Edison Pin Configuration setup!");
		usedLines = buildUsedLines();
		EdisonGPIO.ensureAllLinuxDevices(usedLines);

		beginPinConfiguration(); //TODO:Uncertain stay above/below setToKnownStateFromColdStart,Will trial and error

		setToKnownStateFromColdStart();  
		//		EdisonGPIO.configPWM(5);//config for writeBit
		//		EdisonGPIO.configDigitalOutput(6);//config for writeBit
		//        System.out.println("The digital Output Length is: " +digitalOutputs.length);
		//        System.out.println("The digital Output connection at 0 is: " +digitalOutputs[0].connection);
		//        System.out.println("The digital Output type at 0 is: " +digitalOutputs[0].type);
		for (int i = 0; i < digitalOutputs.length; i++) {
			EdisonGPIO.configDigitalOutput(digitalOutputs[i].register);//config for writeBit
			System.out.println("configured output "+super.digitalOutputs[i].twig+" on connection "+super.digitalOutputs[i].register);
		}
		//      System.out.println("The Analog Output Length is: " +pwmOutputs.length);
		//      System.out.println("The Analog Output connection at 0 is: " +pwmOutputs[0].connection);
		//      System.out.println("The Analog Output Type is at 0 is " + pwmOutputs[0].type );
		//      System.out.println("The output type is: " +ConnectionType.Direct);
		//      System.out.println("The port used is:"+ (int)pwmOutputs[0].connection);
		for (int i = 0; i < pwmOutputs.length; i++) {
			EdisonGPIO.configPWM((int)pwmOutputs[i].register); //config for pwm
		}
		for (int i = 0; i < super.digitalInputs.length; i++) {
			EdisonGPIO.configDigitalInput(digitalInputs[i].register); //config for readBit
		}
		for (int i = 0; i < super.analogInputs.length; i++) {
			EdisonGPIO.configAnalogInput(analogInputs[i].register); //config for readInt
		}
		EdisonGPIO.configI2C();
		endPinConfiguration();//Tri State set high to end configuration

		//everything is up and running so set the pwmRange for each device

		for (int i = 0; i < pwmOutputs.length; i++) {

			EdisonPinManager.writePWMRange(pwmOutputs[i].register, pwmOutputs[i].twig.range() << pwmBitsShift);

		}

		super.coldSetup();
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


	public static void setToKnownStateFromColdStart() {
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

	private void beginPinConfiguration() {
		//    	 try {
		//             if (!devicePinConfigurationLock.tryLock(PIN_SETUP_TIMEOUT, TimeUnit.SECONDS)) {
		//                 throw new RuntimeException("One of the stages was not able to complete startup due to pin configuration issues.");
		//             }
		//         } catch (InterruptedException e) {
		//            Thread.currentThread().interrupt();
		//         }     
		//TODO: Can this be blown away?
		EdisonGPIO.shieldControl.setDirectionLow(0);
	}

	private void endPinConfiguration() {
		EdisonGPIO.shieldControl.setDirectionHigh(0);
		//devicePinConfigurationLock.unlock(); //TODO: can be blown away?
	}

	@Override
	public HardwarePlatformType getPlatformType() {
		return HardwarePlatformType.INTEL_EDITION;
	}

	@Override
	public int read(Port port) {        
		return port.isAnalog() ? EdisonPinManager.analogRead(port.port): EdisonPinManager.digitalRead(port.port);
	}


	@Override
	public void write(Port port, int value) {
		
		if (port.isAnalog()) {
			
			assert(isInPWMRange(port.port,value)) : "Unsupported call"; 
			EdisonPinManager.writePWMDuty(port.port, value << pwmBitsShift);		
			
		} else {
			
			assert(0==value || 1==value);    
			EdisonPinManager.digitalWrite(port.port, value, EdisonGPIO.gpioLinuxPins);
		}
	}


	private boolean isInPWMRange(int connector, int value) {
		for (int i = 0; i < pwmOutputs.length; i++) {
			if (connector == pwmOutputs[i].register) {
				if (value > pwmOutputs[i].twig.range()) {
					logger.error("pwm value {} out of range, must not be larger than {} ",value,pwmOutputs[i].twig.range());
					return false;
				}
				return true;    
			}
		}
		logger.error("did not find connection {} as defined",connector);
		return false;
	}




	@Override
	public <R extends ReactiveListenerStage> R createReactiveListener(GraphManager gm,  Behavior listener, 
			                                                          Pipe<?>[] inputPipes, Pipe<?>[] outputPipes, 
			                                                          ArrayList<ReactiveManagerPipeConsumer> consumers,
			                                                          int parallelInstance, String nameId) {
		return (R)new ReactiveIoTListenerStage(gm, listener, inputPipes, outputPipes, consumers, this, parallelInstance, nameId);
	}
	

}
