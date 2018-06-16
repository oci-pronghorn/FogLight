package com.ociweb.pronghorn.image;

import com.ociweb.iot.maker.PiImageListenerStage;
import com.ociweb.pronghorn.iot.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ImageDownscaleStageTest {

    public static final int[] DOWNSCALE_RESOLUTION_ONE = {640, 360};

    @Test
    public void shouldDownscaleImages() {

        // Setup graph manager and pipes.
        GraphManager gm = new GraphManager();
        Pipe<ImageSchema> imageSourcePipe = ImageSchema.instance.newPipe(1, PiImageListenerStage.ROW_SIZE * PiImageListenerStage.FRAME_HEIGHT);
        imageSourcePipe.initBuffers();
        Pipe<ImageSchema> imageRPipe = ImageSchema.instance.newPipe(1, DOWNSCALE_RESOLUTION_ONE[0] * DOWNSCALE_RESOLUTION_ONE[1]);
        imageRPipe.initBuffers();
        Pipe<ImageSchema> imageGPipe = ImageSchema.instance.newPipe(1, DOWNSCALE_RESOLUTION_ONE[0] * DOWNSCALE_RESOLUTION_ONE[1]);
        imageGPipe.initBuffers();
        Pipe<ImageSchema> imageBPipe = ImageSchema.instance.newPipe(1, DOWNSCALE_RESOLUTION_ONE[0] * DOWNSCALE_RESOLUTION_ONE[1]);
        imageBPipe.initBuffers();
        Pipe<ImageSchema> imageMonoPipe = ImageSchema.instance.newPipe(1, DOWNSCALE_RESOLUTION_ONE[0] * DOWNSCALE_RESOLUTION_ONE[1]);
        imageMonoPipe.initBuffers();

        // Setup stages.
        PiImageListenerStage imageListenerStage = new PiImageListenerStage(gm, imageSourcePipe, 1);
        ImageDownscaleStage imageDownscaleStage = new ImageDownscaleStage(gm, imageSourcePipe,
                                                                          new Pipe[] {imageRPipe, imageGPipe, imageBPipe, imageMonoPipe},
                                                                          DOWNSCALE_RESOLUTION_ONE[0], DOWNSCALE_RESOLUTION_ONE[1]);

        // Create test scheduler.
        NonThreadScheduler scheduler = new NonThreadScheduler(gm);
        scheduler.startup();

        // Run until the output pipes have data.
        // TODO: Stop condition?
        File file = new File("");
        byte[] currentRow = null;
        byte[] currentFrame = null;
        int frameHead = 0;
        while (true) {
            scheduler.run();

            if (PipeReader.tryReadFragment(imageMonoPipe)) {
                switch (PipeReader.getMsgIdx(imageMonoPipe)) {
                    case ImageSchema.MSG_FRAMESTART_1:

                        // Extract data.
                        int width = PipeReader.readInt(imageMonoPipe, ImageSchema.MSG_FRAMESTART_1_FIELD_WIDTH_101);
                        int height = PipeReader.readInt(imageMonoPipe, ImageSchema.MSG_FRAMESTART_1_FIELD_HEIGHT_201);
                        long time = PipeReader.readLong(imageMonoPipe, ImageSchema.MSG_FRAMESTART_1_FIELD_TIMESTAMP_301);
                        int frameSize = PipeReader.readInt(imageMonoPipe, ImageSchema.MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401);
                        int bps = PipeReader.readInt(imageMonoPipe, ImageSchema.MSG_FRAMESTART_1_FIELD_BITSPERPIXEL_501);
                        byte[] encoding = Arrays.copyOf(ImageDownscaleStage.MONO_OUTPUT_ENCODING, ImageDownscaleStage.MONO_OUTPUT_ENCODING.length);
                        PipeReader.readBytes(imageMonoPipe, ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, encoding, 0);

                        // Validate data.
                        assert Arrays.equals(encoding, ImageDownscaleStage.MONO_OUTPUT_ENCODING);

                        // Print data.
                        System.out.printf("New Frame: W%dxH%d @ %d [Frame Bytes: %d, Bits per pixel: %d].\n", width, height, time, frameSize, bps);

                        // Prep buffer.
                        currentFrame = new byte[frameSize * 3];
                        frameHead = 0;
                        file = new File("Mono-" + time + ".raw");

                        break;

                    case ImageSchema.MSG_FRAMECHUNK_2:

                        // If frame is full, skip.
                        if (frameHead >= currentFrame.length) {
                            break;
                        }

                        // Calculate row length.
                        int rowLength = PipeReader.readBytesLength(imageMonoPipe, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102);

                        // Prepare array if not already ready.
                        if (currentRow == null || currentRow.length != rowLength) {
                            currentRow = new byte[rowLength];
                        }

                        // Read bytes into array.
                        PipeReader.readBytes(imageMonoPipe, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102, currentRow, 0);

                        // Send bytes to frame.
                        for (int i = 0; i < currentRow.length; i++) {
                            currentFrame[frameHead] = currentRow[i];
                            currentFrame[frameHead + 1] = currentRow[i];
                            currentFrame[frameHead + 2] = currentRow[i];
                            frameHead += 3;
                        }

                        // If frame is full, flush.
                        if (frameHead >= currentFrame.length) {
                            System.out.println("Write frame.");
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                fos.write(currentFrame);
                                fos.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                }

                PipeReader.releaseReadLock(imageMonoPipe);
            }
        }
    }
}