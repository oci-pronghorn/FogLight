package com.ociweb.iot.impl;

/**
 * Listener for responding to image-receive events on a hardware system
 * equipped with a camera or other image-generating systems.
 *
 * Image frames received by this method will always be in RGB24 format. This format
 * packs all of the pixels in an image together into one long array of bytes, where
 * each 0th byte is an R value, each 1th byte is a G value, and each 2nd byte is a B value.
 *
 * @author Brandon Sanders [brandon@alicorn.io] -- Initial API and implementation
 * @author Ray Lo -- Moved and modified
 */
public interface ImageListenerBase {

    /**
     * Invoked when a new image frame is started.
     *
     * @param width Width in pixels of the image frame.
     * @param height Height in pixels of the image frame.
     * @param timestamp Timestamp of the image frame.
     */
    void onFrameStart(int width, int height, long timestamp);

    /**
     * Invoked when a new image frame row is received.
     *
     * @param frameRowBytes A byte array containing a row of image data.
     */
    void onFrameRow(byte[] frameRowBytes);
}
