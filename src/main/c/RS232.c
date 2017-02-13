#include <stdio.h>   /* Standard input/output definitions */
#include <stdlib.h>
#include <string.h>  /* String function definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <fcntl.h>   /* File control definitions */
#include <errno.h>   /* Error number definitions */
#include <termios.h> /* POSIX terminal control definitions */
#include <jni.h>
#include <sys/ioctl.h>

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_open(JNIEnv *env, jobject object, jstring port, jint baud) {
    const char *actualPort = (*env)->GetStringUTFChars(env, port, NULL);
    int fd = open(actualPort, O_RDWR | O_NOCTTY | O_NDELAY);
    if (fd == -1) {
        return fd;
    } else {
        fcntl(fd, F_SETFL, 0);

        // Get port options.
        struct termios options;
        tcgetattr(fd, &options);

        // Set I/O baud to 19,200.
        cfsetispeed(&options, baud);
        cfsetospeed(&options, baud);

        // Something about receivers and local modes.
        options.c_cflag |= (CLOCAL | CREAD);

        // Apply the options.
        tcsetattr(fd, TCSANOW, &options);

        return fd;
    }
}

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_close(JNIEnv *env, jobject object, jint fd) {
    return close(fd);
}

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_write(JNIEnv *env, jobject object, jint fd, jbyteArray message) {
    jbyte* buffer = (*env)->GetByteArrayElements(env, message, NULL);
    int textLength = strlen((const char*) buffer);
    char* actualMessage = malloc(textLength + 1);
    memcpy(actualMessage, buffer, textLength);
    actualMessage[textLength] = '\0';
    (*env)->ReleaseByteArrayElements(env, message, buffer, 0);

    return write(fd, actualMessage, strlen(actualMessage));
}

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_getAvailableBytes(JNIEnv *env, jobject object, jint fd) {
    int bytes;
    ioctl(fd, FIONREAD, &bytes);
    return bytes;
}

JNIEXPORT jbyteArray JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_readBlocking(JNIEnv *env, jobject object, jint fd, jint size) {
    fcntl(fd, F_SETFL, 0);
    char msg[size];
    memset(msg, '\0', sizeof msg);
    read(fd, msg, size);
    jbyteArray array = (*env)->NewByteArray(env, size);
    (*env)->SetByteArrayRegion(env, array, 0, size, (jbyte *) msg);
    return array;
}

JNIEXPORT jbyteArray JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_read(JNIEnv *env, jobject object, jint fd, jint size) {
    fcntl(fd, F_SETFL, FNDELAY);
    char msg[size];
    memset(msg, '\0', sizeof msg);
    int readSize = read(fd, msg, size);
    if (readSize == size) {
        jbyteArray array = (*env)->NewByteArray(env, size);
        (*env)->SetByteArrayRegion(env, array, 0, size, (jbyte *) msg);
        return array;
    } else {
        if (readSize < 0) {
            return (*env)->NewByteArray(env, 0);
        }

        char actualMessage[readSize];
        strncpy(actualMessage, msg, readSize);
        jbyteArray array = (*env)->NewByteArray(env, readSize);
        (*env)->SetByteArrayRegion(env, array, 0, readSize, (jbyte *) actualMessage);
        return array;
    }
}

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_readInto(JNIEnv *env, jobject object, jint fd, jbyteArray rawBuffer, jint start, jint maxLength) {

    // Create our readBytesArray.
    fcntl(fd, F_SETFL, FNDELAY);
    char msg[maxLength];
    memset(msg, '\0', sizeof msg);
    int readSize = read(fd, msg, maxLength);

    jbyte* buffer = (*env)->GetByteArrayElements(env, rawBuffer, NULL);
    // jsize bufferLength = (*env)->GetArrayLength(env, rawBuffer);

    if (readSize > 0) {
        int readBytes = 0;

        int i;
        for (i = 0; i < maxLength; i++) {
            if (readBytes > readSize) {
                break;
            }

            buffer[start + i] = msg[readBytes];
            readBytes += 1;
        }

        (*env)->ReleaseByteArrayElements(env, rawBuffer, buffer, 0);
        return readBytes;
    } else {
        return -1;
    }
}

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_readIntoTwo(JNIEnv *env, jobject object, jint fd,
                        jbyteArray rawBuffer1, jint start1, jint maxLength1,
                        jbyteArray rawBuffer2, jint start2, jint maxLength2) {

        // Create our readBytesArray.
        int maxLength = maxLength1 + maxLength2;
        fcntl(fd, F_SETFL, FNDELAY);
        char msg[maxLength];
        memset(msg, '\0', sizeof msg);
        int readSize = read(fd, msg, maxLength);

        if (readSize > 0) {
            int readBytes = 0;

            // Open first JNI byte array.
            jbyte* buffer1 = (*env)->GetByteArrayElements(env, rawBuffer1, NULL);

            int i;
            for (i = 0; i < maxLength1; i++) {
                if (readBytes >= readSize) {
                    break;
                }

                buffer1[start1 + i] = msg[readBytes];
                readBytes += 1;
            }

            // Release JNI byte array and open up the second one.
            (*env)->ReleaseByteArrayElements(env, rawBuffer1, buffer1, 0);
            jbyte* buffer2 = (*env)->GetByteArrayElements(env, rawBuffer2, NULL);

            for (i = 0; i < maxLength2; i++) {
                if (readBytes >= readSize) {
                    break;
                }

                buffer2[start2 + i] = msg[readBytes];
                readBytes += 1;
            }

            // Release second JNI byte array.
            (*env)->ReleaseByteArrayElements(env, rawBuffer2, buffer2, 0);

            return readBytes;
        } else {
            return -1;
        }
    }