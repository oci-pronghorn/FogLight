package com.ociweb.device.testApp;

import java.io.File;
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

            FileOutputStream workingFile = null;

            @Override
            public void onFrameStart(int width, int height, long timestamp) {

                // Close existing file if present.
                if (workingFile != null) {
                    try {
                        System.out.println("Captured full image to disk.");
                        workingFile.flush();
                        workingFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

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
                    workingFile = new FileOutputStream(newWorkingFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFrameRow(byte[] frameRowBytes) {
                try {
                    workingFile.write(frameRowBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
