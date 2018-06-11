package com.ociweb.device.testApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.ImageListener;
import com.ociweb.pronghorn.iot.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;

/**
 * Simple Pi image capture test.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class PiCamTest implements FogApp {

    private final List<File> images = new ArrayList<>();
    private byte[] frameBytes = null;
    private int frameBytesLength = -1;
    private int frameBytesHead = 0;

    public static void main( String[] args) {
        FogRuntime.run(new PiCamTest());
    }

    @Override
    public void declareConnections(Hardware hardware) {
        hardware.setImageTriggerRate(30);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.addImageListener(new ImageListener() {

            File workingFile = null;

            @Override
            public void onFrameStart(int width, int height, long timestamp) {

                // Clean existing files if there are many.
                if (images.size() > 50) {
                    File f = images.get(0);
                    images.remove(0);
                    f.delete();
                }

                // Open new file.
                try {
                    File newWorkingFile = new File("image-" + timestamp + ".raw");
                    newWorkingFile.createNewFile();
                    images.add(newWorkingFile);
                    workingFile = newWorkingFile;
                    System.out.printf("Began new working file for image W%dxH%d\n", width, height);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Prepare byte array.
                frameBytesLength = width * height * 3;
                if (frameBytes == null || frameBytes.length != frameBytesLength) {
                    frameBytes = new byte[frameBytesLength];
                    System.out.printf("Created new frame buffer for frames of size %dW x %dH.\n", width, height);
                }
                frameBytesHead = 0;
            }

            @Override
            public void onFrameRow(byte[] frameRowBytes) {

                // Perform frame copy.
                try {
                    System.arraycopy(frameRowBytes, 0, frameBytes, frameBytesHead, frameRowBytes.length);
                    frameBytesHead += frameRowBytes.length;

                    // Flush to disk if we have a full frame.
                    if (frameBytesHead >= frameBytesLength) {
                        try (FileOutputStream fos = new FileOutputStream(workingFile)) {
                            fos.write(frameBytes);
                            fos.flush();
                            System.out.printf("Captured new image to disk: %s.\n", images.get(images.size() - 1).getName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.out.printf("Frame copy failed: Head=%d, DLength=%d, SLength=%d\n", frameBytesHead, frameBytes.length, frameRowBytes.length);
                }
            }
        });
    }
}
