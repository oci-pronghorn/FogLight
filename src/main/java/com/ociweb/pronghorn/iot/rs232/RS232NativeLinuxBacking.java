package com.ociweb.pronghorn.iot.rs232;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * JNI wrapper for RS232 serial operations on a UNIX system.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public final class RS232NativeLinuxBacking implements RS232NativeBacking {

    private static final Logger logger = LoggerFactory.getLogger(RS232NativeLinuxBacking.class);

    static {
        try {
            // TODO: Hard-coded for linux systems.
            // TODO: Uses external native utils stuff for convenience. May consider alternative?
            String arch = System.getProperty("os.arch");
            if (arch.contains("arm")) {
                NativeUtils.loadLibraryFromJar("/jni/arm-Linux/rs232.so");
            } else {
                try {
                    NativeUtils.loadLibraryFromJar("/jni/i386-Linux/rs232.so");
                } catch (Exception e) {
                    logger.warn("Fallback i386 linux libraries failed to load. Attempting to load rs232.so from working directory.");
                    System.load(new File(".").getCanonicalPath() + File.separator + "rs232.so");
                }
            }

        } catch (UnsatisfiedLinkError | IOException e) {
            e.printStackTrace();
        }
    }

    // Native methods.
    public native int open(String port, int baud);
    public native int write(int fd, byte[] message);
    public native int getAvailableBytes(int fd);
    public native byte[] readBlocking(int fd, int size);
    public native byte[] read(int fd, int size);
}
