package com.ociweb.iot.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ociweb.iot.hardware.impl.test.BasicTestPortReader;
import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.stage.scheduling.ScriptedNonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void testApp() {
        FogRuntime runtime = FogRuntime.test(new IoTApp());

        ScriptedNonThreadScheduler scheduler = (ScriptedNonThreadScheduler) runtime.getScheduler();

        scheduler.startup();

        TestHardware hardware = (TestHardware) runtime.getHardware();
        hardware.portReader = new BasicTestPortReader();

        int iterations = 4;
        boolean isFirst = true;

        int expected = 0;

        long lastTime = 0;
        while (iterations > 0) {

            scheduler.run();

            long time = hardware.getLastTime(IoTApp.LED_PORT);

            if (0 != time) {
                iterations--;
                assertEquals(expected, 1&hardware.read(IoTApp.LED_PORT));
                expected = 1 & (expected + 1);

                if (0 != lastTime) {
                    long durationMs = (time - lastTime);

                    if (!isFirst) {
                        assertTrue(Long.toString(durationMs),
                                   durationMs >= 400);//first difference may be short because first transition is later due to startup.
                    } else {
                        isFirst = false;
                    }
                    assertTrue(durationMs <= 750);
                }

                lastTime = time;
                hardware.clearCaputuredLastTimes();
                hardware.clearCaputuredHighs();
            }

        }
    }
}