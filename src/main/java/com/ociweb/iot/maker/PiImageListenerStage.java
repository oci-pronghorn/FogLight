package com.ociweb.iot.maker;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

/**
 * Time-based image listener backing for Raspberry Pi hardware.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class PiImageListenerStage extends PronghornStage {

    private static final Logger logger = LoggerFactory.getLogger(PiImageListenerStage.class);

    // Output pipe for image data.
    private final Pipe<RawDataSchema> output;

    // Image buffer information; we only process one image at a time.
    private byte[] fileBytes = null;
    private int fileBytesReadIndex = -1;

    /**
     * Takes a picture from the currently connected Raspberry Pi camera and
     * saves it to a file.
     *
     * TODO: This method is primitive in that it relies on Pi command line tools
     *       and unknown delays in order to capture images. It would be preferable
     *       to leverage the native APIs in the Raspberry Pi, but those are
     *       extremely complex and undocumented...
     *
     * @param fileName Name of the file (without extensions) to save
     *                 the image to.
     *
     * @return A reference to the created image file.
     */
    private File takePicture(String fileName) {
        try {
            Runtime.getRuntime().exec("raspistill --nopreview --timeout 1 --shutter 2500 --width 1280 --height 960 --quality 75 --output " + fileName + ".jpg");
        } catch (IOException e) {
            logger.error("Unable to take picture from Raspberry Pi Camera due to error [{}].", e.getMessage(), e);
        }

        return new File(fileName + ".jpg"); //TODO: rewrite to be GC free
    }

    public PiImageListenerStage(GraphManager graphManager, Pipe<RawDataSchema> output, int triggerRateMilliseconds) {
        super(graphManager, NONE, output);

        // Attach to our output pipe.
        this.output = output;

        // Add this listener to the graph.
        GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, triggerRateMilliseconds, this);
    }

    @Override
    public void shutdown() {
        // Wait for room on the pipe to write any remaining data.
        Pipe.spinBlockForRoom(output, Pipe.EOF_SIZE);

        // Publish remaining data.
        Pipe.publishEOF(output);
    }

    @Override
    public void run() {

        // Only execute while we have room to write our output.
        if (Pipe.hasRoomForWrite(output)) {

            // Do we have image data? If not, get some!
            if (fileBytesReadIndex == -1) {
                // TODO: rewrite to be GC free
                // TODO: RandomAccessFile may be the best choice once it's available to us...
                // Take a picture and load the byte information.
                File imageFile = takePicture("Pronghorn-Image-Capture-" + System.currentTimeMillis());
                try {
                    fileBytes = Files.readAllBytes(imageFile.toPath());
                    fileBytesReadIndex = 0;
                } catch (IOException e) {
                    // TODO: Exception or just a log error and no write?
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            // Load byte buffers from the pipe so we have somewhere to put the image data.
            ByteBuffer[] buffers = Pipe.wrappedWritingBuffers(output);

            // Determine maximum write size.
            final int maximumToWrite = Math.min(buffers[0].remaining(), fileBytes.length - fileBytesReadIndex);

            // Fill the buffers as much as possible.
            System.arraycopy(fileBytes, fileBytesReadIndex,
                             buffers[0].array(), buffers[0].position(), maximumToWrite);

            // Progress index by the number of bytes written.
            fileBytesReadIndex += maximumToWrite;

            // If the index exceeds our bounds, we're done writing.
            if (fileBytesReadIndex >= fileBytes.length) {
                fileBytesReadIndex = -1;
            }

            // Publish our changes.
            final int size = Pipe.addMsgIdx(output, RawDataSchema.MSG_CHUNKEDSTREAM_1);
            Pipe.moveBlobPointerAndRecordPosAndLength(maximumToWrite, output);
            Pipe.confirmLowLevelWrite(output, size);
            Pipe.publishWrites(output);
        }
    }
}
