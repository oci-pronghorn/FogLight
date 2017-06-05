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

	static FilterStage newInstance(GraphManager gm, Pipe<ValveSchema> input, Pipe<ValveSchema> output) {
		return new FilterStage(gm, input, output);
	}

	private FilterStage(GraphManager graphManager, Pipe<ValveSchema> input, Pipe<ValveSchema> output) {
		super(graphManager, input, output);
		this.input=input;
		this.output=input;
	}

	@Override
	public void startup() {
		MessageRead.startup();
	}

	@Override
	public void run() {
		while (PipeWriter.hasRoomForWrite(output) && PipeReader.tryReadFragment(input)) {
			int msgIdx = PipeReader.getMsgIdx(input);
			MessageRead msg = MessageRead.msg[msgIdx];
			msg.consume(input, output);
			PipeReader.releaseReadLock(input);
		}
	}
}

class MessageRead {
	private int msgIdx;
	private int stationId;
	private int timestampId;

	private int currentStationId;
	private long currentTimeStamp;

	static MessageRead[] msg;

	MessageRead(
			int msgIdx,
			int stationId,
			int timestampId) {
		this.msgIdx = msgIdx;
		this.stationId = stationId;
		this.timestampId = timestampId;
	}

	void consume(Pipe<ValveSchema> input, Pipe<ValveSchema> output) {
		currentStationId = PipeReader.readInt(input, stationId);
		currentTimeStamp = PipeReader.readLong(input, timestampId);
		if (readValue(input)) {
			publish(output);
		}
	}

	protected boolean readValue(Pipe<ValveSchema> input) {
		return true;
	}

	private boolean publish(Pipe<ValveSchema> output) {
		boolean result = false;
		if (PipeWriter.tryWriteFragment(output, msgIdx)) {
			PipeWriter.writeInt(output,stationId, currentStationId);
			PipeWriter.writeLong(output,timestampId, currentTimeStamp);
			writeValue(output);
			PipeWriter.publishWrites(output);
			result = true;
		}
		return result;
	}

	protected void writeValue(Pipe<ValveSchema> output) {
	}

	static void startup() {
		int messageIdCount = ValveSchema.FROM.fieldIdScript.length;
		msg = new MessageRead[messageIdCount];
		msg[MSG_VALVESERIALNUMBER_311] = new MessageReadInt(MSG_VALVESERIALNUMBER_311, MSG_VALVESERIALNUMBER_311_FIELD_STATION_1, MSG_VALVESERIALNUMBER_311_FIELD_TIMESTAMP_2, MSG_VALVESERIALNUMBER_311_FIELD_VALVESERIALNUMBER_11);
		msg[MSG_LIFECYCLECOUNT_312] = new MessageReadInt(MSG_LIFECYCLECOUNT_312, MSG_LIFECYCLECOUNT_312_FIELD_STATION_1, MSG_LIFECYCLECOUNT_312_FIELD_TIMESTAMP_2, MSG_LIFECYCLECOUNT_312_FIELD_LIFECYCLECOUNT_12);
		msg[MSG_SUPPLYPRESSURE_313] = new MessageReadInt(MSG_SUPPLYPRESSURE_313, MSG_SUPPLYPRESSURE_313_FIELD_STATION_1, MSG_SUPPLYPRESSURE_313_FIELD_TIMESTAMP_2, MSG_SUPPLYPRESSURE_313_FIELD_SUPPLYPRESSURE_13);
		msg[MSG_DURATIONOFLAST1_4SIGNAL_314] = new MessageReadInt(MSG_DURATIONOFLAST1_4SIGNAL_314, MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_STATION_1, MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_TIMESTAMP_2, MSG_DURATIONOFLAST1_4SIGNAL_314_FIELD_DURATIONOFLAST1_4SIGNAL_14);
		msg[MSG_DURATIONOFLAST1_2SIGNAL_315] = new MessageReadInt(MSG_DURATIONOFLAST1_2SIGNAL_315, MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_STATION_1, MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_TIMESTAMP_2, MSG_DURATIONOFLAST1_2SIGNAL_315_FIELD_DURATIONOFLAST1_2SIGNAL_15);
		msg[MSG_EQUALIZATIONAVERAGEPRESSURE_316] = new MessageReadInt(MSG_EQUALIZATIONAVERAGEPRESSURE_316, MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_STATION_1, MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_TIMESTAMP_2, MSG_EQUALIZATIONAVERAGEPRESSURE_316_FIELD_EQUALIZATIONAVERAGEPRESSURE_16);
		msg[MSG_EQUALIZATIONPRESSURERATE_317] = new MessageReadInt(MSG_EQUALIZATIONPRESSURERATE_317, MSG_EQUALIZATIONPRESSURERATE_317_FIELD_STATION_1, MSG_EQUALIZATIONPRESSURERATE_317_FIELD_TIMESTAMP_2, MSG_EQUALIZATIONPRESSURERATE_317_FIELD_EQUALIZATIONPRESSURERATE_17);
		msg[MSG_RESIDUALOFDYNAMICANALYSIS_318] = new MessageReadInt(MSG_RESIDUALOFDYNAMICANALYSIS_318, MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_STATION_1, MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_TIMESTAMP_2, MSG_RESIDUALOFDYNAMICANALYSIS_318_FIELD_RESIDUALOFDYNAMICANALYSIS_18);
		msg[MSG_PRESSUREPOINT_319] = new MessageReadInt(MSG_PRESSUREPOINT_319, MSG_PRESSUREPOINT_319_FIELD_STATION_1, MSG_PRESSUREPOINT_319_FIELD_TIMESTAMP_2, MSG_PRESSUREPOINT_319_FIELD_PRESSUREPOINT_19);
	//	msg[MSG_PARTNUMBER_320] = new MessageReadStr(MSG_PARTNUMBER_320, MSG_PARTNUMBER_320_FIELD_STATION_1, MSG_PARTNUMBER_320_FIELD_TIMESTAMP_2, MSG_PARTNUMBER_320_FIELD_PARTNUMBER_20);
	//	msg[MSG_PRESSUREFAULT_321] = new MessageReadStr(MSG_PRESSUREFAULT_321, MSG_PRESSUREFAULT_321_FIELD_STATION_1, MSG_PRESSUREFAULT_321_FIELD_TIMESTAMP_2, MSG_PRESSUREFAULT_321_FIELD_PRESSURELEVEL_21);
	//	msg[MSG_VALVEFAULT_322] = new MessageReadInt(MSG_VALVEFAULT_322, MSG_VALVEFAULT_322_FIELD_STATION_1, MSG_VALVEFAULT_322_FIELD_TIMESTAMP_2, MSG_VALVEFAULT_322_FIELD_FAULT_3);
	//	msg[MSG_LEAKFAULT_323] = new MessageReadInt(MSG_LEAKFAULT_323, MSG_LEAKFAULT_323_FIELD_STATION_1, MSG_LEAKFAULT_323_FIELD_TIMESTAMP_2, MSG_LEAKFAULT_323_FIELD_FAULT_3);
	//	msg[MSG_DATAFAULT_324] = new MessageReadInt(MSG_DATAFAULT_324, MSG_DATAFAULT_324_FIELD_STATION_1, MSG_DATAFAULT_324_FIELD_TIMESTAMP_2, MSG_DATAFAULT_324_FIELD_FAULT_3);
	}
}

class MessageReadInt extends MessageRead {
	private int valueId;
	private int currentValue;

	MessageReadInt(
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
	}
}

class MessageReadStr extends MessageRead {
	private int valueId;
	private String currentValue;

	MessageReadStr(
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
	//	PipeWriter.writeUTF8(output,MSG_PARTNUMBER_320_FIELD_PARTNUMBER_20, currentValue);
		PipeWriter.writeInt(output, valueId, valueId);
	}
}

