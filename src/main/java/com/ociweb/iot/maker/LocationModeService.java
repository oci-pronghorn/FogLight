package com.ociweb.iot.maker;

import com.ociweb.gl.api.MsgCommandChannel;
import com.ociweb.pronghorn.image.schema.LocationModeSchema;
import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;

public class LocationModeService {

	private final FogCommandChannel cmd;
	
	public LocationModeService(FogCommandChannel fogCommandChannel) {
		this.cmd = fogCommandChannel;
		cmd.initFeatures |= FogCommandChannel.IMG_LOC_MODE;
	}

	public LocationModeService(FogCommandChannel fogCommandChannel, int commandCountCapacity, int maxMessageSize) {
		this.cmd = fogCommandChannel;
		FogCommandChannel.growCommandCountRoom(cmd, commandCountCapacity);
		cmd.initFeatures |= FogCommandChannel.IMG_LOC_MODE;
		
		PipeConfig<LocationModeSchema> config = MsgCommandChannel.PCM(cmd).getConfig(LocationModeSchema.class);
		if (FogCommandChannel.isTooSmall(commandCountCapacity, maxMessageSize, config)) {		
			MsgCommandChannel.PCM(cmd).addConfig(Math.max(config.minimumFragmentsOnPipe(), commandCountCapacity),
					           Math.max(config.maxVarLenSize(), maxMessageSize), LocationModeSchema.class);   
		}
	
	}
	
    public boolean hasRoomFor(int messageCount) {
		
		return cmd.goHasRoomFor(messageCount) 
		       && (null==cmd.locationModeOutput || Pipe.hasRoomForWrite(cmd.locationModeOutput, FieldReferenceOffsetManager.maxFragmentSize(Pipe.from(cmd.locationModeOutput))*messageCount));
		
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
