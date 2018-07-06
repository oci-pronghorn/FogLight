package com.ociweb.pronghorn.image;

import com.ociweb.iot.maker.LinuxImageCaptureStage;
import com.ociweb.pronghorn.image.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.ChannelReader;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Image processing stage which accepts an input image and outputs four
 * image pipes at a downscaled resolution:
 *
 * - A R color channel pipe containing {@code outputWidth} x {@code outputHeight} bytes.
 * - A G color channel pipe containing {@code outputWidth} x {@code outputHeight} bytes.
 * - A B color channel pipe containing {@code outputWidth} x {@code outputHeight} bytes.
 * - A Monochrome color channel pipe containing {@code outputWidth} x {@code outputHeight} bytes
 *   where each byte is the average of the R, G, and B bytes at that pixel.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class ImageDownscaleStage extends PronghornStage {

	private static final Logger logger = LoggerFactory.getLogger(ImageDownscaleStage.class);

    // Encoding assertion check
    private final ByteBuffer encodingBytes = ByteBuffer.wrap(new byte[32]);
    private final boolean assertEncoding() {

        // Read encoding.
        encodingBytes.position(0);
        encodingBytes.limit(encodingBytes.capacity());
        PipeReader.readBytes(input, ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, encodingBytes);

        // Encoding bytes should be the same as our expected input encoding.
        boolean assertResult = encodingBytes.position() == INPUT_ENCODING.length;
        for (int i = 0; i < encodingBytes.position(); i++) {
            assertResult = assertResult && (encodingBytes.get(i) == INPUT_ENCODING[i]);
        }

        return assertResult;
    }

    // Output pipe constants.
    private final Pipe<ImageSchema> input;
    private final Pipe<ImageSchema>[] outputs;
    private final int outputHeight;
    private final int outputWidth;

    // Input pipe information.
    private int imageFrameWidth = -1;
    private int imageFrameHeight = -1;
    private int inputFrameColumnsPerOutputColumn = -1;
    private int inputFrameRowsPerOutputFrameRow = -1;
    private int imageFrameRowsReceived = 0;
   
    private int[] imageFrameRowBytesDownsampled = null;
    private final byte[] imageFrameRowBytesR;
    private final byte[] imageFrameRowBytesG;
    private final byte[] imageFrameRowBytesB;
    private final byte[] imageFrameRowBytesMono;
    private final byte[][] imageFrameRowBytesLookup;

    // Pipe indices and encodings.
    public static final int R_OUTPUT_IDX = 0;
    public static final int G_OUTPUT_IDX = 1;
    public static final int B_OUTPUT_IDX = 2;
    public static final int MONO_OUTPUT_IDX = 3;
    public static final byte[] INPUT_ENCODING = "RGB24".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] R_OUTPUT_ENCODING = "R8".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] G_OUTPUT_ENCODING = "G8".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] B_OUTPUT_ENCODING = "B8".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] MONO_OUTPUT_ENCODING = "MONO8".getBytes(StandardCharsets.US_ASCII);

    int rows = 0;

    // Lookup table for encodings.
    public static final byte[][] OUTPUT_ENCODING_LOOKUP = new byte[][]{R_OUTPUT_ENCODING, G_OUTPUT_ENCODING, B_OUTPUT_ENCODING, MONO_OUTPUT_ENCODING};

    public static ImageDownscaleStage newInstance(GraphManager graphManager, Pipe<ImageSchema> input, Pipe<ImageSchema>[] outputs, int outputWidth, int outputHeight) {
        return new ImageDownscaleStage(graphManager, input, outputs, outputWidth, outputHeight);
    }

    public ImageDownscaleStage(GraphManager graphManager, Pipe<ImageSchema> input, Pipe<ImageSchema>[] outputs, int outputWidth, int outputHeight) {
        super(graphManager, input, outputs);

        // Validate and assign pipes.
        assert outputs.length == 4 : "Image downscaling stage expects R, G, B, and Monochrome output pipes.";
        this.input = input;
        this.outputs = outputs;

        // Assign configurations.
        this.outputHeight = outputHeight;
        this.outputWidth = outputWidth;

        // Setup frame row buffers.
        this.imageFrameRowBytesR = new byte[outputWidth];
        this.imageFrameRowBytesG = new byte[outputWidth];
        this.imageFrameRowBytesB = new byte[outputWidth];
        this.imageFrameRowBytesMono = new byte[outputWidth];
        this.imageFrameRowBytesLookup = new byte[][]{imageFrameRowBytesR, imageFrameRowBytesG, imageFrameRowBytesB, imageFrameRowBytesMono};
    }

    @Override
    public void run() {

        while (outputsHaveRoom() && PipeReader.tryReadFragment(input)) {
            int msgIdx = PipeReader.getMsgIdx(input);
            if (msgIdx == ImageSchema.MSG_FRAMECHUNK_2) {
                processChunk();
            } else if (msgIdx == ImageSchema.MSG_FRAMESTART_1) {
                processFrameStart();
            }
            PipeReader.releaseReadLock(input);
        }
    }

	private void processFrameStart() {

		// Extract message start data.
		imageFrameWidth = PipeReader.readInt(input, ImageSchema.MSG_FRAMESTART_1_FIELD_WIDTH_101);
		imageFrameHeight = PipeReader.readInt(input, ImageSchema.MSG_FRAMESTART_1_FIELD_HEIGHT_201);

		//logger.info("Downscaled frame started with original size {}x{}", imageFrameWidth, imageFrameHeight);

		// Ensure source resolution is evenly divisible by target resolution.
		assert imageFrameWidth % outputWidth == 0 &&
		       imageFrameHeight % outputHeight == 0 : "Source resolution must be evenly divisible by target resolution. width=" + outputWidth + ", height=" + outputHeight;

		// Validate encoding.
		assert assertEncoding() : "Encoding is not valid.";

		publishFrameStartToAll(System.currentTimeMillis(), outputWidth * outputHeight);

		// Prepare arrays if not already ready.
		if (imageFrameRowBytesDownsampled == null || imageFrameRowBytesDownsampled.length != outputWidth * 3) {
		    imageFrameRowBytesDownsampled = new int[outputWidth * 3];
		}

		// Calculate working frame sizes.
		inputFrameColumnsPerOutputColumn = imageFrameWidth / outputWidth;
		inputFrameRowsPerOutputFrameRow = imageFrameHeight / outputHeight;
	}

	private void publishFrameStartToAll(final long now, final int frameBytes) {
		// Write frame start to outputs.
		int i = outputs.length;
		while (--i>=0) {
		    Pipe<ImageSchema> pipe = outputs[i];
			if (PipeWriter.tryWriteFragment(pipe, ImageSchema.MSG_FRAMESTART_1)) {
		        // Write basic data.
		        PipeWriter.writeInt(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_WIDTH_101, outputWidth);
		        PipeWriter.writeInt(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_HEIGHT_201, outputHeight);
				PipeWriter.writeLong(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_TIMESTAMP_301, now);
				PipeWriter.writeInt(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401, frameBytes);
		        PipeWriter.writeInt(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_BITSPERPIXEL_501, 8);

		        // Write encoding.
		        PipeWriter.writeBytes(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, OUTPUT_ENCODING_LOOKUP[i]);

		        PipeWriter.publishWrites(pipe);

				//logger.info("Published new downscaled frame start... previous rows: {}", rows);
		        rows = 0;
		    } else {
		    	throw new UnsupportedOperationException("Should not happen since we checked already");
		    }
		}
	}

	private void processChunk() {
		accumulateRowData();
		// If we've summed enough frames to downsample height, generate an output frame.
		imageFrameRowsReceived++;
		if (imageFrameRowsReceived >= inputFrameRowsPerOutputFrameRow) {
		    finishComputeOfRow();			
			publishRowToAll();
		}
	}

	private void accumulateRowData() {
		// Read bytes into array.
		
		byte[] data = PipeReader.readBytesBackingArray(input, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102);
		int len = PipeReader.readBytesLength(input, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102);
		int mask = PipeReader.readBytesMask(input, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102);
		int pos = PipeReader.readBytesPosition(input, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102);
		
		int i = 0;
		           	            	
		// Downsample frame width.
		assert(imageFrameWidth*3 == len) : "internal error";
		
		int inputColumnsPerOutputColumn = inputFrameColumnsPerOutputColumn;
		int w = imageFrameWidth/inputColumnsPerOutputColumn;
		
		i = sumRow(data, mask, pos, i, inputColumnsPerOutputColumn, w);
		assert i == imageFrameRowBytesDownsampled.length;
	}

	private int sumRow(byte[] data, int mask, int pos, int i, int inputColumnsPerOutputColumn, int w) {
		int a = 0;
		int b = 0;
		int c = 0;
		
		while (--w>=0) {
		
			int k = inputColumnsPerOutputColumn;
			while (--k >= 0) {
			    // Add bytes to sum.
			    a += (data[mask& pos++] & 0xFF);
			    b += (data[mask& pos++] & 0xFF);
			    c += (data[mask& pos++] & 0xFF);
			}

		    imageFrameRowBytesDownsampled[i++] +=a;
		    imageFrameRowBytesDownsampled[i++] +=b;
		    imageFrameRowBytesDownsampled[i++] +=c;
			
	        a = 0;
	        b = 0;
	        c = 0;

		}
		return i;
	}

	private void finishComputeOfRow() {
		int i;
		imageFrameRowsReceived = 0;

		// Divide image frames by total pixels per cell.
		// Extract RGB and Mono channels.
		int inputPixelsPerOutputPixel = inputFrameColumnsPerOutputColumn * inputFrameRowsPerOutputFrameRow;

		i = 0;
		for (int j = 0; j < imageFrameRowBytesDownsampled.length; j += 3) {

		    // Extract RGB channels.
			int r = imageFrameRowBytesDownsampled[j] / inputPixelsPerOutputPixel;
			int g = imageFrameRowBytesDownsampled[j + 1] / inputPixelsPerOutputPixel;
			int b = imageFrameRowBytesDownsampled[j + 2] / inputPixelsPerOutputPixel;

		    assert r <= 255;
		    assert g <= 255;
		    assert b <= 255;

		    imageFrameRowBytesR[i] = (byte) r;
		    imageFrameRowBytesG[i] = (byte) g;
		    imageFrameRowBytesB[i] = (byte) b;

		    // Convert RGB24 pixel into monochrome.
		    imageFrameRowBytesMono[i] = (byte) ((0.2125 * r) + (0.7154 * g) + (0.0721 * b));

		    // Progress counter.
		    i++;
		}

		// Clear downsample bytes.
		Arrays.fill(imageFrameRowBytesDownsampled, 0);
	}

	private void publishRowToAll() {
		int k = outputs.length;
		while (--k>=0) {
		    Pipe<ImageSchema> pipe = outputs[k];
			if (PipeWriter.tryWriteFragment(pipe, ImageSchema.MSG_FRAMECHUNK_2)) {
		        PipeWriter.writeBytes(pipe,
		                              ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102,
		                              imageFrameRowBytesLookup[k], 0, imageFrameRowBytesLookup[k].length);
		        PipeWriter.publishWrites(pipe);
		    } else {
		    	throw new UnsupportedOperationException("Should not happen since we checked already");
		    }
		}
	}

	private boolean outputsHaveRoom() {
		int i = outputs.length;
		while (--i >= 0) {
			if (!PipeWriter.hasRoomForWrite(outputs[i])) {
				return false;
			}
		}
		return true;
	}
}
