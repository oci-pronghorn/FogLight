package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.iot.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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

    // Output pipe constants.
    private final Pipe<ImageSchema> input;
    private final Pipe<ImageSchema>[] outputs;
    private final int outputHeight;
    private final int outputWidth;
    private final ByteBuffer encodingBytes = ByteBuffer.wrap(new byte[32]);

    // Input pipe information.
    private int imageFrameWidth = -1;
    private int imageFrameheight = -1;
    private int imageFrameSizeBytes = -1;
    private int imageFrameRowsReceived = 0;
    private byte[] imageFrameRowBytes = null;
    private int[] imageFrameRowBytesDownsampled = null;
    private final byte[] imageFrameRowBytesR;
    private final byte[] imageFrameRowBytesG;
    private final byte[] imageFrameRowBytesB;
    private final byte[] imageFrameRowBytesMono;

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
    }

    @Override
    public void run() {
        while (PipeReader.tryReadFragment(input)) {
            int msgIdx = PipeReader.getMsgIdx(input);
            switch(msgIdx) {
                case ImageSchema.MSG_FRAMESTART_1:

                    // Extract message start data.
                    imageFrameWidth = PipeReader.readInt(input, ImageSchema.MSG_FRAMESTART_1_FIELD_WIDTH_101);
                    imageFrameheight = PipeReader.readInt(input, ImageSchema.MSG_FRAMESTART_1_FIELD_HEIGHT_201);
                    imageFrameSizeBytes = PipeReader.readInt(input, ImageSchema.MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401);

                    // Extract and verify encoding.
                    encodingBytes.position(0);
                    encodingBytes.limit(encodingBytes.capacity());
                    PipeReader.readBytes(input, ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, encodingBytes);
                    assert encodingBytes.position() == INPUT_ENCODING.length;
                    for (int i = 0; i < encodingBytes.position(); i++) {
                        assert encodingBytes.get(i) == INPUT_ENCODING[i];
                    }

                    // Write frame start to outputs.
                    for (int i = 0; i < outputs.length; i++) {
                        if (PipeWriter.tryWriteFragment(outputs[i], ImageSchema.MSG_FRAMESTART_1)) {

                            // Write basic data.
                            PipeWriter.writeInt(outputs[i], ImageSchema.MSG_FRAMESTART_1_FIELD_WIDTH_101, outputWidth);
                            PipeWriter.writeInt(outputs[i], ImageSchema.MSG_FRAMESTART_1_FIELD_HEIGHT_201, outputHeight);
                            PipeWriter.writeLong(outputs[i], ImageSchema.MSG_FRAMESTART_1_FIELD_TIMESTAMP_301, System.currentTimeMillis());
                            PipeWriter.writeInt(outputs[i], ImageSchema.MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401, outputWidth * outputHeight);
                            PipeWriter.writeInt(outputs[i], ImageSchema.MSG_FRAMESTART_1_FIELD_BITSPERPIXEL_501, 8);

                            // Write encoding.
                            switch (i) {
                                case R_OUTPUT_IDX:
                                    PipeWriter.writeBytes(outputs[i], ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, R_OUTPUT_ENCODING);
                                    break;

                                case G_OUTPUT_IDX:
                                    PipeWriter.writeBytes(outputs[i], ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, G_OUTPUT_ENCODING);
                                    break;

                                case B_OUTPUT_IDX:
                                    PipeWriter.writeBytes(outputs[i], ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, B_OUTPUT_ENCODING);
                                    break;

                                case MONO_OUTPUT_IDX:
                                    PipeWriter.writeBytes(outputs[i], ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, MONO_OUTPUT_ENCODING);
                                    break;
                            }

                            PipeWriter.publishWrites(outputs[i]);
                        }
                    }

                    break;

                case ImageSchema.MSG_FRAMECHUNK_2:

                    // Calculate working frame sizes.
                    int inputFrameColumnsPerOutputColumn = imageFrameWidth / outputWidth;
                    int inputFrameRowsPerOutputFrameRow = imageFrameheight / outputHeight;

                    // Determine row length.
                    int rowLength = PipeReader.readBytesLength(input, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102);

                    // Prepare arrays if not already ready.
                    if (imageFrameRowBytes == null || imageFrameRowBytes.length != rowLength) {
                        imageFrameRowBytes = new byte[rowLength];
                        imageFrameRowBytesDownsampled = new int[outputWidth * 3];
                    }

                    // Read bytes into array.
                    PipeReader.readBytes(input, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102, imageFrameRowBytes, 0);

                    // Downsample frame width.
                    int i = 0;
                    for (int j = 0; j < imageFrameRowBytes.length; j += 3) {

                        // Add bytes to sum.
                        imageFrameRowBytesDownsampled[i] += imageFrameRowBytes[j];
                        imageFrameRowBytesDownsampled[i + 1] += imageFrameRowBytes[j + 1];
                        imageFrameRowBytesDownsampled[i + 2] += imageFrameRowBytes[j + 2];

                        // If we've summed enough bytes, move to next output pixel.
                        if ((j / 3) >= inputFrameColumnsPerOutputColumn) {
                            i += 3;
                        }
                    }
                    assert i == imageFrameRowBytesDownsampled.length;

                    // If we've summed enough frames to downsample height, generate an output frame.
                    imageFrameRowsReceived++;
                    if (imageFrameRowsReceived >= inputFrameRowsPerOutputFrameRow) {

                        // Divide image frames by total pixels per cell.
                        int inputPixelsPerOutputPixel = inputFrameColumnsPerOutputColumn * inputFrameRowsPerOutputFrameRow;
                        for (i = 0; i < imageFrameRowBytesDownsampled.length; i += 3) {
                            imageFrameRowBytesDownsampled[i] = imageFrameRowBytesDownsampled[i] / inputPixelsPerOutputPixel;
                            imageFrameRowBytesDownsampled[i + 1] = imageFrameRowBytesDownsampled[i + 1] / inputPixelsPerOutputPixel;
                            imageFrameRowBytesDownsampled[i + 2] = imageFrameRowBytesDownsampled[i + 2] / inputPixelsPerOutputPixel;
                        }

                        // Extract RGB and Mono channels.
                        i = 0;
                        for (int j = 0; j < imageFrameRowBytesDownsampled.length; j += 3) {

                            // Extract RGB channels.
                            imageFrameRowBytesR[i] = (byte) imageFrameRowBytesDownsampled[j];
                            imageFrameRowBytesG[i] = (byte) imageFrameRowBytesDownsampled[j + 1];
                            imageFrameRowBytesB[i] = (byte) imageFrameRowBytesDownsampled[j + 2];

                            // Average bytes into mono channel.
                            int temp = 0;
                            temp += imageFrameRowBytesR[i];
                            temp += imageFrameRowBytesG[i];
                            temp += imageFrameRowBytesB[i];
                            temp = temp / 3;
                            imageFrameRowBytesMono[i] = (byte) temp;

                            // Progress counter.
                            i++;
                        }

                        // Send channels to clients.
                        // TODO: Refactor so that if a try-write fails, the row will be written during the next time slice.
                        for (i = 0; i < outputs.length; i++) {
                            if (PipeWriter.tryWriteFragment(outputs[i], ImageSchema.MSG_FRAMECHUNK_2)) {
                                switch (i) {
                                    case R_OUTPUT_IDX:
                                        PipeWriter.writeBytes(outputs[i],
                                                              ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102,
                                                              imageFrameRowBytesR, 0, imageFrameRowBytesR.length);
                                    case G_OUTPUT_IDX:
                                        PipeWriter.writeBytes(outputs[i],
                                                              ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102,
                                                              imageFrameRowBytesG, 0, imageFrameRowBytesG.length);
                                    case B_OUTPUT_IDX:
                                        PipeWriter.writeBytes(outputs[i],
                                                              ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102,
                                                              imageFrameRowBytesB, 0, imageFrameRowBytesB.length);
                                    case MONO_OUTPUT_IDX:
                                        PipeWriter.writeBytes(outputs[i],
                                                              ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102,
                                                              imageFrameRowBytesMono, 0, imageFrameRowBytesMono.length);
                                }

                                PipeWriter.publishWrites(outputs[i]);
                            }
                        }
                    }

                    break;
            }

            PipeReader.releaseReadLock(input);
        }
    }
}
