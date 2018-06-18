package com.ociweb.iot.camera;

import com.ociweb.pronghorn.iot.jni.NativeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Native wrapper for accessing Raspberry Pi camera data.
 *
 * Special thanks to: http://jwhsmith.net/2014/12/capturing-a-webcam-stream-using-v4l2/
 * for giving a good introduction of V4L2 API usage.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public final class RaspiCam implements Camera {

    private static final Logger logger = LoggerFactory.getLogger(RaspiCam.class);

    // Load native library.
    static {
        try {
            if (new File("raspicam4j.so").exists()) {
                logger.info("raspicam4j.so detected in working directory. Loading this file instead of the packaged one.");
                System.load(new File(".").getCanonicalPath() + File.separator + "raspicam4j.so");
            } else {
                // TODO: Hard-coded for linux systems.
                // TODO: Uses external native utils stuff for convenience. May consider alternative?
                // TODO: Copied from another OCI project (FogLight) in the RS232 native class.
                String arch = System.getProperty("os.arch");
                if (arch.contains("arm")) {
                    NativeUtils.loadLibraryFromJar("/jni/arm-Linux/raspicam4j.so");
                }
            }

        } catch (UnsatisfiedLinkError | IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Default camera device that the V4L2 driver (which powers this API) uses.
     */
    public static final String DEFAULT_CAMERA_DEVICE = "/dev/video0";

    @Override
    public native int open(String device, int width, int height);

    @Override
    public native int getFrameSizeBytes(int fd);

    @Override
    public native int readFrame(int fd, ByteBuffer bytes, int start);

    @Override
    public native int close(int fd);
}
