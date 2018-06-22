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
    private Map<Integer, ByteBuffer> camerasToBuffers = new HashMap<>();
    private Map<Integer, Integer> camerasToNextFrameIndices = new HashMap<>();

    @Override
    public int open(String device, int width, int height) {

        // Track the file descriptor of the camera we are creating.
        int cameraFd = cameraFds++;

        // Calculate frame size and initialize indices.
        camerasToBuffers.put(cameraFd, ByteBuffer.allocateDirect(width * height * 3));
        camerasToNextFrameIndices.put(cameraFd, 0);

        // Discover files.
        File framesDirectory = Paths.get(device).toFile();
        System.out.println("Reading directory" + framesDirectory.getAbsolutePath());
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
    public ByteBuffer getFrameBuffer(int fd) {
        return camerasToBuffers.getOrDefault(fd, null);
    }

    @Override
    public long readFrame(int fd) {

        // Only read if the FD is valid.
        if (camerasToFrames.containsKey(fd)) {
            RandomAccessFile[] frames = camerasToFrames.get(fd);

            // Calculate index of the next frame to read.
            int nextFrameIndex = camerasToNextFrameIndices.get(fd) % frames.length;

            // Perform file read.
            try {
                FileChannel fis = frames[nextFrameIndex].getChannel();
                fis.position(0);
                ByteBuffer buffer = camerasToBuffers.get(fd);
                buffer.position(0);
                buffer.limit(buffer.capacity());

                // Read size must be exactly a frame, or else we return -1.
                if (fis.read(buffer) == camerasToBuffers.get(fd).capacity()) {
                    return System.currentTimeMillis();
                } else {
                    return -1;
                }

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
        camerasToBuffers.remove(fd);
        camerasToNextFrameIndices.remove(fd);
        return 0;
    }
}
