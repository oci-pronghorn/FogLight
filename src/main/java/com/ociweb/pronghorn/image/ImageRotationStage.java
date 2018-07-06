package com.ociweb.pronghorn.image;

import com.ociweb.iot.maker.LinuxImageCaptureStage;
import com.ociweb.pronghorn.image.schema.ImageRotationSchema;
import com.ociweb.pronghorn.image.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

import java.util.Arrays;

/**
 * Stage for rotating and cropping images.
 *
 * This stage consumes and produces frames that are in the RGB24 encoding.
 *
 * {@see http://www.cipprs.org/papers/VI/VI1986/pp077-081-Paeth-1986.pdf}
 * {@see https://www.ocf.berkeley.edu/~fricke/projects/israel/paeth/rotation_by_shearing.html}
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class ImageRotationStage extends PronghornStage {

    private static final int NONE = -1;
    private static final int FRAME_BUFFERED = -2;

    // Pipes.
    private final Pipe<ImageSchema> imagePipeIn;
    private final Pipe<ImageRotationSchema> imageRotationPipeIn;
    private final Pipe<ImageSchema> imagePipeOut;

    // Cropping information.
    private final int cropWidth;
    private final int cropHeight;

    // Information about the next input frame.
    private long nextInputFrameTimestamp = NONE;
    private int nextInputFrameWidth;
    private int nextInputFrameHeight;
    private int nextInputFrameSize;

    // Information about the current input frame.
    private long inputFrameTimestamp = NONE;
    private int inputFrameWidth;
    private int inputFrameHeight;
    private byte[] inputFrameRow;
    private byte[] inputFrame;
    private int inputFrameHead = NONE;

    // Information about the current output frame.
    private long rotationRequestTimestamp = NONE;
    private double rotationAlpha, rotationBeta;
    private final byte[] outputFrame;
    private final int outputFrameRowSize;
    private int outputFrameHead = NONE;

    private static int cartesianToRgb24Index(int x, int y, int width) {
        return y * width * 3 + x * 3;
    }

    private static void shearRotateFrame(double alpha, double beta,
                                         byte[] source, int width, int height,
                                         byte[] destination, int destinationWidth, int destinationHeight) {

        // Iterate over all pixels in the frame.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // Get the pixel's index in the source.
                int sourcePixelIndex = source[cartesianToRgb24Index(x, y, width)];

                // Perform the three shearing operations.
                double destinationX = x + alpha * y;
                double destinationY = y + beta * x;
                destinationX = destinationX + alpha * destinationY;

                // Insert source pixel into destination array if it falls within bounds.
                if (destinationX >= 0 && destinationX < destinationWidth &&
                    destinationY >= 0 && destinationY < destinationHeight) {

                    // Get the pixel's index in the destination.
                    int destinationPixelIndex = cartesianToRgb24Index((int) destinationX, (int) destinationY, destinationWidth);

                    // Insert pixel values.
                    destination[destinationPixelIndex] = source[sourcePixelIndex];
                    destination[destinationPixelIndex + 1] = source[sourcePixelIndex + 1];
                    destination[destinationPixelIndex + 2] = source[sourcePixelIndex + 2];
                }
            }
        }
    }

    public static final int DEFAULT_CROP_WIDTH = LinuxImageCaptureStage.DEFAULT_FRAME_WIDTH;
    public static final int DEFAULT_CROP_HEIGHT = LinuxImageCaptureStage.DEFAULT_FRAME_HEIGHT;

    public ImageRotationStage(GraphManager gm,
                              Pipe<ImageSchema> imagePipeIn,
                              Pipe<ImageRotationSchema> imageRotationPipeIn,
                              Pipe<ImageSchema> imagePipeOut) {
        this(gm, imagePipeIn, imageRotationPipeIn, imagePipeOut, DEFAULT_CROP_WIDTH, DEFAULT_CROP_HEIGHT);
    }

    public ImageRotationStage(GraphManager gm,
                              Pipe<ImageSchema> imagePipeIn,
                              Pipe<ImageRotationSchema> imageRotationPipeIn,
                              Pipe<ImageSchema> imagePipeOut,
                              int cropWidth, int cropHeight) {
        super(gm, new Pipe[] {imagePipeIn, imageRotationPipeIn}, imagePipeOut);

        // Setup constant fields.
        this.imagePipeIn = imagePipeIn;
        this.imageRotationPipeIn = imageRotationPipeIn;
        this.imagePipeOut = imagePipeOut;
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;

        // Calculate output frame size based on RGB24 encoding.
        this.outputFrame = new byte[this.cropWidth * this.cropHeight * 3];
        this.outputFrameRowSize = this.cropWidth * 3;
    }

    @Override
    public void run() {

        // Read in a next frame if we do not have one.
        if (nextInputFrameTimestamp == NONE && PipeReader.tryReadFragment(imagePipeIn)) {
            assert PipeReader.getMsgIdx(imagePipeIn) == ImageSchema.MSG_FRAMESTART_1;
            nextInputFrameWidth = PipeReader.readInt(imagePipeIn, ImageSchema.MSG_FRAMESTART_1_FIELD_WIDTH_101);
            nextInputFrameHeight = PipeReader.readInt(imagePipeIn, ImageSchema.MSG_FRAMESTART_1_FIELD_HEIGHT_201);
            nextInputFrameTimestamp = PipeReader.readLong(imagePipeIn, ImageSchema.MSG_FRAMESTART_1_FIELD_TIMESTAMP_301);
            nextInputFrameSize = PipeReader.readInt(imagePipeIn, ImageSchema.MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401);
            assert inputFrameHead == NONE || inputFrameHead == FRAME_BUFFERED;
            inputFrameHead = 0;
        }

        // Read in a cropping request if we do not have one.
        if (rotationRequestTimestamp == NONE && PipeReader.tryReadFragment(imageRotationPipeIn)) {
            assert PipeReader.getMsgIdx(imageRotationPipeIn) == ImageRotationSchema.MSG_ROTATIONREQUEST_1;

            // Read in rotation data.
            long rotationRequestNumerator = PipeReader.readLong(imageRotationPipeIn, ImageRotationSchema.MSG_ROTATIONREQUEST_1_FIELD_NUMERATOR_101);
            long rotationRequestDenominator = PipeReader.readLong(imageRotationPipeIn, ImageRotationSchema.MSG_ROTATIONREQUEST_1_FIELD_DENOMINATOR_201);
            rotationRequestTimestamp = PipeReader.readLong(imageRotationPipeIn, ImageRotationSchema.MSG_ROTATIONREQUEST_1_FIELD_TIMESTAMP_301);

            // Calculate theta degrees from rotation data.
            double theta = rotationRequestNumerator / rotationRequestDenominator;

            // Calculate alpha, beta, and gamma for rotation data.
            rotationAlpha = -1 * Math.tan(theta / 2);
            rotationBeta = Math.sin(theta);
        }

        // Read in the next frame if the next frame's timestamp is older than the rotation request.
        if (nextInputFrameTimestamp != NONE && nextInputFrameTimestamp < rotationRequestTimestamp) {

            // Ensure image frame buffer is correctly sized.
            if (inputFrame == null || inputFrame.length != nextInputFrameSize) {
                inputFrame = new byte[nextInputFrameSize];
            }

            // Fill frame arrays.
            while (PipeReader.tryReadFragment(imagePipeIn)) {
                assert PipeReader.getMsgIdx(imagePipeIn) == ImageSchema.MSG_FRAMECHUNK_2;

                // Calculate row length.
                int rowLength = PipeReader.readBytesLength(imagePipeIn, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102);

                // Ensure image frame row buffer is correctly sized.
                if (inputFrameRow == null || inputFrameRow.length != rowLength) {
                    inputFrameRow = new byte[rowLength];
                }

                // If frame is full, we're done!
                if (inputFrameHead >= inputFrame.length) {
                    inputFrameTimestamp = nextInputFrameTimestamp;
                    inputFrameWidth = nextInputFrameWidth;
                    inputFrameHeight = nextInputFrameHeight;
                    inputFrameHead = FRAME_BUFFERED;
                    nextInputFrameTimestamp = NONE;
                    break;
                }
            }
        }

        // If we have an input frame buffered, have an active rotation request,
        // and the input frame predates the rotation request, write out a rotation.
        if (outputFrameHead != NONE ||
            (inputFrameHead == FRAME_BUFFERED && rotationRequestTimestamp != NONE && inputFrameTimestamp < rotationRequestTimestamp)) {

            // Frame start.
            if (outputFrameHead == NONE && PipeWriter.tryWriteFragment(imagePipeOut, ImageSchema.MSG_FRAMESTART_1)) {

                // Write start data to the pipe.
                PipeWriter.writeInt(imagePipeOut, ImageSchema.MSG_FRAMESTART_1_FIELD_WIDTH_101, cropWidth);
                PipeWriter.writeInt(imagePipeOut, ImageSchema.MSG_FRAMESTART_1_FIELD_HEIGHT_201, cropHeight);
                PipeWriter.writeLong(imagePipeOut, ImageSchema.MSG_FRAMESTART_1_FIELD_TIMESTAMP_301, System.currentTimeMillis());
                PipeWriter.writeInt(imagePipeOut, ImageSchema.MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401, outputFrame.length);
                PipeWriter.writeInt(imagePipeOut, ImageSchema.MSG_FRAMESTART_1_FIELD_BITSPERPIXEL_501, 24);
                PipeWriter.writeBytes(imagePipeOut, ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, LinuxImageCaptureStage.OUTPUT_ENCODING);
                PipeWriter.publishWrites(imagePipeOut);
                outputFrameHead = 0;

                // Reset the output frame.
                // This must be done because some positions in the frame may not be written to,
                // and thus should appear empty (black).
                Arrays.fill(outputFrame, (byte) 0x00);

                // Skew the input frame into our output frame.
                shearRotateFrame(rotationAlpha, rotationBeta, inputFrame, inputFrameWidth, inputFrameHeight, outputFrame, cropWidth, cropHeight);
            }

            // Write output frame data.
            if (outputFrameHead >= 0 && PipeWriter.tryWriteFragment(imagePipeOut, ImageSchema.MSG_FRAMECHUNK_2)) {

                // Write bytes.
                PipeWriter.writeBytes(imagePipeOut, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102, outputFrame, outputFrameHead, outputFrameRowSize);
                PipeWriter.publishWrites(imagePipeOut);

                // Progress head.
                outputFrameHead += outputFrameRowSize;

                // If the head exceeds the size of the frame bytes, we're done writing.
                if (outputFrameHead >= outputFrame.length) {
                    outputFrameHead = NONE;
                }
            }
        }
    }

    @Override
    public void shutdown() {
        if (Pipe.hasRoomForWrite(imagePipeOut, Pipe.EOF_SIZE)) {
            Pipe.publishEOF(imagePipeOut);
        }
    }
}
