package com.ociweb.device.testApps;

import com.ociweb.iot.grove.device.Grove_LCD_RGB;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * A simple app that demonstrates interacting with I2C via our JFFI wrapper.
 *
 * Take a look at {@link I2CNativeLinuxBacking} for more in-depth information on
 * how JFFI is bound to our app.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class I2CExampleApp {
    private static final Logger logger = LoggerFactory.getLogger(I2CExampleApp.class);

    // Create a connection to the native Linux I2C lines.
    private static final I2CNativeLinuxBacking i2c = new I2CNativeLinuxBacking();

    // Helper to make writing to the LCD less tedious.
    private static void writeCommandsToGroveLCD(byte... commands) {
        //TODO: For some reason, this gives something like 0xffffff6 if we don't assign to a temporary int first.
        int temp = 0xFF & (byte) ((Grove_LCD_RGB.RGB_ADDRESS << 1));
        byte address = (byte) (temp >> 1);

        i2c.write(address, commands);
    }

    public static void main(String[] args) {
        Random random = new Random();

        logger.info("Starting I2C example application.");

        // Loop forever coloring the display.
        while (true) {
            // Reset the display.
            writeCommandsToGroveLCD((byte) 0, (byte) 0);
            writeCommandsToGroveLCD((byte) 1, (byte) 0);
            writeCommandsToGroveLCD((byte) 0x08, (byte) 0xaa);

            // Pick some random colors.
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);

            logger.info("Setting the Grove display color to R{} G{} B{}", r, g, b);

            // Set them on the display.
            writeCommandsToGroveLCD((byte) 4, (byte) r);
            writeCommandsToGroveLCD((byte) 3, (byte) g);
            writeCommandsToGroveLCD((byte) 2, (byte) b);

            // Sleep for a bit.
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
