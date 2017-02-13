/*
 * Project: PronghornIoT
 * Since: Jan 21, 2017
 *
 * Copyright (c) Brandon Sanders [brandon@alicorn.io]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ociweb.pronghorn.iot.rs232;

/**
 * Base interface for supporting RS232 serial communications
 * on an arbitrary system.
 *
 * TODO: This interface is largely UNIX based, not sure if that's
 *       a problem or not.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public interface RS232NativeBacking {

    /**
     * Opens a serial port on the underlying system.
     *
     * @param port Name of the port. On UNIX systems this will typically
     *             be of the form /dev/ttyX, where X is a port number. On
     *             Windows systems this will typically of the form COMX,
     *             where X is again a port number.
     *
     * @param baud Baud rate to open the port with. This should be one of the
     *             standard baud rates found in {@link RS232Client}.
     *
     * @return A file descriptor for the opened port for use with the other
     *         methods on this interface.
     */
    int open(String port, int baud);

    /**
     * Closes a serial port on the underlying system.
     *
     * @param fd File descriptor for the port, obtained from a call
     *           to {@link #open(String, int)}
     *
     * @return Status code of the close operation.
     */
    int close(int fd);

    /**
     * Writes a message to a serial port on the underlying system.
     *
     * @param fd File descriptor for the port, obtained from a call
     *           to {@link #open(String, int)}
     *
     * @param message Message to write to the serial port.
     *
     * @return TODO: Some status code.
     */
    int write(int fd, byte[] message);

    /**
     * Returns the number of available bytes for reading on the serial port.
     *
     * @param fd File descriptor for the port, obtained from a call
     *           to {@link #open(String, int)}
     *
     * @return The number of bytes currently available for reading on the serial port.
     */
    int getAvailableBytes(int fd);

    /**
     * Reads a message from the underlying serial port. This function
     * will block until the given number of bytes (indicated by the
     * size parameter) are read.
     *
     * @param fd File descriptor for the port, obtained from a call
     *           to {@link #open(String, int)}
     *
     * @param size Size of the message to read.
     *
     * @return A byte array representing the read message. The length of
     *         the array will be exactly equal to the size parameter
     *         passed to this method.
     */
    byte[] readBlocking(int fd, int size);

    /**
     * Reads a message from the underlying serial port. This function
     * will return immediately with any available data up to the given
     * size to read; it is possible for this function to return an empty
     * array.
     *
     * @param fd File descriptor for the port, obtained from a call
     *           to {@link #open(String, int)}
     *
     * @param size Size of the message to read.
     *
     * @return A byte array representing the read message. The length
     *         of the array will be at most equal to the size
     *         parameter passed to this method, but it may be
     *         smaller if there were no available bytes to read
     *         when this function was invoked.
     */
    byte[] read(int fd, int size);

    /**
     * TODO:
     *
     * @param fd
     * @param rawBuffer
     * @param start
     * @param maxLength
     * @return
     */
    int writeFrom(int fd, byte[] rawBuffer, int start, int maxLength);

    /**
     * TODO:
     *
     * @param fd
     * @param rawBuffer1
     * @param start1
     * @param maxLength1
     * @param rawBuffer2
     * @param start2
     * @param maxLength2
     * @return
     */
    int writeFromTwo(int fd, byte[] rawBuffer1, int start1, int maxLength1,
                             byte[] rawBuffer2, int start2, int maxLength2);

    /**
     * TODO:
     *
     * @param fd
     * @param buffer
     * @param start
     * @param maxLength
     */
    int readInto(int fd, byte[] buffer, int start, int maxLength);

    /**
     * TODO:
     *
     * @param fd
     * @param buffer1
     * @param start1
     * @param maxLength1
     * @param buffer2
     * @param start2
     * @param maxLength2
     */
    int readIntoTwo(int fd, byte[] buffer1, int start1, int maxLength1,
                            byte[] buffer2, int start2, int maxLength2);
}
