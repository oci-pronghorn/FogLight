package com.ociweb.iot.camera;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy implementation of {@link Camera} that loads 1080p RGB24
 * raw images from the filesystem.
 *
 * In this implementation, the "device" is a path to a folder
 * containing images on the filesystem.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class ProxyCam implements Camera {

    // Open camera file descriptors.
    private int cameraFds = 0;

    // Image files mapped by camera file descriptor.
    private Map<Integer, RandomAccessFile[]> camerasToFrames = new HashMap<>();
    private Map<Integer, Integer> camerasToFrameSizes = new HashMap<>();
    private Map<Integer, Integer> camerasToNextFrameIndices = new HashMap<>();

    @Override
    public int open(String device, int width, int height) {

        // Track the file descriptor of the camera we are creating.
        int cameraFd = cameraFds++;

        // Calculate frame size and initialize indices.
        camerasToFrameSizes.put(cameraFd,width * height * 3);
        camerasToNextFrameIndices.put(cameraFd, 0);

        // Discover files.
        File framesDirectory = Paths.get(device).toFile();
        assert framesDirectory.isDirectory();
        File[] directoryFiles = framesDirectory.listFiles();
        RandomAccessFile[] files = new RandomAccessFile[directoryFiles.length];
        for (int i = 0; i < files.length; i++) {
            try {
                files[i] = new RandomAccessFile(directoryFiles[i], "rw");
            }  catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        camerasToFrames.put(cameraFd, files);
        assert camerasToFrames.get(cameraFd).length >= 1;

        return cameraFd;
    }

    @Override
    public int getFrameSizeBytes(int fd) {
        return camerasToFrameSizes.getOrDefault(fd, -1);
    }

    @Override
    public int readFrame(int fd, ByteBuffer bytes, int start) {

        // Only read if the FD is valid.
        if (camerasToFrames.containsKey(fd)) {
            RandomAccessFile[] frames = camerasToFrames.get(fd);

            // Calculate index of the next frame to read.
            int nextFrameIndex = camerasToNextFrameIndices.get(fd) % frames.length;

            // Perform file read.
            try (FileChannel fis = frames[nextFrameIndex].getChannel()) {
                bytes.position(0);
                bytes.limit(bytes.capacity());
                return fis.read(bytes);

            } catch (IOException e) {
                e.printStackTrace();
                return -1;

            // Increment next frame index.
            } finally {
                camerasToNextFrameIndices.put(fd, nextFrameIndex + 1);
            }
        } else {
            return -1;
        }
    }

    @Override
    public int close(int fd) {
        camerasToFrames.remove(fd);
        camerasToFrameSizes.remove(fd);
        camerasToNextFrameIndices.remove(fd);
        return 0;
    }
}
