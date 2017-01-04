package com.ociweb.device.testApp;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Pi image capture test.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class PiCamTest implements IoTSetup {

    private final List<File> images = new ArrayList<>();

    public static void main( String[] args) {
        DeviceRuntime.run(new PiCamTest());
    }

    @Override
    public void declareConnections(Hardware hardware) {
        hardware.setTriggerRate(1250);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        runtime.addImageListener((image) -> {
            System.out.println("Grove Pi Captured Image: ");
            System.out.println(image);

            images.add(image);

            if (images.size() > 50) {
                File f = images.get(0);
                images.remove(0);
                f.delete();
            }
        });
    }
}
