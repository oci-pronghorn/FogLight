package com.ociweb.pronghorn.image;

import com.ociweb.iot.maker.PiImageListenerStage;
import com.ociweb.pronghorn.iot.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ImageDownscaleStageTest {

    public static final int SOURCE_WIDTH = 640;
    public static final int SOURCE_ROW_SIZE = SOURCE_WIDTH * 3;
    public static final int SOURCE_HEIGHT = 480;
    public static final int[] DOWNSCALE_RESOLUTION_ONE = {320, 240};

    public static class DownsamplePipeReaderState {
        File file = new File("");
        byte[] currentRow = null;
        byte[] currentFrame = null;
        int frameHead = 0;
        int filesWritten = 0;
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
                            state.currentFrame[state.frameHead + 1] = Byte.MAX_VALUE;
                            state.currentFrame[state.frameHead + 2] = Byte.MAX_VALUE;
                        } else if (pipeEncoding.equals(ImageDownscaleStage.G_OUTPUT_ENCODING)) {
                            state.currentFrame[state.frameHead] = Byte.MAX_VALUE;
                            state.currentFrame[state.frameHead + 1] = state.currentRow[i];
                            state.currentFrame[state.frameHead + 2] = Byte.MAX_VALUE;
                        } else if (pipeEncoding.equals(ImageDownscaleStage.B_OUTPUT_ENCODING)) {
                            state.currentFrame[state.frameHead] = Byte.MAX_VALUE;
                            state.currentFrame[state.frameHead + 1] = Byte.MAX_VALUE;
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
                        System.out.println("Wrote " + state.file.getName() + " to disk.");
                        try {
                            state.file.createNewFile();
                        } catch (IOException e) {
                            Assert.fail(e.getMessage());
                        }

                        try (FileOutputStream fos = new FileOutputStream(state.file)) {
                            fos.write(state.currentFrame);
                            fos.flush();
                        } catch (IOException e) {
                            Assert.fail(e.getMessage());
                        }

                        state.filesWritten++;
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
        imageInputPipe = ImageSchema.instance.newPipe(1, SOURCE_ROW_SIZE * SOURCE_HEIGHT);
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
        PiImageListenerStage imageListenerStage = new PiImageListenerStage(gm, imageInputPipe, 1, SOURCE_WIDTH, SOURCE_HEIGHT);
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

        // Run untill all pipes have written a file.
        boolean allWritten = false;
        while (!allWritten) {

            // Run scheduler pipe.
            scheduler.run();

            // Read from pipes.
            allWritten = true;
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

                allWritten = allWritten && downsamplePipeReaderStates[i].filesWritten > 0;
            }
        }
    }
}