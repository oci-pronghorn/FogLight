#include <stdio.h>   /* Standard input/output definitions */
#include <string.h>  /* String function definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <fcntl.h>   /* File control definitions */
#include <errno.h>   /* Error number definitions */
#include <termios.h> /* POSIX terminal control definitions */
#include <jni.h>

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232Native_open(JNIEnv *env, jobject object, jstring port, jint baud) {
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

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232Native_write(JNIEnv *env, jobject object, jint fd, jstring message) {
    const char *actualMessage = (*env)->GetStringUTFChars(env, message, NULL);
    return write(fd, actualMessage, strlen(actualMessage));
}

JNIEXPORT jstring JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232Native_readBlocking(JNIEnv *env, jobject object, jint fd, jint size) {
    fcntl(fd, F_SETFL, 0);
    char msg[size];
    read(fd, msg, size);
    return (*env)->NewStringUTF(env, msg);
}

JNIEXPORT jstring JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232Native_read(JNIEnv *env, jobject object, jint fd, jint size) {
    fcntl(fd, F_SETFL, FNDELAY);
    char msg[size];
    int readSize = read(fd, msg, size);
    if (readSize == size) {
        return (*env)->NewStringUTF(env, msg);
    } else {
        if (readSize < 0) {
            return (*env) ->NewStringUTF(env, "");
        }

        char actualMessage[readSize];
        strncpy(actualMessage, msg, readSize);
        return (*env)->NewStringUTF(env, actualMessage);
    }
}