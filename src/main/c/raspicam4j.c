#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <getopt.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <malloc.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/time.h>
#include <time.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include <asm/types.h>
#include <linux/videodev2.h>
#include <libv4l2.h>
#include <signal.h>
#include <stdint.h>
#include <inttypes.h>
#include <jni.h>

// Define booleans.
#define true (1==1)
#define false (!true)

// Structure for storing a buffer's meta-data.
struct Buffer {
    void* start;
    size_t size;
    struct v4l2_buffer info;
    int fd;
};

// Array of our buffers.
#define BUFFERS_COUNT 2
struct Buffer buffers[BUFFERS_COUNT];
int nextBufferToDequeue = 0;

// Userspace buffer object.
int userBufferCreated = false;
jfieldID byteBufferAddressField;
jobject userByteBuffer;

JNIEXPORT jint JNICALL Java_com_ociweb_iot_camera_RaspiCam_open(JNIEnv *env, jobject object, jstring device, jint width, jint height) {
    const char *actualDevice = (*env)->GetStringUTFChars(env, device, NULL);
    int fd = v4l2_open(actualDevice, O_RDWR | O_NONBLOCK, 0);
    if (fd < 0) {
        fprintf(stderr, "Could not open video feed at: %s.\n", actualDevice);
        return -1; // TODO: More descriptive error?
    } else {

        // Clear and setup pixel format.
        struct v4l2_format format;
        memset(&(format), 0, sizeof(format));
        format.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        format.fmt.pix.pixelformat = V4L2_PIX_FMT_RGB24;
        format.fmt.pix.width = width;
        format.fmt.pix.height = height;

        // Configure image format.
        if (v4l2_ioctl(fd, VIDIOC_S_FMT, &format) < 0) {
            v4l2_close(fd);
            fprintf(stderr, "Could not configure desired image format.\n");
            return -1; // TODO: More descriptive error?
        }

        // Validate formatting.
        if (format.fmt.pix.pixelformat != V4L2_PIX_FMT_RGB24) {
            fprintf(stderr, "Camera used %d format instead of %d.\n", format.fmt.pix.pixelformat, V4L2_PIX_FMT_RGB24);
        }

        if (format.fmt.pix.width != width) {
            fprintf(stderr, "Camera used %d width instead of %d.\n", format.fmt.pix.width, width);
        }

        if (format.fmt.pix.height != height) {
            fprintf(stderr, "Camera used %d height instead of %d.\n", format.fmt.pix.height, height);
        }

        if (format.fmt.pix.bytesperline != width * 3) {
        	fprintf(stderr, "Camera used %d bytes per line instead of the expected %d.\n", format.fmt.pix.bytesperline, width * 3);
        }

        // Request buffers.
        struct v4l2_requestbuffers bufrequest;
        bufrequest.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        bufrequest.memory = V4L2_MEMORY_MMAP;
        bufrequest.count = BUFFERS_COUNT;

        // Configure buffer format.
        if (v4l2_ioctl(fd, VIDIOC_REQBUFS, &bufrequest) < 0) {
            v4l2_close(fd);
            fprintf(stderr, "Could not request buffers.\n");
            return -1; // TODO: More descriptive error?
        }

        // Allocate buffers.
        for (int i = 0; i < BUFFERS_COUNT; i++) {
            struct Buffer buffer;

            // Set buffer file descriptor.
            buffer.fd = fd;

            // Get buffer size.
            buffer.size = format.fmt.pix.sizeimage;

            // Allocate buffer information.
            memset(&buffer.info, 0, sizeof(buffer.info));
            buffer.info.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
            buffer.info.memory = V4L2_MEMORY_MMAP;
            buffer.info.index = i;

            // Configure buffer with V4L2.
            if (v4l2_ioctl(fd, VIDIOC_QUERYBUF, &buffer.info) < 0) {
                v4l2_close(fd);
                fprintf(stderr, "Could not configure query buffers.\n");
                return -1; // TODO: More descriptive error?
            }

            // Memory map buffer data.
            buffer.start = v4l2_mmap(
                NULL,
                buffer.info.length,
                PROT_READ | PROT_WRITE,
                MAP_SHARED,
                fd,
                buffer.info.m.offset
            );

            // Validate memory map succeeded.
            if (buffer.start == MAP_FAILED) {
                v4l2_close(fd);
                fprintf(stderr, "Could not memory map camera.\n");
                return -1; // TODO: More descriptive error?
            }

            // Clean buffer data.
            memset(buffer.start, 0, buffer.info.length);

            // Place buffer in array.
            buffers[i] = buffer;
        }

        // Put the first buffer in the incoming queue.
        if (v4l2_ioctl(fd, VIDIOC_QBUF, &(buffers[0].info)) < 0) {
            fprintf(stderr, "Could not queue buffer (during open) with error: %d.\n", errno);
            return -1; // TODO: More descriptive error?
        }

        // Activate streaming
        if (v4l2_ioctl(fd, VIDIOC_STREAMON, &(buffers[0].info.type)) < 0) {
            v4l2_close(fd);
            fprintf(stderr, "Could not activate streaming.\n");
            return -1; // TODO: More descriptive error?
        }

        // Return descriptor.
        return fd;
    }
}

JNIEXPORT jobject JNICALL Java_com_ociweb_iot_camera_RaspiCam_getFrameBuffer(JNIEnv *env, jobject object, jint fd) {
    if (!userBufferCreated) {
        jclass byteBufferClass = (*env)->FindClass(env, "java/nio/Buffer");
        byteBufferAddressField = (*env)->GetFieldID(env, byteBufferClass, "address", "J");
        userByteBuffer = (*env)->NewDirectByteBuffer(env, buffers[0].start, buffers[0].size);
        userBufferCreated = true;
    }

    return userByteBuffer;
}

JNIEXPORT jint JNICALL Java_com_ociweb_iot_camera_RaspiCam_readFrame(JNIEnv *env, jobject object, jint fd) {

    // Fail if the user hasn't initialized their buffer yet.
    if (!userBufferCreated) {
        fprintf(stderr, "User must invoke getFrameBuffer at least once before reading a frame.");
        return -1;
    }

    // The buffer's waiting in the outgoing queue.
    // If -1 is returned, the buffer isn't ready to read yet.
    memset(&(buffers[nextBufferToDequeue].info), 0, sizeof(buffers[nextBufferToDequeue].info));
    buffers[nextBufferToDequeue].info.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    buffers[nextBufferToDequeue].info.memory = V4L2_MEMORY_USERPTR;
    if (v4l2_ioctl(fd, VIDIOC_DQBUF, &(buffers[nextBufferToDequeue].info)) < 0) {
        if (errno != EAGAIN) {
            fprintf(stderr, "Unknown error code %d when reading frame from camera.", errno);
        }

        return -1;
    }

    // Set user buffer address to the newly filled buffer.
    (*env)->SetObjectField(env, userByteBuffer, byteBufferAddressField, (intptr_t) buffers[nextBufferToDequeue].start);

    // Increment next buffer.
    nextBufferToDequeue++;
    nextBufferToDequeue = nextBufferToDequeue % BUFFERS_COUNT;

    // Put the buffer in the incoming queue.
    if (v4l2_ioctl(fd, VIDIOC_QBUF, &(buffers[nextBufferToDequeue].info)) < 0) {
        fprintf(stderr, "Could not queue buffer (during read).\n");
    }

    return buffers[0].info.length;
}

JNIEXPORT jint JNICALL Java_com_ociweb_iot_camera_RaspiCam_close(JNIEnv *env, jobject object, jint fd) {

    // Deactivate streaming
    if (v4l2_ioctl(fd, VIDIOC_STREAMOFF, &(buffers[0].info.type)) < 0){
        return -1; // TODO: More descriptive error?
    } else {
        v4l2_close(fd);
    }
}
