package com.ociweb.pronghorn.iot.schema;

import java.nio.ByteBuffer;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class I2CCommandSchema extends MessageSchema<I2CCommandSchema> {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400004,0x80000000,0x80000001,0xb8000000,0xc0200004,0xc0400004,0x80000000,0x80000001,0x90000000,0xc0200004,0xc0400004,0x80000000,0x80000001,0x90000001,0xc0200004,0xc0400002,0x90000000,0xc0200002,0xc0400002,0x90000001,0xc0200002},
		    (short)0,
		    new String[]{"Command","Connector","Address","ByteArray",null,"BlockConnection","Connector","Address","DurationNanos",null,"BlockConnectionUntil","Connector","Address","TimeMS",null,"BlockChannel","DurationNanos",null,"BlockChannelUntil","TimeMS",null},
		    new long[]{7, 11, 12, 2, 0, 20, 11, 12, 13, 0, 21, 11, 12, 14, 0, 22, 13, 0, 23, 14, 0},
		    new String[]{"global",null,null,null,null,"global",null,null,null,null,"global",null,null,null,null,"global",null,null,"global",null,null},
		    "I2CCommandSchema.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});
    
    public static final I2CCommandSchema instance = new I2CCommandSchema();
    


    
    protected I2CCommandSchema() {
        super(FROM);
    }
    
    public static final int MSG_COMMAND_7 = 0x00000000;
    public static final int MSG_COMMAND_7_FIELD_CONNECTOR_11 = 0x00000001;
    public static final int MSG_COMMAND_7_FIELD_ADDRESS_12 = 0x00000002;
    public static final int MSG_COMMAND_7_FIELD_BYTEARRAY_2 = 0x01c00003;
    public static final int MSG_BLOCKCONNECTION_20 = 0x00000005;
    public static final int MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11 = 0x00000001;
    public static final int MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12 = 0x00000002;
    public static final int MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13 = 0x00800003;
    public static final int MSG_BLOCKCONNECTIONUNTIL_21 = 0x0000000a;
    public static final int MSG_BLOCKCONNECTIONUNTIL_21_FIELD_CONNECTOR_11 = 0x00000001;
    public static final int MSG_BLOCKCONNECTIONUNTIL_21_FIELD_ADDRESS_12 = 0x00000002;
    public static final int MSG_BLOCKCONNECTIONUNTIL_21_FIELD_TIMEMS_14 = 0x00800003;
    public static final int MSG_BLOCKCHANNEL_22 = 0x0000000f;
    public static final int MSG_BLOCKCHANNEL_22_FIELD_DURATIONNANOS_13 = 0x00800001;
    public static final int MSG_BLOCKCHANNELUNTIL_23 = 0x00000012;
    public static final int MSG_BLOCKCHANNELUNTIL_23_FIELD_TIMEMS_14 = 0x00800001;


    public static void consume(Pipe<I2CCommandSchema> input) {
        while (PipeReader.tryReadFragment(input)) {
            int msgIdx = PipeReader.getMsgIdx(input);
            switch(msgIdx) {
                case MSG_COMMAND_7:
                    consumeCommand(input);
                break;
                case MSG_BLOCKCONNECTION_20:
                    consumeBlockConnection(input);
                break;
                case MSG_BLOCKCONNECTIONUNTIL_21:
                    consumeBlockConnectionUntil(input);
                break;
                case MSG_BLOCKCHANNEL_22:
                    consumeBlockChannel(input);
                break;
                case MSG_BLOCKCHANNELUNTIL_23:
                    consumeBlockChannelUntil(input);
                break;
                case -1:
                   //requestShutdown();
                break;
            }
            PipeReader.releaseReadLock(input);
        }
    }

    public static void consumeCommand(Pipe<I2CCommandSchema> input) {
        int fieldConnector = PipeReader.readInt(input,MSG_COMMAND_7_FIELD_CONNECTOR_11);
        int fieldAddress = PipeReader.readInt(input,MSG_COMMAND_7_FIELD_ADDRESS_12);
        ByteBuffer fieldByteArray = PipeReader.readBytes(input,MSG_COMMAND_7_FIELD_BYTEARRAY_2,ByteBuffer.allocate(PipeReader.readBytesLength(input,MSG_COMMAND_7_FIELD_BYTEARRAY_2)));
    }
    public static void consumeBlockConnection(Pipe<I2CCommandSchema> input) {
        int fieldConnector = PipeReader.readInt(input,MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11);
        int fieldAddress = PipeReader.readInt(input,MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12);
        long fieldDurationNanos = PipeReader.readLong(input,MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13);
    }
    public static void consumeBlockConnectionUntil(Pipe<I2CCommandSchema> input) {
        int fieldConnector = PipeReader.readInt(input,MSG_BLOCKCONNECTIONUNTIL_21_FIELD_CONNECTOR_11);
        int fieldAddress = PipeReader.readInt(input,MSG_BLOCKCONNECTIONUNTIL_21_FIELD_ADDRESS_12);
        long fieldTimeMS = PipeReader.readLong(input,MSG_BLOCKCONNECTIONUNTIL_21_FIELD_TIMEMS_14);
    }
    public static void consumeBlockChannel(Pipe<I2CCommandSchema> input) {
        long fieldDurationNanos = PipeReader.readLong(input,MSG_BLOCKCHANNEL_22_FIELD_DURATIONNANOS_13);
    }
    public static void consumeBlockChannelUntil(Pipe<I2CCommandSchema> input) {
        long fieldTimeMS = PipeReader.readLong(input,MSG_BLOCKCHANNELUNTIL_23_FIELD_TIMEMS_14);
    }

    public static boolean publishCommand(Pipe<I2CCommandSchema> output, int fieldConnector, int fieldAddress, byte[] fieldByteArrayBacking, int fieldByteArrayPosition, int fieldByteArrayLength) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_COMMAND_7)) {
            PipeWriter.writeInt(output,MSG_COMMAND_7_FIELD_CONNECTOR_11, fieldConnector);
            PipeWriter.writeInt(output,MSG_COMMAND_7_FIELD_ADDRESS_12, fieldAddress);
            PipeWriter.writeBytes(output,MSG_COMMAND_7_FIELD_BYTEARRAY_2, fieldByteArrayBacking, fieldByteArrayPosition, fieldByteArrayLength);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }
    public static boolean publishBlockConnection(Pipe<I2CCommandSchema> output, int fieldConnector, int fieldAddress, long fieldDurationNanos) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_BLOCKCONNECTION_20)) {
            PipeWriter.writeInt(output,MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11, fieldConnector);
            PipeWriter.writeInt(output,MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12, fieldAddress);
            PipeWriter.writeLong(output,MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13, fieldDurationNanos);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }
    public static boolean publishBlockConnectionUntil(Pipe<I2CCommandSchema> output, int fieldConnector, int fieldAddress, long fieldTimeMS) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_BLOCKCONNECTIONUNTIL_21)) {
            PipeWriter.writeInt(output,MSG_BLOCKCONNECTIONUNTIL_21_FIELD_CONNECTOR_11, fieldConnector);
            PipeWriter.writeInt(output,MSG_BLOCKCONNECTIONUNTIL_21_FIELD_ADDRESS_12, fieldAddress);
            PipeWriter.writeLong(output,MSG_BLOCKCONNECTIONUNTIL_21_FIELD_TIMEMS_14, fieldTimeMS);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }
    public static boolean publishBlockChannel(Pipe<I2CCommandSchema> output, long fieldDurationNanos) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_BLOCKCHANNEL_22)) {
            PipeWriter.writeLong(output,MSG_BLOCKCHANNEL_22_FIELD_DURATIONNANOS_13, fieldDurationNanos);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }
    public static boolean publishBlockChannelUntil(Pipe<I2CCommandSchema> output, long fieldTimeMS) {
        boolean result = false;
        if (PipeWriter.tryWriteFragment(output, MSG_BLOCKCHANNELUNTIL_23)) {
            PipeWriter.writeLong(output,MSG_BLOCKCHANNELUNTIL_23_FIELD_TIMEMS_14, fieldTimeMS);
            PipeWriter.publishWrites(output);
            result = true;
        }
        return result;
    }


        
}
