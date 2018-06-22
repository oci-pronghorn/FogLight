package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.image.schema.ImageRotationSchema;
import com.ociweb.pronghorn.image.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

/**
 * Stage for rotating and cropping images.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class ImageRotationStage extends PronghornStage {

    public static final int DEFAULT_CROP_WIDTH = 1280;
    public static final int DEFAULT_CROP_HEIGHT = 720;

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
    }

    @Override
    public void startup() {

    }

    @Override
    public void run() {

    }
}
