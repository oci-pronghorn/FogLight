package com.ociweb.iot.maker;

import com.ociweb.iot.camera.Camera;
import com.ociweb.iot.camera.ProxyCam;
import com.ociweb.iot.camera.RaspiCam;
import com.ociweb.pronghorn.pipe.PipeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * Time-based image listener backing for Raspberry Pi hardware.
 *
 * This stage passes image frames line-by-line to its consumers.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class PiImageListenerStage extends PronghornStage {

    private static final Logger logger = LoggerFactory.getLogger(PiImageListenerStage.class);

    // Frame buffer state constants.
    private static final int FRAME_EMPTY = -1;
    private static final int FRAME_BUFFERED = -2;

    // Output pipe for image data.
    private final Pipe<ImageSchema> output;

    // Camera system.
    private Camera camera;
    private int cameraFd;

    // Image buffer information; we only process one image at a time.
    private byte[] frameBytes = null;
    private int frameBytesHead = FRAME_EMPTY;

    // Frame size data.
    public static final int FRAME_WIDTH = 1280;
    public static final int FRAME_HEIGHT = 720;
    public static final int ROW_SIZE = FRAME_WIDTH * 3;

    // Proxy data directory.
    public static final String PROXY_CAMERA_DIRECTORY = "./src/test/images";

    // Output encoding.
    public static final byte[] OUTPUT_ENCODING = "RGB24".getBytes(StandardCharsets.US_ASCII);

    public PiImageListenerStage(GraphManager graphManager, Pipe<ImageSchema> output, int triggerRateMilliseconds) {
        super(graphManager, NONE, output);

        // Attach to our output pipe.
        this.output = output;

        // Add this listener to the graph.
        GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, triggerRateMilliseconds * 1000000L, this);
    }

    @Override
    public void startup() {

        // Get a file for the default camera device.
        File cameraFile = Paths.get(RaspiCam.DEFAULT_CAMERA_DEVICE).toFile();

        // Open /dev/video0 on Raspberry Pi.
        if (!cameraFile.exists()) {

            // Load V4L2 module.
            try {
                Runtime.getRuntime().exec("modprobe bcm2835-v4l2").waitFor();
            } catch (IOException | InterruptedException e) {
                logger.warn("Could not load V4L2 driver via modprobe. Proxy camera will be used.");
            }
        }

        // Open camera interface if the camera is available.
        if (cameraFile.exists()) {
            camera = new RaspiCam();
            cameraFd = camera.open(RaspiCam.DEFAULT_CAMERA_DEVICE, FRAME_WIDTH, FRAME_HEIGHT);
            logger.info("Opened camera device {} with FD {}.", RaspiCam.DEFAULT_CAMERA_DEVICE, cameraFd);

        // Otherwise, use a proxy camera.
        } else {
            camera = new ProxyCam();
            cameraFd = camera.open(PROXY_CAMERA_DIRECTORY, FRAME_WIDTH, FRAME_HEIGHT);
            logger.info("Opened proxy camera in directory {} with FD {}.", PROXY_CAMERA_DIRECTORY, cameraFd);
        }

        // Configure byte array for camera frames.
        frameBytes = new byte[camera.getFrameSizeBytes(cameraFd)];
    }

    @Override
    public void shutdown() {
		if (Pipe.hasRoomForWrite(output, Pipe.EOF_SIZE)) {
			Pipe.publishEOF(output);
		}

		// Close camera.
        camera.close(cameraFd);
    }

    @Override
    public void run() {

        // Only execute while we have room to write our output.
        // TODO: Is this check required, or does tryWriteFragment already do it?
        if (Pipe.hasRoomForWrite(output)) {

            // Capture a frame if we have no bytes left to transmit.
            if (frameBytesHead == FRAME_EMPTY && camera.readFrame(cameraFd, frameBytes, 0) == frameBytes.length) {
                frameBytesHead = FRAME_BUFFERED;
            }

            // Publish a frame start if we have room to transmit.
            if (frameBytesHead == FRAME_BUFFERED && PipeWriter.tryWriteFragment(output, ImageSchema.MSG_FRAMESTART_1)) {
                PipeWriter.writeInt(output, ImageSchema.MSG_FRAMESTART_1_FIELD_WIDTH_101, FRAME_WIDTH);
                PipeWriter.writeInt(output, ImageSchema.MSG_FRAMESTART_1_FIELD_HEIGHT_201, FRAME_HEIGHT);
                PipeWriter.writeLong(output, ImageSchema.MSG_FRAMESTART_1_FIELD_TIMESTAMP_301, System.currentTimeMillis());
                PipeWriter.writeInt(output, ImageSchema.MSG_FRAMESTART_1_FIELD_FRAMEBYTES_401, frameBytes.length);
                PipeWriter.writeInt(output, ImageSchema.MSG_FRAMESTART_1_FIELD_BITSPERPIXEL_501, 24);
                PipeWriter.writeBytes(output, ImageSchema.MSG_FRAMESTART_1_FIELD_ENCODING_601, OUTPUT_ENCODING);
                PipeWriter.publishWrites(output);
                frameBytesHead = 0;
            }

            // Write rows while there are bytes to write and room for those bytes.
            while (frameBytesHead >= 0 &&
                    PipeWriter.hasRoomForFragmentOfSize(output, Pipe.sizeOf(output, ImageSchema.MSG_FRAMECHUNK_2)) &&
                    PipeWriter.tryWriteFragment(output, ImageSchema.MSG_FRAMECHUNK_2)) {

                // Write bytes.
                PipeWriter.writeBytes(output, ImageSchema.MSG_FRAMECHUNK_2_FIELD_ROWBYTES_102,
                                      frameBytes, frameBytesHead, ROW_SIZE);
                PipeWriter.publishWrites(output);

                // Progress head.
                frameBytesHead += ROW_SIZE;

                // If the head exceeds the size of the frame bytes, we're done writing.
                if (frameBytesHead >= frameBytes.length) {
                    frameBytesHead = FRAME_EMPTY;
                }
            }
        }
    }
}
