package com.ociweb.pronghorn.iot.rs232;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * Represents a client for an RS232 serial port.
 *
 * TODO: Should no-op return empty/default values or throw an exception?
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class RS232Client {

    private static final Logger logger = LoggerFactory.getLogger(RS232Client.class);

    private RS232NativeBacking backing;
    private boolean connected = false;
    private int fd = -1;

    // Standard baud rates.
    public static final int B9600 =  0000015;
    public static final int B19200 = 0000016;
    public static final int B38400 = 0000017;
    public static final int B57600 = 0010001;
    public static final int B115200 = 0010002;
    public static final int B230400 = 0010003;
    public static final int B460800 = 0010004;
    public static final int B500000 = 0010005;
    public static final int B576000 = 0010006;
    public static final int B921600 = 0010007;
    public static final int B1000000 = 0010010;
    public static final int B1152000 = 0010011;
    public static final int B1500000 = 0010012;
    public static final int B2000000 = 0010013;
    public static final int B2500000 = 0010014;
    public static final int B3000000 = 0010015;
    public static final int B3500000 = 0010016;
    public static final int B4000000 = 0010017;

    /**
     * @param port Port identifier to open.
     * @param baud Baud rate to use.
     */
    public RS232Client(String port, int baud) {
        try {
            backing = new RS232NativeLinuxBacking();
            fd = backing.open(port, baud);

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

    /**
     *
     * @return
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Closes this serial port.
     *
     * @return Status code of the close operation.
     */
    public int close() {
        if (connected) {
            int status = backing.close(fd);
            if (status == 0) {
                connected = false;
                return 0;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Writes a message to this serial port.
     *
     * TODO: This could be optimized to use chars instead of strings.
     *
     * @param message Message to write to the serial port.
     *
     * @return TODO: Some status code.
     */
    public int write(byte[] message) {
        if (connected) {
            return backing.write(fd, message);
        } else {
            return -1;
        }
    }

    /**
     * Returns the number of available bytes for reading on this serial port.
     *
     * @return The number of bytes currently available for reading on this serial port.
     */
    public int getAvailableBytes() {
        if (connected) {
            return backing.getAvailableBytes(fd);
        } else {
            return 0;
        }
    }

    /**
     * Reads a message from this serial port. This function
     * will block until the given number of bytes (indicated by the
     * size parameter) are read.
     *
     * @param size Size of the message to read.
     *
     * @return A byte array representing the read message. The length of
     *         the array will be exactly equal to the size parameter
     *         passed to this method.
     */
    public byte[] readBlocking(int size) {
        if (connected) {
            return backing.readBlocking(fd, size);
        } else {
            return new byte[0];
        }
    }

    /**
     * Reads a message from this serial port. This function
     * will return immediately with any available data up to the given
     * size to read; it is possible for this function to return an empty
     * string.
     *
     * @param size Size of the message to read.
     *
     * @return A byte array representing the read message. The length
     *         of the array will be at most equal to the size
     *         parameter passed to this method, but it may be
     *         smaller if there were no available bytes to read
     *         when this function was invoked.
     */
    public byte[] read(int size) {
        if (connected) {
            return backing.read(fd, size);
        } else {
            return new byte[0];
        }
    }

    /**
     * TODO:
     *
     * @param buffer
     * @param start
     * @param maxLength
     *
     */
    public int writeFrom(byte[] buffer, int start, int maxLength) {
        if (connected) {
            return backing.writeFrom(fd, buffer, start, maxLength);
        } else {
            return -1;
        }
    }

    /**
     * TODO:
     *
     * @param buffer1
     * @param start1
     * @param maxLength1
     * @param buffer2
     * @param start2
     * @param maxLength2
     *
     */
    public int writeFrom(byte[] buffer1, int start1, int maxLength1,
                         byte[] buffer2, int start2, int maxLength2) {
        if (connected) {
            return backing.writeFromTwo(fd, buffer1, start1, maxLength1,
                                            buffer2, start2, maxLength2);
        } else {
            return -1;
        }
    }

    /**
     * TODO:
     *
     * @param buffer
     * @param start
     * @param maxLength
     *
     */
    public int readInto(byte[] buffer, int start, int maxLength) {
        if (connected) {
            return backing.readInto(fd, buffer, start, maxLength);
        } else {
            return -1;
        }
    }

    /**
     * TODO:
     *
     * @param buffer1
     * @param start1
     * @param maxLength1
     * @param buffer2
     * @param start2
     * @param maxLength2
     *
     */
    public int readInto(byte[] buffer1, int start1, int maxLength1,
                        byte[] buffer2, int start2, int maxLength2) {
        if (connected) {
            return backing.readIntoTwo(fd, buffer1, start1, maxLength1,
                                           buffer2, start2, maxLength2);
        } else {
            return  -1;
        }
    }
}
