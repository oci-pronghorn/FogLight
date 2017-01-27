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
    read(fd, msg, size);
    jbyteArray array = (*env)->NewByteArray(env, size);
    (*env)->SetByteArrayRegion(env, array, 0, size, (jbyte *) msg);
    return array;
}

JNIEXPORT jbyteArray JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_read(JNIEnv *env, jobject object, jint fd, jint size) {
    fcntl(fd, F_SETFL, FNDELAY);
    char msg[size];
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