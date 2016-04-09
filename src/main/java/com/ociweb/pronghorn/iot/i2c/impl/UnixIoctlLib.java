package com.ociweb.pronghorn.iot.i2c.impl;

import jnr.ffi.LibraryLoader;

/**
 * JNI library wrapper for IOCTL operations on a UNIX system
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public interface UnixIoctlLib {
    //C Library instance.
    UnixIoctlLib instance = LibraryLoader.create(UnixIoctlLib.class).load("c");

    //I2C constants.
    int I2C_SLAVE = 0x0703;
    int I2C_SLAVE_FORCE = 0x0706;

    //Filesystem constants.
    int O_RDWR = 00000002;

    //Native Methods///////////////////////////////////////////////////////////
//    int ioctl(int fd, int cmd, NativeLong address);
    int ioctl(int fd, int cmd, long address);
    int open(String path, int flags);
    int close(int fd);
    int read(int fd, byte[] buffer, int count);
    int write(int fd, byte[] buffer, int count);
}
