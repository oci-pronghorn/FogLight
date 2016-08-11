package com.ociweb.pronghorn.iot.i2c;

/**
 * Represents a generic backing for embedded I2C communication.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public interface I2CBacking {

    /**
     * Reads a message from the I2C device at the specified address.
     *
     * @param address Address of the I2C device to read, e.g., "0x32" for a
     *                GrovePi's LCD RGB backlight.
     * @param bufferSize Size of the byte buffer to read into (and return).
     *
     * @return Data received from the I2C device, or an empty array if no
     *         data was received.
     */
    byte[] read(byte address, byte[] target, int bufferSize);

    /**
     * Writes a message to an I2C device at the specified address.
     *
     * @param address Address of the I2C device to write, e.g., "0x32" for a
     *                GrovePi's LCD RGB backlight.
     * @param message Array of bytes to write to the I2C device.
     */
    boolean write(byte address, byte[] message, int length);
}
