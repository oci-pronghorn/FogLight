package com.ociweb.pronghorn.iot.schema;

import java.nio.ByteBuffer;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class I2CResponseSchema extends MessageSchema<I2CResponseSchema> {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400005,0x80000000,0x90000000,0x80000001,0xb8000000,0xc0200005},
            (short)0,
            new String[]{"Response","Address","Time","Register","ByteArray",null},
            new long[]{10, 11, 13, 14, 12, 0},
            new String[]{"global",null,null,null,null,null},
            "I2CResponseSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    


    
    protected I2CResponseSchema() {
        super(FROM);
    }
    
    public static final I2CResponseSchema instance = new I2CResponseSchema();
    
    public static final int MSG_RESPONSE_10 = 0x00000000;
    public static final int MSG_RESPONSE_10_FIELD_ADDRESS_11 = 0x00000001;
    public static final int MSG_RESPONSE_10_FIELD_TIME_13 = 0x00800002;
    public static final int MSG_RESPONSE_10_FIELD_REGISTER_14 = 0x00000004;
    public static final int MSG_RESPONSE_10_FIELD_BYTEARRAY_12 = 0x01c00005;


    public static void consume(Pipe<I2CResponseSchema> input) {
        while (PipeReader.tryReadFragment(input)) {
            int msgIdx = PipeReader.getMsgIdx(input);
            switch(msgIdx) {
                case MSG_RESPONSE_10:
                    consumeResponse(input);
                break;
                case -1:
                   //requestShutdown();
                break;
            }
            PipeReader.releaseReadLock(input);
        }
    }

    public static void consumeResponse(Pipe<I2CResponseSchema> input) {
        int fieldAddress = PipeReader.readInt(input,MSG_RESPONSE_10_FIELD_ADDRESS_11);
        long fieldTime = PipeReader.readLong(input,MSG_RESPONSE_10_FIELD_TIME_13);
        int fieldRegister = PipeReader.readInt(input,MSG_RESPONSE_10_FIELD_REGISTER_14);
        ByteBuffer fieldByteArray = PipeReader.readBytes(input,MSG_RESPONSE_10_FIELD_BYTEARRAY_12,ByteBuffer.allocate(PipeReader.readBytesLength(input,MSG_RESPONSE_10_FIELD_BYTEARRAY_12)));
    }

    public static boolean publishResponse(Pipe<I2CResponseSchema> output, int fieldAddress, long fieldTime, int fieldRegister, byte[] fieldByteArrayBacking, int fieldByteArrayPosition, int fieldByteArrayLength) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_RESPONSE_10)) {
            PipeWriter.writeInt(output,MSG_RESPONSE_10_FIELD_ADDRESS_11, fieldAddress);
            PipeWriter.writeLong(output,MSG_RESPONSE_10_FIELD_TIME_13, fieldTime);
            PipeWriter.writeInt(output,MSG_RESPONSE_10_FIELD_REGISTER_14, fieldRegister);
            PipeWriter.writeBytes(output,MSG_RESPONSE_10_FIELD_BYTEARRAY_12, fieldByteArrayBacking, fieldByteArrayPosition, fieldByteArrayLength);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }

        
}
