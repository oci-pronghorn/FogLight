package com.ociweb.iot.maker;

import com.ociweb.gl.api.MsgCommandChannel;
import com.ociweb.pronghorn.image.schema.LocationModeSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;

public class LocationModeSerivce {

	private final FogCommandChannel cmd;
	
	public LocationModeSerivce(FogCommandChannel fogCommandChannel) {
		this.cmd = fogCommandChannel;
		cmd.initFeatures |= FogCommandChannel.IMG_LOC_MODE;
	}

	public LocationModeSerivce(FogCommandChannel fogCommandChannel, int commandCountCapacity, int maxMessageSize) {
		this.cmd = fogCommandChannel;
		FogCommandChannel.growCommandCountRoom(cmd, commandCountCapacity);
		cmd.initFeatures |= FogCommandChannel.IMG_LOC_MODE;
		
		PipeConfig<LocationModeSchema> config = MsgCommandChannel.PCM(cmd).getConfig(LocationModeSchema.class);
		if (FogCommandChannel.isTooSmall(commandCountCapacity, maxMessageSize, config)) {		
			MsgCommandChannel.PCM(cmd).addConfig(Math.max(config.minimumFragmentsOnPipe(), commandCountCapacity),
					           Math.max(config.maxVarLenSize(), maxMessageSize), LocationModeSchema.class);   
		}
	
	}
	
	public boolean learnCycle(int baseLocation, int maxSteps) {
		
		if (Pipe.hasRoomForWrite(cmd.locationModeOutput)) {
			int size = Pipe.addMsgIdx(cmd.locationModeOutput, LocationModeSchema.MSG_CYCLELEARNINGSTART_1);
			Pipe.addIntValue(baseLocation, cmd.locationModeOutput);
			Pipe.addIntValue(maxSteps, cmd.locationModeOutput);
			Pipe.confirmLowLevelWrite(cmd.locationModeOutput, size);
			Pipe.publishWrites(cmd.locationModeOutput);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean cancel() {
		
		if (Pipe.hasRoomForWrite(cmd.locationModeOutput)) {
			int size = Pipe.addMsgIdx(cmd.locationModeOutput, LocationModeSchema.MSG_CYCLELEARNINGCANCEL_3);
			Pipe.confirmLowLevelWrite(cmd.locationModeOutput, size);
			Pipe.publishWrites(cmd.locationModeOutput);
			return true;
		} else {
			return false;
		}
	}
	
}
