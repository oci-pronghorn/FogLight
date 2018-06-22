package com.ociweb.pronghorn.image;

import com.ociweb.iot.maker.LinuxImageCaptureStage;
import com.ociweb.pronghorn.image.schema.ImageRotationSchema;
import com.ociweb.pronghorn.image.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

/**
 * Stage for rotating and cropping images.
 *
 * This stage consumes and produces frames that are in the RGB24 encoding.
 *
 * {@see http://www.cipprs.org/papers/VI/VI1986/pp077-081-Paeth-1986.pdf}
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class ImageRotationStage extends PronghornStage {

    // Frame buffer state constants.
    private static final int FRAME_EMPTY = -1;
    private static final int FRAME_BUFFERED = -2;

    // Pipes.
    private final Pipe<ImageSchema> imagePipeIn;
    private final Pipe<ImageRotationSchema> imageRotationPipeIn;
    private final Pipe<ImageSchema> imagePipeOut;

    // Cropping information.
    private final int cropWidth;
    private final int cropHeight;

    // Information about the next input frame.
    private long nextInputFrameTimestamp;
    private int nextInputFrameWidth;
    private int nextInputFrameHeight;

    // Information about the current input frame.
    private long inputFrameTimestamp;
    private int inputFrameWidth;
    private int inputFrameHeight;
    private byte[] inputFrame;
    private int inputFrameHead = FRAME_EMPTY;

    // Information about the current output frame.
    private final byte[] outputFrame;
    private int outputFrameHead = FRAME_EMPTY;

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
    }

    @Override
    public void run() {

        // If we have no input frame - or if there is a new input frame - we must process it.
        if (inputFrameHead != FRAME_BUFFERED) {
            while (PipeReader.tryReadFragment(imagePipeIn)) {
                int msgIdx = PipeReader.getMsgIdx(imagePipeIn);
                switch (msgIdx) {
                    case ImageSchema.MSG_FRAMESTART_1:
                        break;
                    case ImageSchema.MSG_FRAMECHUNK_2:
                        break;
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
