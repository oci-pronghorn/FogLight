package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
public class GroveResponseSchema extends MessageSchema<GroveResponseSchema> {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400005,0x80000000,0x90000000,0x80000001,0x90000001,0xc0200005,0xc0400005,0x80000002,0x90000000,0x80000003,0x90000002,0xc0200005,0xc0400007,0x80000004,0x90000000,0x88000005,0x88000006,0x80000007,0x90000003,0xc0200007},
            (short)0,
            new String[]{"DigitalSample","Connector","Time","Value","PrevDuration",null,"AnalogSample","Connector","Time","Value","PrevDuration",null,"Encoder","Connector","Time","Value","Delta","Speed","PrevDuration",null},
            new long[]{20, 21, 11, 22, 25, 0, 30, 31, 11, 32, 35, 0, 70, 71, 11, 72, 73, 74, 75, 0},
            new String[]{"global",null,null,null,null,null,"global",null,null,null,null,null,"global",null,null,null,null,null,null,null},
            "GroveResponse.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});


    
    private GroveResponseSchema() {
        super(FROM);
    }
    
    public static final GroveResponseSchema instance = new GroveResponseSchema();
    
    public static final int MSG_DIGITALSAMPLE_20 = 0x00000000;
    public static final int MSG_DIGITALSAMPLE_20_FIELD_CONNECTOR_21 = 0x00000001;
    public static final int MSG_DIGITALSAMPLE_20_FIELD_TIME_11 = 0x00800002;
    public static final int MSG_DIGITALSAMPLE_20_FIELD_VALUE_22 = 0x00000004;
    public static final int MSG_DIGITALSAMPLE_20_FIELD_PREVDURATION_25 = 0x00800005;
    public static final int MSG_ANALOGSAMPLE_30 = 0x00000006;
    public static final int MSG_ANALOGSAMPLE_30_FIELD_CONNECTOR_31 = 0x00000001;
    public static final int MSG_ANALOGSAMPLE_30_FIELD_TIME_11 = 0x00800002;
    public static final int MSG_ANALOGSAMPLE_30_FIELD_VALUE_32 = 0x00000004;
    public static final int MSG_ANALOGSAMPLE_30_FIELD_PREVDURATION_35 = 0x00800005;
    public static final int MSG_ENCODER_70 = 0x0000000c;
    public static final int MSG_ENCODER_70_FIELD_CONNECTOR_71 = 0x00000001;
    public static final int MSG_ENCODER_70_FIELD_TIME_11 = 0x00800002;
    public static final int MSG_ENCODER_70_FIELD_VALUE_72 = 0x00400004;
    public static final int MSG_ENCODER_70_FIELD_DELTA_73 = 0x00400005;
    public static final int MSG_ENCODER_70_FIELD_SPEED_74 = 0x00000006;
    public static final int MSG_ENCODER_70_FIELD_PREVDURATION_75 = 0x00800007;


    public static void consume(Pipe<GroveResponseSchema> input) {
        while (PipeReader.tryReadFragment(input)) {
            int msgIdx = PipeReader.getMsgIdx(input);
            switch(msgIdx) {
                case MSG_DIGITALSAMPLE_20:
                    consumeDigitalSample(input);
                break;
                case MSG_ANALOGSAMPLE_30:
                    consumeAnalogSample(input);
                break;
                case MSG_ENCODER_70:
                    consumeEncoder(input);
                break;
                case -1:
                   //requestShutdown();
                break;
            }
            PipeReader.releaseReadLock(input);
        }
    }

    public static void consumeDigitalSample(Pipe<GroveResponseSchema> input) {
        int fieldConnector = PipeReader.readInt(input,MSG_DIGITALSAMPLE_20_FIELD_CONNECTOR_21);
        long fieldTime = PipeReader.readLong(input,MSG_DIGITALSAMPLE_20_FIELD_TIME_11);
        int fieldValue = PipeReader.readInt(input,MSG_DIGITALSAMPLE_20_FIELD_VALUE_22);
        long fieldPrevDuration = PipeReader.readLong(input,MSG_DIGITALSAMPLE_20_FIELD_PREVDURATION_25);
    }
    public static void consumeAnalogSample(Pipe<GroveResponseSchema> input) {
        int fieldConnector = PipeReader.readInt(input,MSG_ANALOGSAMPLE_30_FIELD_CONNECTOR_31);
        long fieldTime = PipeReader.readLong(input,MSG_ANALOGSAMPLE_30_FIELD_TIME_11);
        int fieldValue = PipeReader.readInt(input,MSG_ANALOGSAMPLE_30_FIELD_VALUE_32);
        long fieldPrevDuration = PipeReader.readLong(input,MSG_ANALOGSAMPLE_30_FIELD_PREVDURATION_35);
    }
    public static void consumeEncoder(Pipe<GroveResponseSchema> input) {
        int fieldConnector = PipeReader.readInt(input,MSG_ENCODER_70_FIELD_CONNECTOR_71);
        long fieldTime = PipeReader.readLong(input,MSG_ENCODER_70_FIELD_TIME_11);
        int fieldValue = PipeReader.readInt(input,MSG_ENCODER_70_FIELD_VALUE_72);
        int fieldDelta = PipeReader.readInt(input,MSG_ENCODER_70_FIELD_DELTA_73);
        int fieldSpeed = PipeReader.readInt(input,MSG_ENCODER_70_FIELD_SPEED_74);
        long fieldPrevDuration = PipeReader.readLong(input,MSG_ENCODER_70_FIELD_PREVDURATION_75);
    }

    public static boolean publishDigitalSample(Pipe<GroveResponseSchema> output, int fieldConnector, long fieldTime, int fieldValue, long fieldPrevDuration) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_DIGITALSAMPLE_20)) {
            PipeWriter.writeInt(output,MSG_DIGITALSAMPLE_20_FIELD_CONNECTOR_21, fieldConnector);
            PipeWriter.writeLong(output,MSG_DIGITALSAMPLE_20_FIELD_TIME_11, fieldTime);
            PipeWriter.writeInt(output,MSG_DIGITALSAMPLE_20_FIELD_VALUE_22, fieldValue);
            PipeWriter.writeLong(output,MSG_DIGITALSAMPLE_20_FIELD_PREVDURATION_25, fieldPrevDuration);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }
    public static boolean publishAnalogSample(Pipe<GroveResponseSchema> output, int fieldConnector, long fieldTime, int fieldValue, long fieldPrevDuration) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_ANALOGSAMPLE_30)) {
            PipeWriter.writeInt(output,MSG_ANALOGSAMPLE_30_FIELD_CONNECTOR_31, fieldConnector);
            PipeWriter.writeLong(output,MSG_ANALOGSAMPLE_30_FIELD_TIME_11, fieldTime);
            PipeWriter.writeInt(output,MSG_ANALOGSAMPLE_30_FIELD_VALUE_32, fieldValue);
            PipeWriter.writeLong(output,MSG_ANALOGSAMPLE_30_FIELD_PREVDURATION_35, fieldPrevDuration);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }
    public static boolean publishEncoder(Pipe<GroveResponseSchema> output, int fieldConnector, long fieldTime, int fieldValue, int fieldDelta, int fieldSpeed, long fieldPrevDuration) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_ENCODER_70)) {
            PipeWriter.writeInt(output,MSG_ENCODER_70_FIELD_CONNECTOR_71, fieldConnector);
            PipeWriter.writeLong(output,MSG_ENCODER_70_FIELD_TIME_11, fieldTime);
            PipeWriter.writeInt(output,MSG_ENCODER_70_FIELD_VALUE_72, fieldValue);
            PipeWriter.writeInt(output,MSG_ENCODER_70_FIELD_DELTA_73, fieldDelta);
            PipeWriter.writeInt(output,MSG_ENCODER_70_FIELD_SPEED_74, fieldSpeed);
            PipeWriter.writeLong(output,MSG_ENCODER_70_FIELD_PREVDURATION_75, fieldPrevDuration);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }


        
}
