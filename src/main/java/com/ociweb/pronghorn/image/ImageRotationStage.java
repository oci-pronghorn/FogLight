package com.ociweb.pronghorn.image;

import com.ociweb.iot.maker.LinuxImageCaptureStage;
import com.ociweb.pronghorn.image.schema.ImageRotationSchema;
import com.ociweb.pronghorn.image.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

/**
 * Stage for rotating and cropping images.
 *
 * This stage consumes and produces frames that are in the RGB24 encoding.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class ImageRotationStage extends PronghornStage {

    private final Pipe<ImageSchema> imagePipeIn;
    private final Pipe<ImageRotationSchema> imageRotationPipeIn;
    private final Pipe<ImageSchema> imagePipeOut;

    private final int cropWidth;
    private final int cropHeight;

    private final byte[] outputFrame;
    private int outputFrameHead = 0;
    private byte[] inputFrame;
    private int inputFrameHead = 0;

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

    }

    @Override
    public void shutdown() {
        if (Pipe.hasRoomForWrite(imagePipeOut, Pipe.EOF_SIZE)) {
            Pipe.publishEOF(imagePipeOut);
        }
    }
}
