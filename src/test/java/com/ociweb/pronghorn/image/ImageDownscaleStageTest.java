package com.ociweb.pronghorn.image;

import com.ociweb.iot.maker.PiImageListenerStage;
import com.ociweb.pronghorn.iot.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ImageDownscaleStageTest {

    public static final int[] DOWNSCALE_RESOLUTION_ONE = {640, 360};

    public static class DownsamplePipeReaderState {
        File file = new File("");
        byte[] currentRow = null;
        byte[] currentFrame = null;
        int frameHead = 0;
    }

    public static void readFromDownsamplePipe(Pipe<ImageSchema> pipe, DownsamplePipeReaderState state, byte[] pipeEncoding) {
        if (PipeReader.tryReadFragment(pipe)) {
            switch (PipeReader.getMsgIdx(pipe)) {
                case ImageSchema.MSG_FRAMESTART_1:

                    // Extract data.
                    int width = PipeReader.readInt(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_WIDTH_101);
                    int height = PipeReader.readInt(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_HEIGHT_201);
                    long time = PipeReader.readLong(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_TIMESTAMP_301);
                    int frameSize = PipeReader.readInt(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401);
                    int bps = PipeReader.readInt(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_BITSPERPIXEL_501);
                    byte[] encoding = Arrays.copyOf(pipeEncoding, pipeEncoding.length);
                    PipeReader.readBytes(pipe, ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, encoding, 0);

                    // Validate data.
                    assert Arrays.equals(encoding, pipeEncoding);

                    // Print data.
                    System.out.printf("New Frame: W%dxH%d @ %d [Frame Bytes: %d, Bits per pixel: %d].\n", width, height, time, frameSize, bps);

                    // Prep buffer.
                    state.currentFrame = new byte[frameSize * 3];
                    state.frameHead = 0;
                    state.file = new File(new String(pipeEncoding) + "-" + time + ".raw");

                    break;

                case ImageSchema.MSG_FRAMECHUNK_2:

                    // If frame is full, skip.
                    if (state.frameHead >= state.currentFrame.length) {
                        break;
                    }

                    // Calculate row length.
                    int rowLength = PipeReader.readBytesLength(pipe, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102);

                    // Prepare array if not already ready.
                    if (state.currentRow == null || state.currentRow.length != rowLength) {
                        state.currentRow = new byte[rowLength];
                    }

                    // Read bytes into array.
                    PipeReader.readBytes(pipe, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102, state.currentRow, 0);

                    // Send bytes to frame.
                    for (int i = 0; i < state.currentRow.length; i++) {

                        // Switch by format.
                        if (pipeEncoding.equals(ImageDownscaleStage.R_OUTPUT_ENCODING)) {
                            state.currentFrame[state.frameHead] = state.currentRow[i];
                            state.currentFrame[state.frameHead + 1] = 0;
                            state.currentFrame[state.frameHead + 2] = 0;
                        } else if (pipeEncoding.equals(ImageDownscaleStage.G_OUTPUT_ENCODING)) {
                            state.currentFrame[state.frameHead] = 0;
                            state.currentFrame[state.frameHead + 1] = state.currentRow[i];
                            state.currentFrame[state.frameHead + 2] = 0;
                        } else if (pipeEncoding.equals(ImageDownscaleStage.B_OUTPUT_ENCODING)) {
                            state.currentFrame[state.frameHead] = 0;
                            state.currentFrame[state.frameHead + 1] = 0;
                            state.currentFrame[state.frameHead + 2] = state.currentRow[i];
                        } else if (pipeEncoding.equals(ImageDownscaleStage.MONO_OUTPUT_ENCODING)) {
                            state.currentFrame[state.frameHead] = state.currentRow[i];
                            state.currentFrame[state.frameHead + 1] = state.currentRow[i];
                            state.currentFrame[state.frameHead + 2] = state.currentRow[i];
                        }

                        state.frameHead += 3;
                    }

                    // If frame is full, flush.
                    if (state.frameHead >= state.currentFrame.length) {
                        System.out.println("Wrote " + state.file.getName().toString() + " to disk.");
                        try {
                            state.file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        try (FileOutputStream fos = new FileOutputStream(state.file)) {
                            fos.write(state.currentFrame);
                            fos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
            }

            PipeReader.releaseReadLock(pipe);
        }
    }

    public GraphManager gm;
    public Pipe<ImageSchema> imageInputPipe;
    public Pipe<ImageSchema>[] downsampleOutputPipes = new Pipe[4];
    public DownsamplePipeReaderState[] downsamplePipeReaderStates = new DownsamplePipeReaderState[downsampleOutputPipes.length];

    @Before
    public void setup() {

        // Setup graph manager,
        gm = new GraphManager();

        // Setup image production pipe.
        imageInputPipe = ImageSchema.instance.newPipe(1, PiImageListenerStage.ROW_SIZE * PiImageListenerStage.FRAME_HEIGHT);
        imageInputPipe.initBuffers();

        // Setup downsampling output pipes.
        for (int i = 0; i < downsampleOutputPipes.length; i++) {
            downsampleOutputPipes[i] = ImageSchema.instance.newPipe(1, DOWNSCALE_RESOLUTION_ONE[0] * DOWNSCALE_RESOLUTION_ONE[1]);
            downsampleOutputPipes[i].initBuffers();
        }

        // Setup downsample pipe reader states.
        for (int i = 0; i < downsamplePipeReaderStates.length; i++) {
            downsamplePipeReaderStates[i] = new DownsamplePipeReaderState();
        }

        // Setup stages.
        PiImageListenerStage imageListenerStage = new PiImageListenerStage(gm, imageInputPipe, 1);
        ImageDownscaleStage imageDownscaleStage = new ImageDownscaleStage(gm, imageInputPipe,
                                                                          downsampleOutputPipes,
                                                                          DOWNSCALE_RESOLUTION_ONE[0],
                                                                          DOWNSCALE_RESOLUTION_ONE[1]);
    }

    @Test
    @Ignore
    public void shouldDownscaleImages() {

        // Create test scheduler.
        NonThreadScheduler scheduler = new NonThreadScheduler(gm);
        scheduler.startup();

        // Run until the output pipes have data.
        // TODO: Stop condition?
        while (true) {

            // Run scheduler pipe.
            scheduler.run();

            // Read from pipes.
            for (int i = 0; i < downsampleOutputPipes.length; i++) {
                switch (i) {
                    case ImageDownscaleStage.R_OUTPUT_IDX:
                        readFromDownsamplePipe(downsampleOutputPipes[i], downsamplePipeReaderStates[i], ImageDownscaleStage.R_OUTPUT_ENCODING);
                        break;
                    case ImageDownscaleStage.G_OUTPUT_IDX:
                        readFromDownsamplePipe(downsampleOutputPipes[i], downsamplePipeReaderStates[i], ImageDownscaleStage.G_OUTPUT_ENCODING);
                        break;
                    case ImageDownscaleStage.B_OUTPUT_IDX:
                        readFromDownsamplePipe(downsampleOutputPipes[i], downsamplePipeReaderStates[i], ImageDownscaleStage.B_OUTPUT_ENCODING);
                        break;
                    case ImageDownscaleStage.MONO_OUTPUT_IDX:
                        readFromDownsamplePipe(downsampleOutputPipes[i], downsamplePipeReaderStates[i], ImageDownscaleStage.MONO_OUTPUT_ENCODING);
                        break;
                }
            }
        }
    }
}