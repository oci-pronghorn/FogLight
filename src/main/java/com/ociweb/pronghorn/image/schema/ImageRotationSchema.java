package com.ociweb.pronghorn.image.schema;

import com.ociweb.pronghorn.pipe.*;

public class ImageRotationSchema extends MessageSchema<ImageRotationSchema> {
    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400004,0x90000000,0x90000001,0x90000002,0xc0200004},
            (short)0,
            new String[]{"RotationRequest","Numerator","Denominator","Timestamp",null},
            new long[]{1, 101, 201, 301, 0},
            new String[]{"global",null,null,null,null},
            "ImageRotationSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});


    public ImageRotationSchema() {
        super(FROM);
    }

    protected ImageRotationSchema(FieldReferenceOffsetManager from) {
        super(from);
    }

    public static final ImageRotationSchema instance = new ImageRotationSchema();

    public static final int MSG_ROTATIONREQUEST_1 = 0x00000000; //Group/OpenTempl/4
    public static final int MSG_ROTATIONREQUEST_1_FIELD_NUMERATOR_101 = 0x00800001; //LongUnsigned/None/0
    public static final int MSG_ROTATIONREQUEST_1_FIELD_DENOMINATOR_201 = 0x00800003; //LongUnsigned/None/1
    public static final int MSG_ROTATIONREQUEST_1_FIELD_TIMESTAMP_301 = 0x00800005; //LongUnsigned/None/2

    public static void consume(Pipe<ImageRotationSchema> input) {
        while (PipeReader.tryReadFragment(input)) {
            int msgIdx = PipeReader.getMsgIdx(input);
            switch(msgIdx) {
                case MSG_ROTATIONREQUEST_1:
                    consumeRotationRequest(input);
                    break;
                case -1:
                    //requestShutdown();
                    break;
            }
            PipeReader.releaseReadLock(input);
        }
    }

    public static void consumeRotationRequest(Pipe<ImageRotationSchema> input) {
        long fieldNumerator = PipeReader.readLong(input,MSG_ROTATIONREQUEST_1_FIELD_NUMERATOR_101);
        long fieldDenominator = PipeReader.readLong(input,MSG_ROTATIONREQUEST_1_FIELD_DENOMINATOR_201);
        long fieldTimestamp = PipeReader.readLong(input,MSG_ROTATIONREQUEST_1_FIELD_TIMESTAMP_301);
    }

    public static void publishRotationRequest(Pipe<ImageRotationSchema> output, long fieldNumerator, long fieldDenominator, long fieldTimestamp) {
        PipeWriter.presumeWriteFragment(output, MSG_ROTATIONREQUEST_1);
        PipeWriter.writeLong(output,MSG_ROTATIONREQUEST_1_FIELD_NUMERATOR_101, fieldNumerator);
        PipeWriter.writeLong(output,MSG_ROTATIONREQUEST_1_FIELD_DENOMINATOR_201, fieldDenominator);
        PipeWriter.writeLong(output,MSG_ROTATIONREQUEST_1_FIELD_TIMESTAMP_301, fieldTimestamp);
        PipeWriter.publishWrites(output);
    }
}
