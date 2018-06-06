package com.ociweb.iot.camera;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Proxy implementation of {@link Camera} that loads 1080p RGB24
 * raw images from the filesystem.
 *
 * TODO: Untested
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class ProxyCam implements Camera {

    public static final Path rootPath = Paths.get("./images");
    public static final File[] images;
    static {
        File file = rootPath.toFile();
        assert file.isDirectory();

        images = file.listFiles();

        assert images.length >= 1;
    }

    // 1080p RGB24 frames.
    public static final int FRAME_SIZE_BYTES = 1920 * 1080 * 3;

    private int imageIndex = 0;

    @Override
    public int open(String device, int width, int height) {
        return 0; /* No-op */
    }

    @Override
    public int getFrameSizeBytes(int fd) {
        return FRAME_SIZE_BYTES;
    }

    @Override
    public int readFrame(int fd, byte[] bytes, int start) {
        try (FileInputStream fis = new FileInputStream(images[(++imageIndex % images.length)])) {
            fis.read(bytes, start, FRAME_SIZE_BYTES);
            return FRAME_SIZE_BYTES;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int close(int fd) {
        return 0; /* No-op */
    }
}
