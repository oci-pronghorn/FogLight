package com.ociweb.iot.valveManifold;

import com.ociweb.iot.valveManifold.schema.ValveSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

import java.util.Objects;

import static com.ociweb.iot.valveManifold.schema.ValveSchema.*;

class FilterStage extends PronghornStage {

	private final Pipe<ValveSchema> input;
	private final Pipe<ValveSchema> output;
	private MessageProcessor[] messages = null;

	static FilterStage newInstance(GraphManager gm, Pipe<ValveSchema> input, Pipe<ValveSchema> output) {
		return new FilterStage(gm, input, output);
	}

	private FilterStage(GraphManager graphManager, Pipe<ValveSchema> input, Pipe<ValveSchema> output) {
		super(graphManager, input, output);
		this.input=input;
		this.output=output;
	}

	@Override
	public void startup() {
		int messageIdCount = ValveSchema.FROM.fieldIdScript.length;
		messages = new MessageProcessor[messageIdCount];
		messages[MSG_VALVESERIALNUMBER_311] = new MessageProcessorInt(MSG_VALVESERIALNUMBER_311, MSG_VALVESERIALNUMBER_311_FIELD_STATION_1, MSG_VALVESERIALNUMBER_311_FIELD_TIMESTAMP_2, MSG_VALVESERIALNUMBER_311_FIELD_VALVESERIALNUMBER_11);
		messages[MSG_LIFECYCLECOUNT_312] = new MessageProcessorInt(MSG_LIFECYCLECOUNT_312, MSG_LIFECYCLECOUNT_312_FIELD_STATION_1, MSG_LIFECYCLECOUNT_312_FIELD_TIMESTAMP_2, MSG_LIFECYCLECOUNT_312_FIELD_LIFECYCLECOUNT_12);
		messages[MSG_SUPPLYPRESSURE_313] = new MessageProcessorInt(MSG_SUPPLYPRESSURE_313, MSG_SUPPLYPRESSURE_313_FIELD_STATION_1, MSG_SUPPLYPRESSURE_313_FIELD_TIMESTAMP_2, MSG_SUPPLYPRESSURE_313_FIELD_SUPPLYPRESSURE_13);
		messages[MSG_DURATIONOFLAST1_4SIGNAL_314] = new MessageProcessorInt(MSG_DURATIONOFLAST1_4SIGNAL_314, MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_STATION_1, MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_TIMESTAMP_2, MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_DURATIONOFLAST1_4SIGNAL_14);
		messages[MSG_DURATIONOFLAST1_2SIGNAL_315] = new MessageProcessorInt(MSG_DURATIONOFLAST1_2SIGNAL_315, MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_STATION_1, MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_TIMESTAMP_2, MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_DURATIONOFLAST1_2SIGNAL_15);
		messages[MSG_EQUALIZATIONAVERAGEPRESSURE_316] = new MessageProcessorInt(MSG_EQUALIZATIONAVERAGEPRESSURE_316, MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_STATION_1, MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_TIMESTAMP_2, MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_EQUALIZATIONAVERAGEPRESSURE_16);
		messages[MSG_EQUALIZATIONPRESSURERATE_317] = new MessageProcessorInt(MSG_EQUALIZATIONPRESSURERATE_317, MSG_EQUALIZATIONPRESSURERATE_317_FIELD_STATION_1, MSG_EQUALIZATIONPRESSURERATE_317_FIELD_TIMESTAMP_2, MSG_EQUALIZATIONPRESSURERATE_317_FIELD_EQUALIZATIONPRESSURERATE_17);
		messages[MSG_RESIDUALOFDYNAMICANALYSIS_318] = new MessageProcessorInt(MSG_RESIDUALOFDYNAMICANALYSIS_318, MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_STATION_1, MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_TIMESTAMP_2, MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_RESIDUALOFDYNAMICANALYSIS_18);
		messages[MSG_PRESSUREPOINT_319] = new MessageProcessorInt(MSG_PRESSUREPOINT_319, MSG_PRESSUREPOINT_319_FIELD_STATION_1, MSG_PRESSUREPOINT_319_FIELD_TIMESTAMP_2, MSG_PRESSUREPOINT_319_FIELD_PRESSUREPOINT_19);
		messages[MSG_PARTNUMBER_330] = new MessageProcessorStr(MSG_PARTNUMBER_330, MSG_PARTNUMBER_330_FIELD_STATION_1, MSG_PARTNUMBER_330_FIELD_TIMESTAMP_2, MSG_PARTNUMBER_330_FIELD_PARTNUMBER_30);
		messages[MSG_PRESSUREFAULT_350] = new MessageProcessorStr(MSG_PRESSUREFAULT_350, MSG_PRESSUREFAULT_350_FIELD_STATION_1, MSG_PRESSUREFAULT_350_FIELD_TIMESTAMP_2, MSG_PRESSUREFAULT_350_FIELD_PRESSUREFAULT_50);
		messages[MSG_LEAKFAULT_360] = new MessageProcessorInt(MSG_LEAKFAULT_360, MSG_LEAKFAULT_360_FIELD_STATION_1, MSG_LEAKFAULT_360_FIELD_TIMESTAMP_2, MSG_LEAKFAULT_360_FIELD_LEAKFAULT_60);
		messages[MSG_VALVEFAULT_340] = new MessageProcessorInt(MSG_VALVEFAULT_340, MSG_VALVEFAULT_340_FIELD_STATION_1, MSG_VALVEFAULT_340_FIELD_TIMESTAMP_2, MSG_VALVEFAULT_340_FIELD_VALVEFAULT_40);
		messages[MSG_DATAFAULT_362] = new MessageProcessorInt(MSG_DATAFAULT_362, MSG_DATAFAULT_362_FIELD_STATION_1, MSG_DATAFAULT_362_FIELD_TIMESTAMP_2, MSG_DATAFAULT_362_FIELD_DATAFAULT_62);
	}

	@Override
	public void run() {
		while (PipeWriter.hasRoomForWrite(output) && PipeReader.tryReadFragment(input)) {
			int msgIdx = PipeReader.getMsgIdx(input);
			//System.out.format("\nMessage Idx %d %d %d\n", msgIdx, Pipe.tailPosition(input), input.sizeOfSlabRing);
			if (msgIdx >= 0) {
				MessageProcessor msg = messages[msgIdx];
				if (msg != null) {
					msg.readAndWrite(input, output);
				}
				//else {
					//System.out.format("Not Found %d", msgIdx);
				//}
				PipeReader.releaseReadLock(input);
			}
			else {
				PipeReader.releaseReadLock(input);
				PipeWriter.publishEOF(output);
				requestShutdown();
				//System.out.format("End of the line");
				return;
			}
		}
	}
}

class MessageProcessor {
	private final int msgIdx;
	private final int stationFieldId;
	private final int timestampFieldId;

	private int currentStationId;
	private long currentTimeStamp;

	MessageProcessor(
			int msgIdx,
			int stationFieldId,
			int timestampId) {
		this.msgIdx = msgIdx;
		this.stationFieldId = stationFieldId;
		this.timestampFieldId = timestampId;
	}

	void readAndWrite(Pipe<ValveSchema> input, Pipe<ValveSchema> output) {
		currentStationId = PipeReader.readInt(input, stationFieldId);
		currentTimeStamp = PipeReader.readLong(input, timestampFieldId);
		if (readValue(input)) {
			publish(output);
		}
		//else {
		//	System.out.format("Filtered %d, %d, %d\n", msgIdx, currentStationId, currentTimeStamp);
		//}
	}

	protected boolean readValue(Pipe<ValveSchema> input) {
		return true;
	}

	private void publish(Pipe<ValveSchema> output) {
		PipeWriter.presumeWriteFragment(output, msgIdx);
		PipeWriter.writeInt(output, stationFieldId, currentStationId);
		PipeWriter.writeLong(output, timestampFieldId, currentTimeStamp);
		//System.out.format("Wrote %d, %d, %d\n", msgIdx, currentStationId, currentTimeStamp);
		writeValue(output);
		PipeWriter.publishWrites(output);
	}

	protected void writeValue(Pipe<ValveSchema> output) {
	}
}

class MessageProcessorInt extends MessageProcessor {
	private final int valueId;
	private int currentValue = Integer.MAX_VALUE;

	MessageProcessorInt(
			int msgIdx,
			int stationId,
			int timestampId,
			int valueId) {
		super(msgIdx, stationId, timestampId);
		this.valueId = valueId;
	}

	@Override
	protected boolean readValue(Pipe<ValveSchema> input) {
		int newValue = PipeReader.readInt(input,valueId);
		if (newValue != currentValue) {
			currentValue = newValue;
			return true;
		}
		return false;
	}

	@Override
	protected void writeValue(Pipe<ValveSchema> output) {
		PipeWriter.writeInt(output,valueId, currentValue);
		//System.out.format("Value %d, %d\n", valueId, currentValue);
	}
}

class MessageProcessorStr extends MessageProcessor {
	private final int valueId;
	private String currentValue = "_unknown_";

	MessageProcessorStr(
			int msgIdx,
			int stationId,
			int timestampId,
			int valueId) {
		super(msgIdx, stationId, timestampId);
		this.valueId = valueId;
	}

	@Override
	protected boolean readValue(Pipe<ValveSchema> input) {
		String newValue = PipeReader.readUTF8(input,valueId,new StringBuilder(PipeReader.readBytesLength(input,valueId))).toString();
		if (!Objects.equals(newValue, currentValue)) {
			currentValue = newValue;
			return true;
		}
		return false;
	}

	@Override
	protected void writeValue(Pipe<ValveSchema> output) {
		PipeWriter.writeUTF8(output, valueId, currentValue);
		//System.out.format("Value %d, %s\n", valueId, currentValue);
	}
}
