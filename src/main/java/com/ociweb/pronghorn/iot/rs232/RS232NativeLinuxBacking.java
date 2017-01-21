package com.ociweb.pronghorn.iot.rs232;

import java.io.File;
import java.io.IOException;

/**
 * JNI wrapper for RS232 serial operations on a UNIX system.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public final class RS232NativeLinuxBacking implements RS232NativeBacking {

    static {
        try {
            // TODO: Hard-coded for linux systems.
            // TODO: Uses external native utils stuff for convenience. May consider alternative?
            String arch = System.getenv("os.arch");
            if (arch.contains("arm")) {
                NativeUtils.loadLibraryFromJar("/jni/arm-linux/rs232.so");
            } else {
                NativeUtils.loadLibraryFromJar("/jni/i386-linux/rs232.so");
            }
//            System.load(new File(".").getCanonicalPath() + File.separator + "rs232.so");
        } catch (UnsatisfiedLinkError | IOException e) {
            e.printStackTrace();
        }
    }

    // Native methods.
    public native int open(String port, int baud);
    public native int write(int fd, String message);
    public native String readBlocking(int fd, int size);
    public native String read(int fd, int size);
}
