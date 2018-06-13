package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.*;

public class ImageSchema extends MessageSchema<ImageSchema> {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400005,0x80000000,0x80000001,0x90000000,0x90000001,0xc0200005,0xc0400002,0xb8000000,0xc0200002},
            (short)0,
            new String[]{"FrameStart","Width","Height","Timestamp","FrameBytes",null,"FrameChunk","RowBytes",
                    null},
            new long[]{1, 101, 201, 301, 401, 0, 2, 102, 0},
            new String[]{"global",null,null,null,null,null,"global",null,null},
            "ImageSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});


    public ImageSchema() {
        super(FROM);
    }

    protected ImageSchema(FieldReferenceOffsetManager from) {
        super(from);
    }

    public static final ImageSchema instance = new ImageSchema();

    public static final int MSG_FRAMESTART_1 = 0x00000000; //Group/OpenTempl/5
    public static final int MSG_FRAMESTART_1_FIELD_WIDTH_101 = 0x00000001; //IntegerUnsigned/None/0
    public static final int MSG_FRAMESTART_1_FIELD_HEIGHT_201 = 0x00000002; //IntegerUnsigned/None/1
    public static final int MSG_FRAMESTART_1_FIELD_TIMESTAMP_301 = 0x00800003; //LongUnsigned/None/0
    public static final int MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401 = 0x00800005; //LongUnsigned/None/1
    public static final int MSG_FRAMECHUNK_2 = 0x00000006; //Group/OpenTempl/2
    public static final int MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102 = 0x01c00001; //ByteVector/None/0

    public static void consume(Pipe<ImageSchema> input) {
        while (PipeReader.tryReadFragment(input)) {
            int msgIdx = PipeReader.getMsgIdx(input);
            switch(msgIdx) {
                case MSG_FRAMESTART_1:
                    consumeFrameStart(input);
                    break;
                case MSG_FRAMECHUNK_2:
                    consumeFrameChunk(input);
                    break;
                case -1:
                    //requestShutdown();
                    break;
            }
            PipeReader.releaseReadLock(input);
        }
    }

    public static void consumeFrameStart(Pipe<ImageSchema> input) {
        int fieldWidth = PipeReader.readInt(input,MSG_FRAMESTART_1_FIELD_WIDTH_101);
        int fieldHeight = PipeReader.readInt(input,MSG_FRAMESTART_1_FIELD_HEIGHT_201);
        long fieldTimestamp = PipeReader.readLong(input,MSG_FRAMESTART_1_FIELD_TIMESTAMP_301);
        long fieldFrameBytes = PipeReader.readLong(input,MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401);
    }
    public static void consumeFrameChunk(Pipe<ImageSchema> input) {
        DataInputBlobReader<ImageSchema> fieldRowBytes = PipeReader.inputStream(input, MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102);
    }

    public static void publishFrameStart(Pipe<ImageSchema> output, int fieldWidth, int fieldHeight, long fieldTimestamp, long fieldFrameBytes) {
        PipeWriter.presumeWriteFragment(output, MSG_FRAMESTART_1);
        PipeWriter.writeInt(output,MSG_FRAMESTART_1_FIELD_WIDTH_101, fieldWidth);
        PipeWriter.writeInt(output,MSG_FRAMESTART_1_FIELD_HEIGHT_201, fieldHeight);
        PipeWriter.writeLong(output,MSG_FRAMESTART_1_FIELD_TIMESTAMP_301, fieldTimestamp);
        PipeWriter.writeLong(output,MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401, fieldFrameBytes);
        PipeWriter.publishWrites(output);
    }
    public static void publishFrameChunk(Pipe<ImageSchema> output, byte[] fieldRowBytesBacking, int fieldRowBytesPosition, int fieldRowBytesLength) {
        PipeWriter.presumeWriteFragment(output, MSG_FRAMECHUNK_2);
        PipeWriter.writeBytes(output,MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102, fieldRowBytesBacking, fieldRowBytesPosition, fieldRowBytesLength);
        PipeWriter.publishWrites(output);
    }

}
