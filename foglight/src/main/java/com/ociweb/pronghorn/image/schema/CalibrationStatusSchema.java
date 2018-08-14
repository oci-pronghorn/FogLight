package com.ociweb.pronghorn.image.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class CalibrationStatusSchema extends MessageSchema<CalibrationStatusSchema> {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400003,0x80000000,0x80000001,0xc0200003},
		    (short)0,
		    new String[]{"CycleCalibrated","StartValue","TotalUnits",null},
		    new long[]{1, 12, 13, 0},
		    new String[]{"global",null,null,null},
		    "CalibrationStatusSchema.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});


		public CalibrationStatusSchema() { 
		    super(FROM);
		}

		protected CalibrationStatusSchema(FieldReferenceOffsetManager from) { 
		    super(from);
		}

		public static final CalibrationStatusSchema instance = new CalibrationStatusSchema();

		public static final int MSG_CYCLECALIBRATED_1 = 0x00000000; //Group/OpenTempl/3
		public static final int MSG_CYCLECALIBRATED_1_FIELD_STARTVALUE_12 = 0x00000001; //IntegerUnsigned/None/0
		public static final int MSG_CYCLECALIBRATED_1_FIELD_TOTALUNITS_13 = 0x00000002; //IntegerUnsigned/None/1

		public static void consume(Pipe<CalibrationStatusSchema> input) {
		    while (PipeReader.tryReadFragment(input)) {
		        int msgIdx = PipeReader.getMsgIdx(input);
		        switch(msgIdx) {
		            case MSG_CYCLECALIBRATED_1:
		                consumeCycleCalibrated(input);
		            break;
		            case -1:
		               //requestShutdown();
		            break;
		        }
		        PipeReader.releaseReadLock(input);
		    }
		}

		public static void consumeCycleCalibrated(Pipe<CalibrationStatusSchema> input) {
		    int fieldStartValue = PipeReader.readInt(input,MSG_CYCLECALIBRATED_1_FIELD_STARTVALUE_12);
		    int fieldTotalUnits = PipeReader.readInt(input,MSG_CYCLECALIBRATED_1_FIELD_TOTALUNITS_13);
		}

		public static void publishCycleCalibrated(Pipe<CalibrationStatusSchema> output, int fieldStartValue, int fieldTotalUnits) {
		        PipeWriter.presumeWriteFragment(output, MSG_CYCLECALIBRATED_1);
		        PipeWriter.writeInt(output,MSG_CYCLECALIBRATED_1_FIELD_STARTVALUE_12, fieldStartValue);
		        PipeWriter.writeInt(output,MSG_CYCLECALIBRATED_1_FIELD_TOTALUNITS_13, fieldTotalUnits);
		        PipeWriter.publishWrites(output);
		}

}
