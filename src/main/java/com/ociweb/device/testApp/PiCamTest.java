package com.ociweb.device.testApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ociweb.iot.maker.*;

/**
 * Simple Pi image capture test.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class PiCamTest implements FogApp {

    private final List<File> images = new ArrayList<>();
    private byte[] frameBytes = null;
    private int frameBytesHead = 0;
    private long start = -1;

    public static void main( String[] args) {
        FogRuntime.run(new PiCamTest());
    }

    @Override
    public void declareConnections(Hardware hardware) {
        hardware.setImageTriggerRate(1);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.addImageListener(new ImageListener() {

            File workingFile = null;

            @Override
            public boolean onFrameStart(int width, int height, long timestamp, int frameBytesCount) {

                // Prepare file.
                workingFile = new File("image-" + timestamp + ".raw");
                start = System.currentTimeMillis();

                // Prepare byte array.
                if (frameBytes == null || frameBytes.length != frameBytesCount) {
                    frameBytes = new byte[frameBytesCount];
                    System.out.printf("Created new frame buffer for frames of size %dW x %dH with %d bytes.\n", width, height, frameBytesCount);
                }

                frameBytesHead = 0;

                return true;
            }

            @Override
            public boolean onFrameRow(byte[] frameRowBytes) {

                // Copy bytes.
                System.arraycopy(frameRowBytes, 0, frameBytes, frameBytesHead, frameRowBytes.length);
                frameBytesHead += frameRowBytes.length;

                // Flush to disk if we have a full frame.
                if (frameBytesHead >= frameBytes.length) {

                    // Clean existing files if there are too many.
                    if (images.size() > 50) {
                        File f = images.get(0);
                        images.remove(0);
                        f.delete();
                    }

                    // Write file.
                    try {
                        workingFile.createNewFile();
                        FileOutputStream fos = new FileOutputStream(workingFile);
                        fos.write(frameBytes);
                        fos.flush();
                        fos.close();
                        images.add(workingFile);
                        System.out.printf("Captured image to disk @ %d (took %d milliseconds).\n", System.currentTimeMillis(), System.currentTimeMillis() - start);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
        });
    }
}
