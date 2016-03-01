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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.device.config.GroveConnectionConfiguration;
import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;

/**
 * Java I2C backing implementation for a GrovePi using the amazing technique of
 * bit-banging GPIO ports.
 *
 * TODO: Completely untested.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class GrovePiI2CStageJavaBacking implements GrovePiI2CStageBacking {
//Private//////////////////////////////////////////////////////////////////////

    private static final Logger logger = LoggerFactory.getLogger(GrovePiI2CStageJavaBacking.class);
    private static final int MAX_CONFIGURABLE_BYTES = 16;

    //Nanoseconds to pause between I2C commands.
    private static final int NS_PAUSE = 10 * 1000;

    //Task states.
    private static final int TASK_NONE = 0;
    private static final int TASK_MASTER_START = 1;
    private static final int TASK_MASTER_STOP  = 2;
    private static final int TASK_WRITE_BYTES  = 3;

    //GrovePi GPIO connection.
    private final Pipe<I2CCommandSchema> request;
    private final GroveConnectionConfiguration config;

    //Current task state.
    private int taskPhase = TASK_NONE;

    //Current byte buffer.
    private int cyclesToWait;
    private int[] cyclesToWaitLookup = new int[MAX_CONFIGURABLE_BYTES];
    private int byteToSend;

    //Holds the same array as used by the Blob from the ring.
    private byte[] bytesToSendBacking;
    private int    bytesToSendRemaining;
    private int    bytesToSendPosition;
    private int    bytesToSendMask;
    private int    bytesToSendReleaseSize;

    //Member Function: pause///////////////////////////////////////////////////
    /**
     * Puts the thread to sleep just long enough for I2C based off of
     * {@link #NS_PAUSE}.
     */
    public void pause() {
        try { Thread.sleep(NS_PAUSE / 1000000, NS_PAUSE % 1000000); }
        catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    //Member Function: writeBit////////////////////////////////////////////////
    /**
     * Writes a bit to this backing's I2C line.
     *
     * @param bit Bit to write.
     */
    private void writeBit(boolean bit) {
        if (bit) config.i2cSetDataHigh();
        else config.i2cSetDataLow();
        config.i2cClockOut();

        config.i2cSetClockHigh();
        config.i2cClockIn();

        while (config.i2cReadClock() == 0) {
            logger.warn("Clock stretching in writeBit...");
        }

        config.i2cClockOut();
        config.i2cSetClockLow();
    }

    //Member Function: writeByte///////////////////////////////////////////////
    /**
     * Writes a byte to this backing's I2C line.
     *
     * @param b Byte to write.
     */
    private void writeByte(int b) {
        for (int bit = 0; bit < 8; bit++) {
            writeBit((b & 0x80) != 0);
            b <<= 1;
        }
    }

    //Member Function: readRequest/////////////////////////////////////////////
    /**
     * TODO:
     */
    private void readRequest() {
        if (Pipe.hasContentToRead(request)) {
            int msgId = Pipe.takeMsgIdx(request);

            if (msgId<0) {
                //TODO: Shutdown?
//                requestShutdown();
                return;
            }

            bytesToSendReleaseSize =  Pipe.sizeOf(request, msgId);

            switch(msgId) {
                case I2CCommandSchema.MSG_COMMAND_1:
                    int meta = Pipe.takeRingByteMetaData(request);
                    int len = Pipe.takeRingByteLen(request);

                    bytesToSendBacking = Pipe.byteBackingArray(meta, request);
                    bytesToSendMask = Pipe.blobMask(request);
                    bytesToSendPosition = Pipe.bytePosition(meta, request, len);
                    bytesToSendRemaining = len;

                    taskPhase = TASK_MASTER_START;

                    cyclesToWait = bytesToSendPosition<MAX_CONFIGURABLE_BYTES ? cyclesToWaitLookup[bytesToSendPosition] : 0;
                    byteToSend = 0xFF&bytesToSendBacking[bytesToSendMask&bytesToSendPosition++];
                    break;
                case I2CCommandSchema.MSG_SETDELAY_10:
                    int offset = Pipe.takeValue(request);
                    cyclesToWaitLookup[offset] = 1 + (Pipe.takeValue(request)/NS_PAUSE);
                    Pipe.confirmLowLevelRead(request,bytesToSendReleaseSize);
                    Pipe.releaseReads(request);
                    break;
            }
        }
    }

    //Member Function: masterStart/////////////////////////////////////////////
    /**
     * Writes the master start bit to the I2C line.
     */
    private void masterStart() {
        config.i2cSetDataLow();

        config.i2cClockOut();
        config.i2cSetClockLow();

        taskPhase = TASK_WRITE_BYTES;
    }

    //Member Function: masterWrite/////////////////////////////////////////////
    /**
     * Writes the master's message to the I2C line.
     */
    private void masterWrite() {
        writeByte(byteToSend);
        System.out.println("Sent 0x" + Integer.toHexString(byteToSend));
        if (--bytesToSendRemaining <= 0) {
            taskPhase = TASK_MASTER_STOP; //we are all done

            //release the resources from the pipe for more data
            Pipe.confirmLowLevelRead(request, bytesToSendReleaseSize);
            Pipe.releaseReads(request);
        }

        else {
            byteToSend = 0xFF&bytesToSendBacking[bytesToSendMask&bytesToSendPosition++];
        }
    }

    //Member Function: masterStop//////////////////////////////////////////////
    /**
     * Writes the master stop bit to the I2C line.
     */
    private void masterStop() {
        config.i2cSetDataLow();

        config.i2cClockIn();
        while (config.i2cReadClock() == 0) {
            System.out.println("Clock stretching in masterStop...");
        }

        config.i2cSetDataHigh();

        taskPhase = TASK_NONE;
    }

//Public///////////////////////////////////////////////////////////////////////

    //Constructor//////////////////////////////////////////////////////////////
    //TODO: This constructor shouldn't need to be passed a request;
    //      that data should be passed via the read/write methods.
    public GrovePiI2CStageJavaBacking(Pipe<I2CCommandSchema> request, GroveConnectionConfiguration config) {
        this.request = request;
        this.config = config;

        //Setup the I2C lines.
        config.beginPinConfiguration();
        config.configurePinsForI2C();
        config.i2cClockOut();
        config.endPinConfiguration();
        config.i2cSetClockHigh();
        pause();
        config.i2cSetDataHigh();
        pause();
    }

    //TODO: Unimplemented; need to figure out how to extract the messages from
    //      the pipe and feed them to these methods.
    @Override public byte[] read(byte address, byte... message) {
        return new byte[0];
    }

    //TODO: Unimplemeted; need to figure out how to extract the messages from
    //      the pipe and feed them to these methods.
    @Override public void write(byte address, byte... message) {

    }

    @Override public void update() {
        if (taskPhase == TASK_NONE) {
            readRequest();
        }

        switch (taskPhase) {
            case TASK_MASTER_START:
                masterStart();
                break;
            case TASK_WRITE_BYTES:
                masterWrite();
                break;
            case TASK_MASTER_STOP:
                masterStop();
                break;
        }
    }
}
