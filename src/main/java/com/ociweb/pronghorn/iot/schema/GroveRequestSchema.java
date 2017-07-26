package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
public class GroveRequestSchema extends MessageSchema<GroveRequestSchema> {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc1400003,0x80200000,0x80000001,0xc1200003,0xc1400003,0x80200000,0x90000000,0xc1200003,0xc1400003,0x80200000,0x90000001,0xc1200003,0xc1400003,0x80200002,0x80000003,0xc1200003},
		    (short)0,
		    new String[]{"DigitalSet","Connector","Value",null,"BlockConnection","Connector","DurationNanos",
		    null,"BlockConnectionUntil","Connector","TimeMS",null,"AnalogSet","Connector","Value",
		    null},
		    new long[]{110, 111, 112, 0, 220, 111, 13, 0, 221, 111, 114, 0, 140, 141, 142, 0},
		    new String[]{"global",null,null,null,"global",null,null,null,"global",null,null,null,"global",
		    null,null,null},
		    "GroveRequest.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});

    
    public static final GroveRequestSchema instance = new GroveRequestSchema();
    
    private GroveRequestSchema() {
        super(FROM);
    }
    
    public static final int MSG_DIGITALSET_110 = 0x00000000; //Group/OpenTemplPMap/3
    public static final int MSG_DIGITALSET_110_FIELD_CONNECTOR_111 = 0x00000001; //IntegerUnsigned/Copy/0
    public static final int MSG_DIGITALSET_110_FIELD_VALUE_112 = 0x00000002; //IntegerUnsigned/None/1
    public static final int MSG_BLOCKCONNECTION_220 = 0x00000004; //Group/OpenTemplPMap/3
    public static final int MSG_BLOCKCONNECTION_220_FIELD_CONNECTOR_111 = 0x00000001; //IntegerUnsigned/Copy/0
    public static final int MSG_BLOCKCONNECTION_220_FIELD_DURATIONNANOS_13 = 0x00800002; //LongUnsigned/None/0
    public static final int MSG_BLOCKCONNECTIONUNTIL_221 = 0x00000008; //Group/OpenTemplPMap/3
    public static final int MSG_BLOCKCONNECTIONUNTIL_221_FIELD_CONNECTOR_111 = 0x00000001; //IntegerUnsigned/Copy/0
    public static final int MSG_BLOCKCONNECTIONUNTIL_221_FIELD_TIMEMS_114 = 0x00800002; //LongUnsigned/None/1
    public static final int MSG_ANALOGSET_140 = 0x0000000c; //Group/OpenTemplPMap/3
    public static final int MSG_ANALOGSET_140_FIELD_CONNECTOR_141 = 0x00000001; //IntegerUnsigned/Copy/2
    public static final int MSG_ANALOGSET_140_FIELD_VALUE_142 = 0x00000002; //IntegerUnsigned/None/3


    public static void consume(Pipe<GroveRequestSchema> input) {
        while (PipeReader.tryReadFragment(input)) {
            int msgIdx = PipeReader.getMsgIdx(input);
            switch(msgIdx) {
                case MSG_DIGITALSET_110:
                    consumeDigitalSet(input);
                break;
                case MSG_BLOCKCONNECTION_220:
                    consumeBlockConnection(input);
                break;
                case MSG_BLOCKCONNECTIONUNTIL_221:
                    consumeBlockConnectionUntil(input);
                break;
                case MSG_ANALOGSET_140:
                    consumeAnalogSet(input);
                break;
                case -1:
                   //requestShutdown();
                break;
            }
            PipeReader.releaseReadLock(input);
        }
    }

    public static void consumeDigitalSet(Pipe<GroveRequestSchema> input) {
        int fieldConnector = PipeReader.readInt(input,MSG_DIGITALSET_110_FIELD_CONNECTOR_111);
        int fieldValue = PipeReader.readInt(input,MSG_DIGITALSET_110_FIELD_VALUE_112);
    }
    public static void consumeBlockConnection(Pipe<GroveRequestSchema> input) {
        int fieldConnector = PipeReader.readInt(input,MSG_BLOCKCONNECTION_220_FIELD_CONNECTOR_111);
        long fieldDurationNanos = PipeReader.readLong(input,MSG_BLOCKCONNECTION_220_FIELD_DURATIONNANOS_13);
    }
    public static void consumeBlockConnectionUntil(Pipe<GroveRequestSchema> input) {
        int fieldConnector = PipeReader.readInt(input,MSG_BLOCKCONNECTIONUNTIL_221_FIELD_CONNECTOR_111);
        long fieldTimeMS = PipeReader.readLong(input,MSG_BLOCKCONNECTIONUNTIL_221_FIELD_TIMEMS_114);
    }
    public static void consumeAnalogSet(Pipe<GroveRequestSchema> input) {
        int fieldConnector = PipeReader.readInt(input,MSG_ANALOGSET_140_FIELD_CONNECTOR_141);
        int fieldValue = PipeReader.readInt(input,MSG_ANALOGSET_140_FIELD_VALUE_142);
    }

    public static void publishDigitalSet(Pipe<GroveRequestSchema> output, int fieldConnector, int fieldValue) {
            PipeWriter.presumeWriteFragment(output, MSG_DIGITALSET_110);
            PipeWriter.writeInt(output,MSG_DIGITALSET_110_FIELD_CONNECTOR_111, fieldConnector);
            PipeWriter.writeInt(output,MSG_DIGITALSET_110_FIELD_VALUE_112, fieldValue);
            PipeWriter.publishWrites(output);
    }
    public static void publishBlockConnection(Pipe<GroveRequestSchema> output, int fieldConnector, long fieldDurationNanos) {
            PipeWriter.presumeWriteFragment(output, MSG_BLOCKCONNECTION_220);
            PipeWriter.writeInt(output,MSG_BLOCKCONNECTION_220_FIELD_CONNECTOR_111, fieldConnector);
            PipeWriter.writeLong(output,MSG_BLOCKCONNECTION_220_FIELD_DURATIONNANOS_13, fieldDurationNanos);
            PipeWriter.publishWrites(output);
    }
    public static void publishBlockConnectionUntil(Pipe<GroveRequestSchema> output, int fieldConnector, long fieldTimeMS) {
            PipeWriter.presumeWriteFragment(output, MSG_BLOCKCONNECTIONUNTIL_221);
            PipeWriter.writeInt(output,MSG_BLOCKCONNECTIONUNTIL_221_FIELD_CONNECTOR_111, fieldConnector);
            PipeWriter.writeLong(output,MSG_BLOCKCONNECTIONUNTIL_221_FIELD_TIMEMS_114, fieldTimeMS);
            PipeWriter.publishWrites(output);
    }
    public static void publishAnalogSet(Pipe<GroveRequestSchema> output, int fieldConnector, int fieldValue) {
            PipeWriter.presumeWriteFragment(output, MSG_ANALOGSET_140);
            PipeWriter.writeInt(output,MSG_ANALOGSET_140_FIELD_CONNECTOR_141, fieldConnector);
            PipeWriter.writeInt(output,MSG_ANALOGSET_140_FIELD_VALUE_142, fieldValue);
            PipeWriter.publishWrites(output);
    }

}
