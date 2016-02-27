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
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

/**
 * Sample I2C stage for use with a Grove Pi.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class GrovePiI2CStageV2 extends PronghornStage {
    private static final Logger logger = LoggerFactory.getLogger(GrovePiI2CStageV2.class);
    private static final int NS_PAUSE = 10*1000;
    private static final int MAX_CONFIGURABLE_BYTES = 16;

    private final Pipe<I2CCommandSchema> request;
    private final GroveConnectionConfiguration config;
    private GrovePiI2CStageBacking backing;

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
    
    public GrovePiI2CStageV2(GraphManager gm, Pipe<I2CCommandSchema> request, GroveConnectionConfiguration config) {
        super(gm, request, NONE);
        
        this.request = request;
        this.config = config;
        
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, NS_PAUSE, this);
        GraphManager.addNota(gm, GraphManager.PRODUCER, GraphManager.PRODUCER, this);
    }
    
    @Override
    public void startup() {
        //Set this thread to low priority to give other threads more resources.
        if (Thread.currentThread().getPriority() != Thread.MAX_PRIORITY) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        }

        //Figure out which backing to use.
        try {
            backing = new GrovePiI2CStageNativeBacking();
        } catch (Exception e) {
            logger.warn("Couldn't start up native I2C backing; " +
                        "falling back to bit-banged Java implementation.");
            backing = new GrovePiI2CStageJavaBacking(request, config);
        }
    }
    
    @Override
    public void run() {
        //TODO: Shouldn't need to actually check what kind of backing is in use.
        //TODO: This logic could probably be cleaned up due to the way it's being performed now, but I don't entirely understand it so...
        if (backing instanceof GrovePiI2CStageNativeBacking) {
            if (Pipe.hasContentToRead(request)) {
                //Verify message ID.
                int msgId = Pipe.takeMsgIdx(request);
                if (msgId < 0 ) {
                    requestShutdown();
                    return;
                }

                //Process ID.
                bytesToSendReleaseSize = Pipe.sizeOf(request, msgId);
                switch (msgId) {
                    case I2CCommandSchema.MSG_COMMAND_1:
                        int meta = Pipe.takeRingByteMetaData(request);
                        int len = Pipe.takeRingByteLen(request);

                        bytesToSendBacking = Pipe.byteBackingArray(meta, request);
                        bytesToSendMask = Pipe.blobMask(request);
                        bytesToSendPosition = Pipe.bytePosition(meta, request, len);
                        bytesToSendRemaining = len;
                        cyclesToWait = bytesToSendPosition < MAX_CONFIGURABLE_BYTES ? cyclesToWaitLookup[bytesToSendPosition] : 0;
                        byteToSend = 0xFF & bytesToSendBacking[bytesToSendMask & bytesToSendPosition++];

                        break;

                    case I2CCommandSchema.MSG_SETDELAY_10:
                        int offset = Pipe.takeValue(request);

                        cyclesToWaitLookup[offset] = 1 + (Pipe.takeValue(request) / NS_PAUSE);
                        Pipe.confirmLowLevelRead(request, bytesToSendReleaseSize);
                        Pipe.releaseReads(request);

                        break;
                }

                if (bytesToSendRemaining > 0) {
                    byte[] bytes = new byte[bytesToSendRemaining - 1];
                    int i = 0;

                    while (--bytesToSendRemaining > 0) {
                        bytes[i] = (byte) (0xFF & bytesToSendBacking[bytesToSendMask & bytesToSendPosition++]);
                        i++;
                    }

//                    byte address = (byte) (0xFF & bytesToSendBacking[bytesToSendMask & bytesToSendPosition++]);
                    byte address = (0xc4 >> 1);

                    System.out.println("ORIGADDR " + (byte) (0xFF & bytesToSendBacking[bytesToSendMask & bytesToSendPosition++]));
                    for (byte b : bytes) {
                        System.out.println("B " + b);
                    }
                    System.out.println("ADDR " + address);

                    backing.write(address, bytes);

                    Pipe.confirmLowLevelRead(request, bytesToSendReleaseSize);
                    Pipe.releaseReads(request);
                }
            }
        }

        backing.update();
    }
}
