package com.ociweb.device.grove.grovepi;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;

/**
 * Library wrapper for a Linux native C library.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public interface CLib extends Library {
    //C Library instance.
    CLib instance = (CLib) Native.loadLibrary("c", CLib.class);

    //I2C constants.
    int I2C_SLAVE = 0x0703;
    int I2C_SLAVE_FORCE = 0x0706;

    //Filesystem constants.
    int O_RDWR = 00000002;

    //Native Methods///////////////////////////////////////////////////////////
    int ioctl(int fd, int cmd, NativeLong address);
    int open(String path, int flags);
    int close(int fd);
    int read(int fd, byte[] buffer, int count);
    int write(int fd, byte[] buffer, int count);
}
