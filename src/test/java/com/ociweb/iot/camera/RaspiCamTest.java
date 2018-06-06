package com.ociweb.iot.camera;

import org.junit.Ignore;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RaspiCamTest {

    public static final int IMAGE_WIDTH = 1920;
    public static final int IMAGE_HEIGHT = 1080;

    @Test
    @Ignore
    public void shouldHaveACoolApi() throws IOException {

        // Create a RaspiCam.
        Camera cam = new RaspiCam();
        int fd = cam.open(RaspiCam.DEFAULT_CAMERA_DEVICE, IMAGE_WIDTH, IMAGE_HEIGHT);

        // Create an image file buffer.
        byte[] bytes = new byte[cam.getFrameSizeBytes(fd)];

        // Capture an image.
        int i = 0;
        while (cam.readFrame(fd, bytes, 0) <= 0) { i++; }
        System.out.printf("Read %d times before success.\n", i);

        // Close the cam.
        cam.close(fd);

        // Write RAW bytes to RAW.
        try (FileOutputStream fos = new FileOutputStream("test.raw")) {
            fos.write(bytes);
        }

        // Convert RAW bytes to JPG.
        // https://stackoverflow.com/a/31461720
        int samplesPerPixel = 3;
        int[] bandOffsets = {0, 1, 2}; // Locations of R, G, B in each tuple of pixel data.

        DataBuffer dataBuffer = new DataBufferByte(bytes, bytes.length);
        WritableRaster writableRaster = Raster.createInterleavedRaster(dataBuffer,
                                                                       IMAGE_WIDTH, IMAGE_HEIGHT,
                                                                       samplesPerPixel * IMAGE_WIDTH,
                                                                       samplesPerPixel, bandOffsets, null);

        ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                                        false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);

        BufferedImage bufferedImage = new BufferedImage(colorModel, writableRaster, colorModel.isAlphaPremultiplied(), null);

        ImageIO.write(bufferedImage, "JPG", new File("test.jpg"));
    }
}
