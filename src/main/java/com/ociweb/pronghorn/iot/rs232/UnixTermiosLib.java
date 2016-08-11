package com.ociweb.pronghorn.iot.rs232;

import jnr.ffi.LibraryLoader;

/**
 * JNI wrapper for Termios operations on a UNIX system.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public interface UnixTermiosLib {
    //C Library instance.
    UnixTermiosLib instance = LibraryLoader.create(UnixTermiosLib.class).load("c");

    //Filesystem constants.
    int F_SETFL = 4;
    int O_RDWR = 00000002;

    //Native Methods///////////////////////////////////////////////////////////
    int open(String path, int flags);
    int close(int fd);
    int fcntl(int fd, int opt1, int opt2);
    int read(int fd, byte[] buffer, int count);
    int write(int fd, byte[] buffer, int count);
}
