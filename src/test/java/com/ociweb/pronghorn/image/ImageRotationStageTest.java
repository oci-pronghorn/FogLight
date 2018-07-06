package com.ociweb.pronghorn.image;

import com.ociweb.iot.maker.LinuxImageCaptureStage;
import com.ociweb.pronghorn.image.schema.ImageRotationSchema;
import com.ociweb.pronghorn.image.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ImageRotationStageTest {
    public static final int SOURCE_WIDTH = 640;
    public static final int SOURCE_ROW_SIZE = SOURCE_WIDTH * 3;
    public static final int SOURCE_HEIGHT = 480;
    public static final int ROTATION_WIDTH = 320;
    public static final int ROTATION_ROW_SIZE = ROTATION_WIDTH * 3;
    public static final int ROTATION_HEIGHT = 240;
    public static final long[] ROTATION_ANGLE = {180, 1};

    public static class RotationPipeReaderState {
        File file = new File("");
        byte[] currentRow = null;
        byte[] currentFrame = null;
        int frameHead = 0;
        int framesProcessed = 0;
    }

    public static void readFromRotationPipe(Pipe<ImageSchema> pipe, RotationPipeReaderState state, byte[] pipeEncoding) {
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
                    state.currentFrame = new byte[frameSize];
                    state.frameHead = 0;
                    state.file = new File( "rotated-" + time + ".raw");

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
                        state.currentFrame[state.frameHead] = state.currentRow[i];
                        state.frameHead++;
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

                        state.framesProcessed++;
                    }

                    break;
            }

            PipeReader.releaseReadLock(pipe);
        }
    }

    public GraphManager gm;
    public Pipe<ImageSchema> imageInputPipe;
    public Pipe<ImageRotationSchema> imageRotationInputPipe;
    public Pipe<ImageSchema> rotationOutputPipe;
    public RotationPipeReaderState rotationPipeReaderState;

    private void setup() {

        // Setup graph manager,
        gm = new GraphManager();

        // Setup image production pipe.
        imageInputPipe = ImageSchema.instance.newPipe(1, SOURCE_ROW_SIZE * SOURCE_HEIGHT);
        imageInputPipe.initBuffers();

        // Setup rotation request pipe.
        imageRotationInputPipe = ImageRotationSchema.instance.newPipe(1, 0);
        imageRotationInputPipe.initBuffers();

        // Setup rotation output pipe.
        rotationOutputPipe = ImageSchema.instance.newPipe(1, ROTATION_ROW_SIZE * ROTATION_HEIGHT);
        rotationOutputPipe.initBuffers();
        rotationPipeReaderState = new RotationPipeReaderState();

        // Setup stages.
        LinuxImageCaptureStage imageListenerStage = new LinuxImageCaptureStage(gm, imageInputPipe, 1000, SOURCE_WIDTH, SOURCE_HEIGHT, Paths.get("src", "test", "images", "cat1-640-480.rgb"));
        ImageRotationStage imageRotationStage = new ImageRotationStage(gm, imageInputPipe, imageRotationInputPipe, rotationOutputPipe, ROTATION_WIDTH, ROTATION_HEIGHT);
    }

    @Test
    @Ignore
    public void shouldDownscaleImagesTest() {

        int iterations = 5;
        int j = iterations;
        while (--j>=0) {

            setup();

            // Create test scheduler.
            NonThreadScheduler scheduler = new NonThreadScheduler(gm);
            scheduler.startup();

            // Put a rotation request on the pipe.
            if (PipeWriter.tryWriteFragment(imageRotationInputPipe, ImageRotationSchema.MSG_ROTATIONREQUEST_1)) {
                PipeWriter.writeLong(imageRotationInputPipe, ImageRotationSchema.MSG_ROTATIONREQUEST_1_FIELD_NUMERATOR_101, ROTATION_ANGLE[0]);
                PipeWriter.writeLong(imageRotationInputPipe, ImageRotationSchema.MSG_ROTATIONREQUEST_1_FIELD_DENOMINATOR_201, ROTATION_ANGLE[1]);
                PipeWriter.writeLong(imageRotationInputPipe, ImageRotationSchema.MSG_ROTATIONREQUEST_1_FIELD_TIMESTAMP_301,Long.MAX_VALUE);
                PipeWriter.publishWrites(imageRotationInputPipe);
            }

            // Run until we write a frame.
            while (rotationPipeReaderState.framesProcessed <= 0) {

                // Run scheduler pipe.
                scheduler.run();

                // Read from pipe.
                readFromRotationPipe(rotationOutputPipe, rotationPipeReaderState, LinuxImageCaptureStage.OUTPUT_ENCODING);
            }

            // Verify the frame is valid.
            try {
                Path filePath = Paths.get("src", "test", "images", "cat1-320-240.rotated");
                byte[] fileBytes = Files.readAllBytes(filePath);
                Assert.assertTrue(Arrays.equals(fileBytes, rotationPipeReaderState.currentFrame));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }
}