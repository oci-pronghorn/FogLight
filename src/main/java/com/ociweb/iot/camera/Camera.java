package com.ociweb.iot.camera;

import java.nio.ByteBuffer;

/**
 * TODO:
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public interface Camera {

    /**
     * Opens a camera file descriptor.
     *
     * @param device Absolute path of the device file to open.
     * @param width Width in pixels to begin streaming frames.
     * @param height Height in pixels to begin streaming frames.
     *
     * @return A file descriptor for the given device, or -1 if something went wrong.
     */
    public int open(String device, int width, int height);

    /**
     * Returns the size of a complete image frame in bytes for the camera
     * denoted by the given file descriptor.
     *
     * @param fd File descriptor of the camera to read from. Obtained from {@link #open(String, int, int)}.
     *
     * @return The size in bytes of a complete image frame. -1 if the passed fd is invalid.
     */
    public ByteBuffer getFrameBuffer(int fd);

    /**
     * Reads the next frame from the Raspberry Pi camera into a byte array.
     *
     * The bytes are in RGB24 format:
     * - Byte [0] is the R byte
     * - Byte [1] is the G byte
     * - Byte [2] is the B byte
     * - Byte [3] is the R byte
     * - ...And so on.
     *
     * @param fd File descriptor of the camera to read from. Obtained from {@link #open(String, int, int)}.
     * @param bytes Byte buffer to read the frame into.
     * @param start Position to start reading bytes into in the byte array. The difference between the
     *              byte array's length and the start position must be greater than or equal
     *              to the returned value of {@link #getFrameSizeBytes(int)} for this file descriptor.
     *
     * @return The number of bytes read.
     */
    public int readFrame(int fd);

    /**
     * Closes a camera file descriptor.
     *
     * @param fd File descriptor of the camera to close. Obtained from {@link #open(String, int, int)}.
     *
     * @return Some number if closing succeeded, -1 if failed.
     */
    public int close(int fd);
}
