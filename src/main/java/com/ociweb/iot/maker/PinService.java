package com.ociweb.iot.maker;

import com.ociweb.gl.api.MsgCommandChannel;
import com.ociweb.gl.impl.schema.MessagePrivate;
import com.ociweb.iot.hardware.impl.grovepi.PiCommandChannel;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;

public class PinService {

	private final FogCommandChannel cmd;
	
	public PinService(FogCommandChannel fogCommandChannel) {
		this.cmd = fogCommandChannel;

		if (fogCommandChannel instanceof PiCommandChannel) {
			cmd.initFeatures |= FogCommandChannel.I2C_WRITER;		
		} else {
			cmd.initFeatures |= FogCommandChannel.PIN_WRITER;
		}
		
	}

	public PinService(FogCommandChannel fogCommandChannel, int commandCountCapacity, int maxMessageSize) {
		this.cmd = fogCommandChannel;

		FogCommandChannel.growCommandCountRoom(cmd, commandCountCapacity);
		if (fogCommandChannel instanceof PiCommandChannel) {
			cmd.initFeatures |= FogCommandChannel.I2C_WRITER;
		} else {
			cmd.initFeatures |= FogCommandChannel.PIN_WRITER;
		}   
		PipeConfig<GroveRequestSchema> config = MsgCommandChannel.PCM(cmd).getConfig(GroveRequestSchema.class);
		if (FogCommandChannel.isTooSmall(commandCountCapacity, maxMessageSize, config)) {
			MsgCommandChannel.PCM(cmd).addConfig(Math.max(config.minimumFragmentsOnPipe(), commandCountCapacity),
			           Math.max(config.maxVarLenSize(), maxMessageSize), GroveRequestSchema.class);   
		}
	}


    /**
     * Sets the value of an analog/digital port on this command channel.
     *
     * @param port {@link Port} to set the value of.
     * @param value true is set to on full and false is set to off full.
     *
     * @return True if the port could be set, and false otherwise.
     */
    public boolean setValue(Port port, boolean value) {
    	return cmd.setValue(port, value);
    }
    
    /**
     * Sets the value of an analog/digital port on this command channel.
     *
     * @param port {@link Port} to set the value of.
     * @param value Value to set the port to.
     *
     * @return True if the port could be set, and false otherwise.
     */
    public boolean setValue(Port port, int value) {
    	return cmd.setValue(port,value);
    }

    /**
     * Sets the value of an analog/digital port on this command channel and then
     * delays processing of all future actions on this port until a specified
     * amount of time passes.
     *
     * @param port {@link Port} to set the value of.
     * @param value Value to set the port to.
     * @param durationMilli Time in milliseconds to delay processing of future actions
     *                      on this port.
     *
     * @return True if the port could be set, and false otherwise.
     */
    public boolean setValueAndBlock(Port port, boolean value, long durationMilli) {
    	return cmd.setValueAndBlock(port, value, durationMilli);
    }
    
    /**
     * Sets the value of an analog/digital port on this command channel and then
     * delays processing of all future actions on this port until a specified
     * amount of time passes.
     *
     * @param port {@link Port} to set the value of.
     * @param value Value to set the port to.
     * @param durationMilli Time in milliseconds to delay processing of future actions
     *                      on this port.
     *
     * @return True if the port could be set, and false otherwise.
     */
    public boolean setValueAndBlock(Port port, int value, long durationMilli) {
    	return cmd.setValueAndBlock(port,value,durationMilli);
    }

    /**
     * "Pulses" a given port, setting its state to True/On and them immediately
     * setting its state to False/Off.
     *
     * @param port {@link Port} to pulse.
     *
     * @return True if the port could be pulsed, and false otherwise.
     */
    public boolean digitalPulse(Port port) {
    	return cmd.digitalPulse(port);
    }

    /**
     * "Pulses" a given port, setting its state to True/On and them immediately
     * setting its state to False/Off.
     *
     * @param port {@link Port} to pulse.
     * @param durationNanos Time in nanoseconds to sustain the pulse for.
     *
     * @return True if the port could be pulsed, and false otherwise.
     */
    public boolean digitalPulse(Port port, long durationNanos) {
    	return cmd.digitalPulse(port, durationNanos);
    }
	
	
	/**
	 * start shutdown of the runtime, this can be vetoed or postponed by any shutdown listeners
	 */
	public void requestShutdown() {
		
		assert(cmd.enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			cmd.builder.requestShutdown();
		} finally {
		    assert(cmd.exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}

	
	
}
