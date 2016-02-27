// Project: PronghornIoT
// Since: Feb 21, 2016
//
///////////////////////////////////////////////////////////////////////////////
/**
 * TODO: What license?
 */
///////////////////////////////////////////////////////////////////////////////
//
package com.ociweb.device.grove.grovepi;

import com.sun.jna.NativeLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native I2C backing implementation for a GrovePi using JNA and ioctl.h.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class GrovePiI2CStageNativeBacking implements GrovePiI2CStageBacking {
//Private//////////////////////////////////////////////////////////////////////

    private static final Logger logger = LoggerFactory.getLogger(GrovePiI2CStageNativeBacking.class);

    //Native C library.
    private static final CLib c = CLib.instance;

    //Active I2C connections and files.
    private int i2cFile = -1;

    //TODO: Does the maximum number of I2C addresses cap out at 0x77? Is this partially wasting memory?
    private int[] ioctls = new int[0x77];

    //Member Function: ensureI2CDevice/////////////////////////////////////////
    /**
     * Registers an ioctl handle for the device at the specified address if
     * it hasn't been registered already.
     *
     * @param address Address of the I2C device to register.
     */
    private boolean ensureI2CDevice(byte address) {
        if (address > 0 && ioctls.length >= address && ioctls[address] == 0) {
            ioctls[address] = c.ioctl(i2cFile, CLib.I2C_SLAVE_FORCE, new NativeLong(address));

            if (ioctls[address] < 0) {
                logger.error("Could not connect I2C device at 0x" + Integer.toHexString(address));
//                throw new RuntimeException("Could not connect I2C device at 0x" + Integer.toHexString(address));
            } else {
                logger.info("Connected I2C device at 0x" + Integer.toHexString(address));
                return true;
            }
        } else if (address < 0 || ioctls.length <= address) {
            logger.error("I2C Device 0x" + Integer.toHexString(address) + " is out of array bounds.");
        }

        return false;
    }

//Public///////////////////////////////////////////////////////////////////////

    //Constructor//////////////////////////////////////////////////////////////
    public GrovePiI2CStageNativeBacking() {
        //Get the I2C file.
        i2cFile = c.open("/dev/i2c-1", CLib.O_RDWR);

        //Make sure it worked....
        if (i2cFile < 0) {
            logger.error("Could not open /dev/i2c-1");
            throw new RuntimeException("Could not open /dev/i2c-1");
        } else {
            logger.info("Successfully opened /dev/i2c-1");
        }
    }

    @Override public void update() { }

    @Override public byte[] read(byte address, byte... message) {
        //Check if we need to load the address into memory.
        if (ensureI2CDevice(address)){
            return new byte[] {(byte) c.read(i2cFile, message, message.length)};
        } else {
            return new byte[]{};
        }
    }

    @Override public void write(byte address, byte... message) {
        //Check if we need to load the address into memory.
        if (ensureI2CDevice(address)) {

            //TODO: Debugging output.
//            for (byte b : message) {
//                logger.info("Writing 0x" + Integer.toHexString(b));
//            }

            c.write(i2cFile, message, message.length);
        }
    }
}