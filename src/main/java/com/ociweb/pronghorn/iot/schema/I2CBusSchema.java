package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class I2CBusSchema extends MessageSchema<I2CBusSchema> {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400004,0x80000000,0x80000001,0x90000000,0xc0200004,0xc0400005,0x80000002,0x80000003,0x80000003,0x90000000,0xc0200005},
            (short)0,
            new String[]{"Point","Clock","Data","Time",null,"State","Task","Step","Byte","Time",null},
            new long[]{100, 101, 102, 103, 0, 200, 201, 202, 202, 103, 0},
            new String[]{"global",null,null,null,null,"global",null,null,null,null,null},
            "I2CBusSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final I2CBusSchema instance = new I2CBusSchema();
    
    private I2CBusSchema() {
        super(FROM);
    }

    public static final int MSG_POINT_100 = 0x00000000;
    public static final int MSG_POINT_100_FIELD_CLOCK_101 = 0x00000001;
    public static final int MSG_POINT_100_FIELD_DATA_102 = 0x00000002;
    public static final int MSG_POINT_100_FIELD_TIME_103 = 0x00800003;
    public static final int MSG_STATE_200 = 0x00000005;
    public static final int MSG_STATE_200_FIELD_TASK_201 = 0x00000001;
    public static final int MSG_STATE_200_FIELD_STEP_202 = 0x00000002;
    public static final int MSG_STATE_200_FIELD_BYTE_202 = 0x00000003;
    public static final int MSG_STATE_200_FIELD_TIME_103 = 0x00800004;


    public static void consume(Pipe<I2CBusSchema> input) {
        while (PipeReader.tryReadFragment(input)) {
            int msgIdx = PipeReader.getMsgIdx(input);
            switch(msgIdx) {
                case MSG_POINT_100:
                    consumePoint(input);
                break;
                case MSG_STATE_200:
                    consumeState(input);
                break;
                case -1:
                   //requestShutdown();
                break;
            }
            PipeReader.releaseReadLock(input);
        }
    }

    public static void consumePoint(Pipe<I2CBusSchema> input) {
        int fieldClock = PipeReader.readInt(input,MSG_POINT_100_FIELD_CLOCK_101);
        int fieldData = PipeReader.readInt(input,MSG_POINT_100_FIELD_DATA_102);
        long fieldTime = PipeReader.readLong(input,MSG_POINT_100_FIELD_TIME_103);
    }
    public static void consumeState(Pipe<I2CBusSchema> input) {
        int fieldTask = PipeReader.readInt(input,MSG_STATE_200_FIELD_TASK_201);
        int fieldStep = PipeReader.readInt(input,MSG_STATE_200_FIELD_STEP_202);
        int fieldByte = PipeReader.readInt(input,MSG_STATE_200_FIELD_BYTE_202);
        long fieldTime = PipeReader.readLong(input,MSG_STATE_200_FIELD_TIME_103);
    }

    public static boolean publishPoint(Pipe<I2CBusSchema> output, int fieldClock, int fieldData, long fieldTime) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_POINT_100)) {
            PipeWriter.writeInt(output,MSG_POINT_100_FIELD_CLOCK_101, fieldClock);
            PipeWriter.writeInt(output,MSG_POINT_100_FIELD_DATA_102, fieldData);
            PipeWriter.writeLong(output,MSG_POINT_100_FIELD_TIME_103, fieldTime);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }
    public static boolean publishState(Pipe<I2CBusSchema> output, int fieldTask, int fieldStep, int fieldByte, long fieldTime) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_STATE_200)) {
            PipeWriter.writeInt(output,MSG_STATE_200_FIELD_TASK_201, fieldTask);
            PipeWriter.writeInt(output,MSG_STATE_200_FIELD_STEP_202, fieldStep);
            PipeWriter.writeInt(output,MSG_STATE_200_FIELD_BYTE_202, fieldByte);
            PipeWriter.writeLong(output,MSG_STATE_200_FIELD_TIME_103, fieldTime);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }


    
}
