// Project: PronghornIoT
// Since: Feb 21, 2016
//
///////////////////////////////////////////////////////////////////////////////
/**
 * TODO: Which license?
 */
///////////////////////////////////////////////////////////////////////////////
//
package com.ociweb.device.grove.grovepi;

/**
 * Represents a generic backing for the GrovePi I2C lines.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public interface GrovePiI2CStageBacking {

    //Member Function: read////////////////////////////////////////////////////
    /**
     * Reads a message from the I2C device at the specified address.
     *
     * @param address Address of the I2C device to read, e.g., "0x32" for a
     *                GrovePi's LCD RGB backlight.
     * @param message Array of bytes to write to the I2C device.
     *
     * @return Data received from the I2C device, or an empty array if no
     *         data was received.
     */
    public byte[] read(byte address, byte... message);

    //Member Function: write///////////////////////////////////////////////////
    /**
     * Writes a message to an I2C device at the specified address.
     *
     * @param address Address of the I2C device to write, e.g., "0x32" for a
     *                GrovePi's LCD RGB backlight.
     * @param message Array of bytes to write to the I2C device.
     */
    public void write(byte address, byte... message);

    //Member Function: update//////////////////////////////////////////////////
    /**
     * Updates the state of this I2C stage backing; this function is only
     * needed by backings that need to execute their reads and writes in
     * stages instead of all at once.
     */
    public void update();
}
