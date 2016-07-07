package com.ociweb.iot.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.impl.edison.EdisonConstants;
import com.ociweb.iot.hardware.impl.edison.EdisonGPIO;
import com.ociweb.iot.hardware.impl.edison.EdisonPinManager;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.EdisonCommandChannel;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GroveV3EdisonImpl extends Hardware {
	
	private final static Logger logger = LoggerFactory.getLogger(GroveV3EdisonImpl.class);
    private HardConnection[] usedLines;
    
    //pwm supports the same range and duty values for multiple platforms,  The frequencies are "near" each other but not yet the same.
    private int pwmBitsShift = 12; //the absolute minimum range for Edison is 1<<12 or 4096 this prevents the user from hitting this value.
    
    public GroveV3EdisonImpl(GraphManager gm) {
    	super(gm);
   }
      
    
    
    public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<TrafficOrderSchema> orderPipe) {
		return new EdisonCommandChannel(gm, pipe, i2cPayloadPipe, orderPipe);
	}
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
			EdisonGPIO.configDigitalOutput(digitalOutputs[i].connection);//config for writeBit
			System.out.println("configured output "+super.digitalOutputs[i].twig+" on connection "+super.digitalOutputs[i].connection);
		}
//      System.out.println("The Analog Output Length is: " +pwmOutputs.length);
//      System.out.println("The Analog Output connection at 0 is: " +pwmOutputs[0].connection);
//      System.out.println("The Analog Output Type is at 0 is " + pwmOutputs[0].type );
//      System.out.println("The output type is: " +ConnectionType.Direct);
//      System.out.println("The port used is:"+ (int)pwmOutputs[0].connection);
		for (int i = 0; i < pwmOutputs.length; i++) {
			EdisonGPIO.configPWM((int)pwmOutputs[i].connection); //config for pwm
		}
		for (int i = 0; i < super.digitalInputs.length; i++) {
			EdisonGPIO.configDigitalInput(digitalInputs[i].connection); //config for readBit
		}
		for (int i = 0; i < super.analogInputs.length; i++) {
			EdisonGPIO.configAnalogInput(analogInputs[i].connection); //config for readInt
		}
		EdisonGPIO.configI2C();
		endPinConfiguration();//Tri State set high to end configuration
		
		//everything is up and running so set the pwmRange for each device
		
		for (int i = 0; i < pwmOutputs.length; i++) {
		    
		        EdisonPinManager.writePWMRange(pwmOutputs[i].connection, pwmOutputs[i].twig.pwmRange() << pwmBitsShift);
		    
        }
		
		
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

    @Override
    public void analogWrite(int connector, int value) {
       assert(isInPWMRange(connector,value)) : "Unsupported call"; 
       EdisonPinManager.writePWMDuty(connector, value << pwmBitsShift);
    }
    
	private boolean isInPWMRange(int connector, int value) {
	    for (int i = 0; i < pwmOutputs.length; i++) {
	        if (connector == pwmOutputs[i].connection) {
	            if (value > pwmOutputs[i].twig.pwmRange()) {
	                logger.error("pwm value {} out of range, must not be larger than {} ",value,pwmOutputs[i].twig.pwmRange());
	                return false;
	            }
	            return true;    
	        }
	    }
	    logger.error("did not find connection {} as defined",connector);
        return false;
    }



    public void digitalWrite(int connector, int value) {
	    assert(0==value || 1==value);    
	    EdisonPinManager.digitalWrite(connector, value, EdisonGPIO.gpioLinuxPins);
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
            result[pos++] = new HardConnection(analogInputs[j].twig,(int) EdisonConstants.ANALOG_CONNECTOR_TO_PIN[analogInputs[j].connection]);
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
		return 6;
	}



    @Override
    public byte[][] getGroveI2CInputs() {
        // TODO Auto-generated method stub
        return null;
    }


}
