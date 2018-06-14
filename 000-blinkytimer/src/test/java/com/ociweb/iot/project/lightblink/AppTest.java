package com.ociweb.iot.project.lightblink;

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

        int testSize = 8;
        int iterations = testSize;
        int expected = 1;

        long sum = 0;

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
                    sum += durationMs;
                    assertTrue(Long.toString(durationMs), durationMs >= 470);
                    assertTrue(Long.toString(durationMs), durationMs <= 530);
                }

                lastTime = time;
                hardware.clearCaputuredLastTimes();
                hardware.clearCaputuredHighs();
            }

        }

        long avg = sum / (testSize - 1);
        assertTrue(Long.toString(avg), avg >= 490);
        assertTrue(Long.toString(avg), avg <= 1000);

    }
}
