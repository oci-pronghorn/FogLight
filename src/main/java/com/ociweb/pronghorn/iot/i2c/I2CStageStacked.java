package com.ociweb.pronghorn.iot.i2c;

import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.*;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic I2C stage with native support for Linux systems.
 *
 * TODO: This stage can be cleaned up as it was (mostly) blindly copied from the pre-JNA I2C stage.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class I2CStageStacked {
    private static final Logger logger = LoggerFactory.getLogger(I2CStageStacked.class);
    private static final int NS_PAUSE = 10*1000;
    private static final int MAX_CONFIGURABLE_BYTES = 16;

    public static StackedStage provide(final GraphManager gm, final Pipe<I2CCommandSchema> request, final I2CBacking fallback) {
        return new StackedStage.Builder()
        .create(new StackedStageCreator() {
            @Override public void create(StackedStageContext context) {
                GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, NS_PAUSE, context.getStage());
                GraphManager.addNota(gm, GraphManager.PRODUCER, GraphManager.PRODUCER, context.getStage());

                //Setup context.
                context.set("request", request);
                context.set("fallback", fallback);

                //Set this thread to low priority to give other threads more resources.
                if (Thread.currentThread().getPriority() != Thread.MAX_PRIORITY) {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                }

                //Figure out which backing to use.
                //TODO: This should probably be chosen by the creator of this stage instead.
                try {
                    context.set(I2CBacking.class, new I2CNativeLinuxBacking());
                    logger.info("Successfully initialized native I2C backing.");
                } catch (Exception e) {
                    logger.warn("Couldn't start up native I2C backing; " +
                                        "Using fallback backing if present.");
                    if (fallback != null) {
                        context.set(I2CBacking.class, fallback);
                    } else {
                        logger.error("Fallback backing not present; I2C stage shutting down.");
                        context.getStage().requestShutdown();
                    }
                }
            }
        })

        .update(new StackedStageUpdater() {
            //Current byte buffer.
            private int[] cyclesToWaitLookup = new int[MAX_CONFIGURABLE_BYTES];

            //Holds the same array as used by the Blob from the ring.
            private byte[] bytesToSendBacking;
            private int    bytesToSendRemaining;
            private int    bytesToSendPosition;
            private int    bytesToSendMask;
            private int    bytesToSendReleaseSize;

            @Override public void update(StackedStageContext context) {
                //TODO: This logic can definitely be cleaned up.
                if (Pipe.hasContentToRead(request)) {
                    //Verify message ID.
                    int msgId = Pipe.takeMsgIdx(request);
                    if (msgId < 0 ) {
                        context.getStage().requestShutdown();
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
                            int cyclesToWait = bytesToSendPosition < MAX_CONFIGURABLE_BYTES ? cyclesToWaitLookup[bytesToSendPosition] : 0;
                            int byteToSend = 0xFF & bytesToSendBacking[bytesToSendMask & bytesToSendPosition++];

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

                        //TODO: For some reason, this gives something like 0xffffff6 if we don't assign to a temporary int first.
                        int temp = 0xFF & bytesToSendBacking[bytesToSendMask & bytesToSendPosition++];
                        byte address = (byte) (temp >> 1);

                        context.get(I2CBacking.class).write(address, bytes);

                        Pipe.confirmLowLevelRead(request, bytesToSendReleaseSize);
                        Pipe.releaseReads(request);
                    }
                }
            }
        })

        .build(gm);
    }
}
