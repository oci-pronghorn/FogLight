package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class LocationModeSchema extends MessageSchema<LocationModeSchema> {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400002,0x80000000,0xc0200002,0xc0400002,0x80000001,0xc0200002},
		    (short)0,
		    new String[]{"InitCalibration","StartValue",null,"State","Flags",null},
		    new long[]{1, 12, 0, 2, 22, 0},
		    new String[]{"global",null,null,"global",null,null},
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

		public static final int MSG_INITCALIBRATION_1 = 0x00000000; //Group/OpenTempl/2
		public static final int MSG_INITCALIBRATION_1_FIELD_STARTVALUE_12 = 0x00000001; //IntegerUnsigned/None/0
		public static final int MSG_STATE_2 = 0x00000003; //Group/OpenTempl/2
		public static final int MSG_STATE_2_FIELD_FLAGS_22 = 0x00000001; //IntegerUnsigned/None/1

		public static void consume(Pipe<LocationModeSchema> input) {
		    while (PipeReader.tryReadFragment(input)) {
		        int msgIdx = PipeReader.getMsgIdx(input);
		        switch(msgIdx) {
		            case MSG_INITCALIBRATION_1:
		                consumeInitCalibration(input);
		            break;
		            case MSG_STATE_2:
		                consumeState(input);
		            break;
		            case -1:
		               //requestShutdown();
		            break;
		        }
		        PipeReader.releaseReadLock(input);
		    }
		}

		public static void consumeInitCalibration(Pipe<LocationModeSchema> input) {
		    int fieldStartValue = PipeReader.readInt(input,MSG_INITCALIBRATION_1_FIELD_STARTVALUE_12);
		}
		public static void consumeState(Pipe<LocationModeSchema> input) {
		    int fieldFlags = PipeReader.readInt(input,MSG_STATE_2_FIELD_FLAGS_22);
		}

		public static void publishInitCalibration(Pipe<LocationModeSchema> output, int fieldStartValue) {
		        PipeWriter.presumeWriteFragment(output, MSG_INITCALIBRATION_1);
		        PipeWriter.writeInt(output,MSG_INITCALIBRATION_1_FIELD_STARTVALUE_12, fieldStartValue);
		        PipeWriter.publishWrites(output);
		}
		public static void publishState(Pipe<LocationModeSchema> output, int fieldFlags) {
		        PipeWriter.presumeWriteFragment(output, MSG_STATE_2);
		        PipeWriter.writeInt(output,MSG_STATE_2_FIELD_FLAGS_22, fieldFlags);
		        PipeWriter.publishWrites(output);
		}

}
