package com.ociweb.pronghorn.iot.rs232;

import java.io.File;
import java.io.IOException;

/**
 * JNI wrapper for RS232 serial operations on a UNIX system.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public final class RS232Native {

    static {
        try {
            System.load(new File(".").getCanonicalPath() + File.separator + "rs232.so");
        } catch (UnsatisfiedLinkError | IOException e) {
            e.printStackTrace();
        }
    }

    private RS232Native() {}

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

    // Native methods.
    public static native int open(String port, int baud);
    public static native int write(int fd, String message);
    public static native String readBlocking(int fd, int size);
    public static native String read(int fd, int size);
}
