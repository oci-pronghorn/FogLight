package com.ociweb.pronghorn.iot.i2c;

/**
 * Represents a generic backing for embedded I2C communication.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public interface I2CBacking {

    /**
     * Configures the bus to use on this I2C device. This method
     * <b>must</b> be invoked before any invocations of
     * {@link #read(byte, byte[], int)} or {@link #write(byte, byte[], int)},
     * and it may only be invoked <b>once</b>.
     *
     * @param bus I2C bus number to use.
     *
     * @throws IllegalStateException if this method is invoked more than once.
     *
     * @return This I2CBacking.
     */
    I2CBacking configure(byte bus);

    /**
     * Reads a message from the I2C device at the specified address.
     *
     * @param address Address of the I2C device to read, e.g., "0x32" for a
     *                GrovePi's LCD RGB backlight.
     * @param bufferSize Size of the byte buffer to read into (and return).
     *
     * @return Data received from the I2C device, or an empty array if no
     *         data was received.
     *
     * @throws IllegalStateException if this method is invoked before {@link #configure(byte)}.
     */
    byte[] read(byte address, byte[] target, int bufferSize);

    /**
     * Writes a message to an I2C device at the specified address.
     *
     * @param address Address of the I2C device to write, e.g., "0x32" for a
     *                GrovePi's LCD RGB backlight.
     * @param message Array of bytes to write to the I2C device.
     *
     * @return True if writing the message was successful, and false otherwise.
     *
     * @throws IllegalStateException if this method is invoked before {@link #configure(byte)}.
     */
    boolean write(byte address, byte[] message, int length);
}
