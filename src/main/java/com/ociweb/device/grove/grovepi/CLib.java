// Project: PronghornIoT
// Since: Feb 18, 2016
//
///////////////////////////////////////////////////////////////////////////////
/**
 * TODO: What license?
 */
///////////////////////////////////////////////////////////////////////////////
//
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
    long memcpy(int[] dst, short[] src, long n);

    int memcpy(int[] dst, short[] src, int n);

    int pipe(int[] fds);

    int tcdrain(int fd);

    int fcntl(int fd, int cmd, int arg);

    int ioctl(int fd, int cmd, NativeLong address);

    int open(String path, int flags);

    int close(int fd);

    int write(int fd, byte[] buffer, int count);

    int read(int fd, byte[] buffer, int count);

    long write(int fd, byte[] buffer, long count);

    long read(int fd, byte[] buffer, long count);

    int poll(int[] fds, int nfds, int timeout);

    int tcflush(int fd, int qs);

    void perror(String msg);

    int tcsendbreak(int fd, int duration);
}
