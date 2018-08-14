package com.ociweb.iot.hardware.impl;

import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class SerialDataSchema<T extends SerialDataSchema<T>> extends MessageSchema<T> {
	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400002,0xb8000000,0xc0200002},
		    (short)0,
		    new String[]{"ChunkedStream","ByteArray",null},
		    new long[]{1, 2, 0},
		    new String[]{"global",null,null},
		    "UARTDataSchema.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});


		protected SerialDataSchema() { 
		    super(FROM);
		}

		public static final SerialDataSchema instance = new SerialDataSchema();
		
		public static final int MSG_CHUNKEDSTREAM_1 = 0x00000000; //Group/OpenTempl/2
		public static final int MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2 = 0x01c00001; //ByteVector/None/0


		public static <T extends SerialDataSchema<T>> void consume(Pipe<T> input) {
		    while (PipeReader.tryReadFragment(input)) {
		        int msgIdx = PipeReader.getMsgIdx(input);
		        switch(msgIdx) {
		            case MSG_CHUNKEDSTREAM_1:
		                consumeChunkedStream(input);
		            break;
		            case -1:
		               //requestShutdown();
		            break;
		        }
		        PipeReader.releaseReadLock(input);
		    }
		}

		public static <T extends SerialDataSchema<T>> void consumeChunkedStream(Pipe<T> input) {
		    DataInputBlobReader<T> fieldByteArray = PipeReader.inputStream(input, MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
		}

		public static <T extends SerialDataSchema<T>> void publishChunkedStream(Pipe<T> output, byte[] fieldByteArrayBacking, int fieldByteArrayPosition, int fieldByteArrayLength) {
		        PipeWriter.presumeWriteFragment(output, MSG_CHUNKEDSTREAM_1);
		        PipeWriter.writeBytes(output,MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2, fieldByteArrayBacking, fieldByteArrayPosition, fieldByteArrayLength);
		        PipeWriter.publishWrites(output);
		}

		
}
