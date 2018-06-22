package com.ociweb.pronghorn.image;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.ImageListener;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ImageListenerTest {

    private static final int BACKPRESSURES_PER_INVOCATION = 2;
    private static final Path BACKPRESSURE_FRAME_SOURCE = Paths.get("src", "test", "images", "cat1-640-480.rgb");
    private static final int BACKPRESSURE_FRAME_WIDTH = 640;
    private static final int BACKPRESSURE_FRAME_HEIGHT = 480;

    // Read in backpressure test frame data.
    private static final byte[] BACKPRESSURE_EXPECTED_FRAME; static {
        try {
            BACKPRESSURE_EXPECTED_FRAME = Files.readAllBytes(BACKPRESSURE_FRAME_SOURCE);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private class BackpressureTestApp implements FogApp {

        int backpressuresSoFar = 0;
        byte[] frameBytes;
        int frameBytesHead = 0;

        @Override
        public void declareConnections(Hardware builder) {
            builder.setImageTriggerRate(1000);
            builder.setImageSize(BACKPRESSURE_FRAME_WIDTH, BACKPRESSURE_FRAME_HEIGHT);
            builder.setTestImageSource(BACKPRESSURE_FRAME_SOURCE);
        }

        @Override
        public void declareBehavior(FogRuntime fogRuntime) {
            fogRuntime.addImageListener(new ImageListener() {
                @Override
                public boolean onFrameStart(int width, int height,
                                            long timestamp, int frameBytesCount) {

                    // Apply backpressure.
                    if (backpressuresSoFar < BACKPRESSURES_PER_INVOCATION) {
                        backpressuresSoFar++;
                        return false;
                    }

                    backpressuresSoFar = 0;

                    // Read frame metadata.
                    Assert.assertEquals(BACKPRESSURE_FRAME_WIDTH, width);
                    Assert.assertEquals(BACKPRESSURE_FRAME_HEIGHT, height);
                    Assert.assertEquals(BACKPRESSURE_EXPECTED_FRAME.length, frameBytesCount);
                    frameBytes = new byte[frameBytesCount];
                    return true;
                }

                @Override
                public boolean onFrameRow(byte[] frameRowBytes) {

                    // Apply backpressure.
                    if (backpressuresSoFar < BACKPRESSURES_PER_INVOCATION) {
                        backpressuresSoFar++;
                        return false;
                    }

                    backpressuresSoFar = 0;

                    // Copy bytes.
                    System.arraycopy(frameRowBytes, 0, frameBytes, frameBytesHead, frameRowBytes.length);
                    frameBytesHead += frameRowBytes.length;

                    // Perform assertion and request shutdown if we have a full frame.
                    if (frameBytesHead >= frameBytes.length) {
                        Assert.assertTrue(Arrays.equals(BACKPRESSURE_EXPECTED_FRAME, frameBytes));
                        fogRuntime.shutdownRuntime();
                    }

                    return true;
                }
            });
        }
    }

    @Test
    public void shouldSupportBackpressure() {
        BackpressureTestApp app = new BackpressureTestApp();
        FogRuntime.testUntilShutdownRequested(app, 10_000);

        // Double-check.
        Assert.assertTrue(Arrays.equals(BACKPRESSURE_EXPECTED_FRAME, app.frameBytes));
    }
}
