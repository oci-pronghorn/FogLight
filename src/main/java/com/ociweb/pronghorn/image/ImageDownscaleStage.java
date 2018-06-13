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
 * Image processing stage which accepts an ImageSchema pipe and writes to
 * multiple output pipes of the same type.
 *
 * - This stage splits the image into R, G, B, and Monochrome images.
 *
 * - This stage downscales the resolution of the image based on a configurable
 *   setting in the constructor. E.G., given a resolution of 100 x 100, this
 *   stage will produce four output pipes with a 100x100 image in r, g, b, and
 *   monochrome.
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

    // Working output.
    private int imageFrameWidth = -1;
    private int imageFrameheight = -1;
    private int imageFrameSizeBytes = -1;
    private byte[] imageFrameRowBytes = null;
    private byte[] imageFrameRowBytesR = null;
    private byte[] imageFrameRowBytesG = null;
    private byte[] imageFrameRowBytesB = null;
    private byte[] imageFrameRowBytesMono = null;

    // Pipe indexes.
    public static final int R_OUTPUT_IDX = 0;
    public static final int G_OUTPUT_IDX = 1;
    public static final int B_OUTPUT_IDX = 2;
    public static final int MONO_OUTPUT_IDX = 3;

    // Image encodings.
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

                    // Calculate row length.
                    int rowLength = PipeReader.readBytesLength(input, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102);

                    // Prepare arrays if not already ready.
                    if (imageFrameRowBytes == null || imageFrameRowBytes.length != rowLength) {
                        imageFrameRowBytes = new byte[rowLength];
                        imageFrameRowBytesR = new byte[imageFrameRowBytes.length / 3];
                        imageFrameRowBytesG = new byte[imageFrameRowBytes.length / 3];
                        imageFrameRowBytesB = new byte[imageFrameRowBytes.length / 3];
                        imageFrameRowBytesMono = new byte[imageFrameRowBytes.length / 3];
                    }

                    // Read bytes into array.
                    PipeReader.readBytes(input, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102, imageFrameRowBytes, 0);

                    // Process channel data for bytes.
                    int monoAvg = 0;
                    int rPos = 0;
                    int gPos = 0;
                    int bPos = 0;
                    int monoPos = 0;
                    for (int i = 0; i < imageFrameRowBytes.length; i++) {

                        // R
                        if (i % 3 == 0) {

                            // If this is not the first iteration, also compute the mono.
                            if (i != 0) {
                                monoAvg = monoAvg / 3;
                                imageFrameRowBytesMono[monoPos] = (byte) monoAvg;
                                monoAvg = 0;
                                monoPos++;
                            }

                            imageFrameRowBytesR[rPos] = imageFrameRowBytes[i];
                            monoAvg += imageFrameRowBytes[i];
                            rPos++;

                        // G
                        } else if (i % 3 == 1) {
                            imageFrameRowBytesG[gPos] = imageFrameRowBytes[i];
                            monoAvg += imageFrameRowBytes[i];
                            gPos++;

                        // B
                        } else if (i % 3 == 2) {
                            imageFrameRowBytesB[bPos] = imageFrameRowBytes[i];
                            monoAvg += imageFrameRowBytes[i];
                            bPos++;
                        }
                    }

                    assert rPos == imageFrameRowBytesR.length;
                    assert gPos == imageFrameRowBytesG.length;
                    assert bPos == imageFrameRowBytesB.length;
                    assert monoPos == imageFrameRowBytesMono.length;

                    // Send channels to clients.
                    // TODO: Also, downsample.
                    for (int i = 0; i < outputs.length; i++) {
                        if (PipeWriter.tryWriteFragment(outputs[i], ImageSchema.MSG_FRAMECHUNK_2)) {
                            switch (i) {
                                case R_OUTPUT_IDX:
                                    PipeWriter.writeBytes(outputs[i], ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102, imageFrameRowBytesR, 0, rPos);
                                case G_OUTPUT_IDX:
                                    PipeWriter.writeBytes(outputs[i], ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102, imageFrameRowBytesG, 0, gPos);
                                case B_OUTPUT_IDX:
                                    PipeWriter.writeBytes(outputs[i], ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102, imageFrameRowBytesB, 0, bPos);
                                case MONO_OUTPUT_IDX:
                                    PipeWriter.writeBytes(outputs[i], ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102, imageFrameRowBytesMono, 0, monoPos);
                            }

                            PipeWriter.publishWrites(outputs[i]);
                        }
                    }

                    break;
            }

            PipeReader.releaseReadLock(input);
        }
    }
}
