package com.ociweb.pronghorn.iot.rs232;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.Baud;

/**
 * Represents a client for an RS232 serial port.
 *
 * TODO: Should no-op return empty/default values or throw an exception?
 *
 * When building the native code on the pi:
 *   declare -x JAVA_HOME="/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt"
 *   gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/linux -shared -o rs232.so -fPIC src/main/c/RS232.c
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class RS232Client implements RS232Clientable {

    private static final Logger logger = LoggerFactory.getLogger(RS232Client.class);

    private RS232NativeBacking backing;
    private boolean connected = false;
    private int fd = -1;

    private int failCount = 25;
    /**
     * @param device Port identifier to open.
     * @param baud Baud rate to use.
     */
    public RS232Client(String device, Baud baud) {
        try {
        	logger.info("connecting to serial {} {}",device,baud);
        	
            backing = new RS232NativeLinuxBacking();
            fd = backing.open(device, baud.code());

            if (fd == -1) {
                connected = true;
                logger.info("Backing FD is: {}", fd);
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
     * @return True if this client is currently connected to
     *         an RS232 interface, and false otherwise.
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
        	logger.trace("call write {}",message.length);
            int result = backing.write(fd, message);
            logger.trace("did write {}",result);
            if ( 0 != message.length && 0 !=result && --failCount<=0) {
            	logger.warn("Unable to write to serial port");  	
            }
            return result;
        } else {
            return -1;
        }
    }

    /**
     * Returns the number of unsent bytes in the serial output buffer.
     *
     * @return The number of unsent bytes in the serial output buffer.
     */
    public int getBytesInOutputBuffer() {
        if (connected) {
            return backing.getBytesInOutputBuffer(fd);
        } else {
            return 0;
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
     * Writes the bytes from an array directly to this client's serial port.
     *
     * @param buffer Buffer to write bytes from.
     * @param start Position in the buffer to start writing from.
     * @param maxLength Maximum number of values to write from the buffer.
     *
     * @return TODO: Some status code.
     */
    public int writeFrom(byte[] buffer, int start, int maxLength) {
        if (connected) {
        	logger.trace("call write {}",maxLength);
            int result = backing.writeFrom(fd, buffer, start, maxLength);
            logger.trace("did write {}",result);
            if (0 != maxLength && 0==result && --failCount<=0) {
            	logger.warn("Unable to write to serial port");  	
            }
            else {
            	failCount = 25;
            }
            return result;
        } else {
        	logger.trace("not connected");
            return -1;
        }
    }

    /**
     * Writes the bytes from two arrays directly to this client's serial port.
     *
     * @param buffer1 First buffer to write bytes from.
     * @param start1 Position in the first buffer to start writing from.
     * @param maxLength1 Maximum number of values to write from the first buffer.
     * @param buffer2 Second buffer to write bytes from.
     * @param start2 Position in the second buffer to start writing from.
     * @param maxLength2 Maximum number of values to write from the second buffer.
     *
     * @return TODO: Some status code.
     */
    public int writeFrom(byte[] buffer1, int start1, int maxLength1,
                         byte[] buffer2, int start2, int maxLength2) {
        if (connected) {
        	logger.trace("called write {} {} ",maxLength1,maxLength2);
            int result = backing.writeFromTwo(fd, buffer1, start1, maxLength1,
                                            buffer2, start2, maxLength2);
            logger.trace("write results {} ", result);
           
            return result;
        } else {
        	logger.trace("not connected");
            return -1;
        }
    }

    /**
     * Reads data from this client's serial port directly into an array.
     *
     * This method performs a <b>non-blocking</b> read, meaning the array
     * may not be entirely filled with data.
     *
     * @param buffer Array to read bytes into.
     * @param start Starting position in the array to read bytes into.
     * @param maxLength Maximum number of bytes to read.
     *
     * @return The total number of bytes read.
     */
    public int readInto(byte[] buffer, int start, int maxLength) {
        if (connected) {
            return backing.readInto(fd, buffer, start, maxLength);
        } else {
            return -1;
        }
    }

    /**
     * Reads data from this client's serial port directly into two arrays.
     *
     * This method performs a <b>non-blocking</b> read, meaning the array
     * may not be entirely filled with data. Additionally, it's possible
     * for only one of the arrays to actually be written to depending on
     * how much data is available when this method is invoked.
     *
     * @param buffer1 First array to read bytes into.
     * @param start1 Starting position in the first array to read bytes
     *               into.
     * @param maxLength1 Maximum number of bytes to read into the first
     *                   array.
     * @param buffer2 Second array to read bytes into.
     * @param start2 Starting position in the second array to read bytes
     *               into.
     * @param maxLength2 Maximum number of bytes to read into the second
     *                   array.
     *
     * @return The total number of bytes read.
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
