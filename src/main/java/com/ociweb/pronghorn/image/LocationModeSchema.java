package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class LocationModeSchema extends MessageSchema<LocationModeSchema> {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400003,0x80000000,0x80000001,0xc0200003,0xc0400003,0x80000000,0x80000002,0xc0200003,0xc0400001,0xc0200001},
		    (short)0,
		    new String[]{"CycleLearningStart","StartValue","MaxSteps",null,"CycleLearningCompleted","StartValue",
		    "TotalSteps",null,"CycleLearningCancel",null},
		    new long[]{1, 12, 13, 0, 2, 12, 23, 0, 3, 0},
		    new String[]{"global",null,null,null,"global",null,null,null,"global",null},
		    "LocationModeSchema.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});


		public LocationModeSchema() { 
		    super(FROM);
		}

		protected LocationModeSchema(FieldReferenceOffsetManager from) { 
		    super(from);
		}

		public static final LocationModeSchema instance = new LocationModeSchema();

		public static final int MSG_CYCLELEARNINGSTART_1 = 0x00000000; //Group/OpenTempl/3
		public static final int MSG_CYCLELEARNINGSTART_1_FIELD_STARTVALUE_12 = 0x00000001; //IntegerUnsigned/None/0
		public static final int MSG_CYCLELEARNINGSTART_1_FIELD_MAXSTEPS_13 = 0x00000002; //IntegerUnsigned/None/1
		public static final int MSG_CYCLELEARNINGCOMPLETED_2 = 0x00000004; //Group/OpenTempl/3
		public static final int MSG_CYCLELEARNINGCOMPLETED_2_FIELD_STARTVALUE_12 = 0x00000001; //IntegerUnsigned/None/0
		public static final int MSG_CYCLELEARNINGCOMPLETED_2_FIELD_TOTALSTEPS_23 = 0x00000002; //IntegerUnsigned/None/2
		public static final int MSG_CYCLELEARNINGCANCEL_3 = 0x00000008; //Group/OpenTempl/1

		public static void consume(Pipe<LocationModeSchema> input) {
		    while (PipeReader.tryReadFragment(input)) {
		        int msgIdx = PipeReader.getMsgIdx(input);
		        switch(msgIdx) {
		            case MSG_CYCLELEARNINGSTART_1:
		                consumeCycleLearningStart(input);
		            break;
		            case MSG_CYCLELEARNINGCOMPLETED_2:
		                consumeCycleLearningCompleted(input);
		            break;
		            case MSG_CYCLELEARNINGCANCEL_3:
		                consumeCycleLearningCancel(input);
		            break;
		            case -1:
		               //requestShutdown();
		            break;
		        }
		        PipeReader.releaseReadLock(input);
		    }
		}

		public static void consumeCycleLearningStart(Pipe<LocationModeSchema> input) {
		    int fieldStartValue = PipeReader.readInt(input,MSG_CYCLELEARNINGSTART_1_FIELD_STARTVALUE_12);
		    int fieldMaxSteps = PipeReader.readInt(input,MSG_CYCLELEARNINGSTART_1_FIELD_MAXSTEPS_13);
		}
		public static void consumeCycleLearningCompleted(Pipe<LocationModeSchema> input) {
		    int fieldStartValue = PipeReader.readInt(input,MSG_CYCLELEARNINGCOMPLETED_2_FIELD_STARTVALUE_12);
		    int fieldTotalSteps = PipeReader.readInt(input,MSG_CYCLELEARNINGCOMPLETED_2_FIELD_TOTALSTEPS_23);
		}
		public static void consumeCycleLearningCancel(Pipe<LocationModeSchema> input) {
		}

		public static void publishCycleLearningStart(Pipe<LocationModeSchema> output, int fieldStartValue, int fieldMaxSteps) {
		        PipeWriter.presumeWriteFragment(output, MSG_CYCLELEARNINGSTART_1);
		        PipeWriter.writeInt(output,MSG_CYCLELEARNINGSTART_1_FIELD_STARTVALUE_12, fieldStartValue);
		        PipeWriter.writeInt(output,MSG_CYCLELEARNINGSTART_1_FIELD_MAXSTEPS_13, fieldMaxSteps);
		        PipeWriter.publishWrites(output);
		}
		public static void publishCycleLearningCompleted(Pipe<LocationModeSchema> output, int fieldStartValue, int fieldTotalSteps) {
		        PipeWriter.presumeWriteFragment(output, MSG_CYCLELEARNINGCOMPLETED_2);
		        PipeWriter.writeInt(output,MSG_CYCLELEARNINGCOMPLETED_2_FIELD_STARTVALUE_12, fieldStartValue);
		        PipeWriter.writeInt(output,MSG_CYCLELEARNINGCOMPLETED_2_FIELD_TOTALSTEPS_23, fieldTotalSteps);
		        PipeWriter.publishWrites(output);
		}
		public static void publishCycleLearningCancel(Pipe<LocationModeSchema> output) {
		        PipeWriter.presumeWriteFragment(output, MSG_CYCLELEARNINGCANCEL_3);
		        PipeWriter.publishWrites(output);
		}


}
