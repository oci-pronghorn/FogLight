package com.ociweb.pronghorn.iot.rs232;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Should no-op return empty/default values or throw an exception?
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class RS232Client {

    private static final Logger logger = LoggerFactory.getLogger(RS232Client.class);

    private boolean connected = false;
    private int fd = -1;

    public RS232Client(String port, int baud) {
        try {
            fd = RS232NativeLinuxBacking.open(port, baud);

            if (fd != -1) {
                connected = true;
            } else {
                logger.error("Could not connect RS232 due to an unknown error.");
                logger.error("Switching to NO-OP mode.");
                logger.error("Did you use a valid port identifier and baud rate?");
            }
        } catch (Exception e) {
            logger.error("Could not connect RS232 client due to error: ", e);
            logger.error("Switching to NO-OP mode.");
            logger.error("Are the RS232 native libraries available and loaded?");
        }
    }

    public int write(String message) {
        if (connected) {
            return RS232NativeLinuxBacking.write(fd, message);
        } else {
            return -1;
        }
    }

    public String readBlocking(int size) {
        if (connected) {
            return RS232NativeLinuxBacking.readBlocking(fd, size);
        } else {
            return "";
        }
    }

    public String read(int size) {
        if (connected) {
            return RS232NativeLinuxBacking.read(fd, size);
        } else {
            return "";
        }
    }
}
