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
     * Returns the {@link ByteBuffer} that a camera writes its frames to.
     *
     * @param fd File descriptor of the camera to get a buffer for. Obtained from {@link #open(String, int, int)}.
     *
     * @return The byte buffer for the given camera. -1 if the passed fd is invalid.
     */
    public ByteBuffer getFrameBuffer(int fd);

    /**
     * Reads the next frame from the Raspberry Pi camera into the camera's {@link ByteBuffer}.
     *
     * The bytes are in RGB24 format:
     * - Byte [0] is the R byte
     * - Byte [1] is the G byte
     * - Byte [2] is the B byte
     * - Byte [3] is the R byte
     * - ...And so on.
     *
     * @param fd File descriptor of the camera to read from. Obtained from {@link #open(String, int, int)}.
     *
     * @return The number of bytes read. -1 if no bytes are read.
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
