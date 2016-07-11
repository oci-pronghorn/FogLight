package com.ociweb.iot.hardware;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.impl.grovepi.GrovePiConstants;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.PiCommandChannel;
import com.ociweb.pronghorn.TrafficCopStage;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;


public class GroveV2PiImpl extends Hardware {

	private static final Logger logger = LoggerFactory.getLogger(GroveV2PiImpl.class);
	
	private byte commandIndex = -1;
	

	public GroveV2PiImpl(GraphManager gm, I2CBacking i2cBacking) {
		super(gm, i2cBacking);
	}


	@Override
	public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<TrafficOrderSchema> orderPipe) {
		this.commandIndex++;
		return new PiCommandChannel(gm, pipe, i2cPayloadPipe, orderPipe, commandIndex);	
	}
	
	@Override
	public Hardware useConnectA(IODevice t, int connection) {
        return useConnectA(t,connection,-1);
    }
    
	@Override
    public Hardware useConnectA(IODevice t, int connection, int customRate) { //TODO: add customRate support
        if (t.isInput()) {
            assert(!t.isOutput());
            byte[] temp = {0x01,0x03,(byte)connection,0x00,0x00};
            i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t,(byte)4,temp,(byte)3,connection));
        } else {
            assert(t.isOutput());
            pwmOutputs = growHardConnections(pwmOutputs, new HardConnection(t,connection));
        }
        return this;
    }
    
	@Override
    public Hardware useConnectD(IODevice t, int connection) {
        return useConnectD(t,connection,-1);
    }
    
	@Override
    public Hardware useConnectD(IODevice t, int connection, int customRate) { //TODO: add customRate support
    	
        if (t.isInput()) {
            assert(!t.isOutput());
            byte[] temp = {0x01,0x01,(byte)connection,0x00,0x00};
            System.out.println("Digital Input Connected on "+connection);
            i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t,(byte)4,temp,1,connection)); //TODO: Always assumes Grove
        } else {
            assert(t.isOutput());
            digitalOutputs = growHardConnections(digitalOutputs, new HardConnection(t,connection));
        }
        return this;
    }  
    
	@Override
    public Hardware useConnectDs(IODevice t, int ... connections) {

        if (t.isInput()) {
            assert(!t.isOutput());
            for(int con:connections) {
                multiBitInputs = growHardConnections(multiBitInputs, new HardConnection(t,con)); //TODO: Add multiple input support for pi
            }
            
          System.out.println("connections "+Arrays.toString(connections));  
          System.out.println("Encoder here "+Arrays.toString(multiBitInputs));  
            
        } else {
            assert(t.isOutput());
            for(int con:connections) {
                multiBitOutputs = growHardConnections(multiBitOutputs, new HardConnection(t,con));
            }
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
		System.out.println("I'm calling this method like a dingus");
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

