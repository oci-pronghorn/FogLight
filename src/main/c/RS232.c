#include <stdio.h>   /* Standard input/output definitions */
#include <stdlib.h>
#include <string.h>  /* String function definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <fcntl.h>   /* File control definitions */
#include <errno.h>   /* Error number definitions */
#include <termios.h> /* POSIX terminal control definitions */
#include <jni.h>
#include <sys/ioctl.h>

struct serial_struct {
	int	type;
	int	line;
	unsigned int	port;
	int	irq;
	int	flags;
	int	xmit_fifo_size;
	int	custom_divisor;
	int	baud_base;
	unsigned short	close_delay;
	char	io_type;
	char	reserved_char[1];
	int	hub6;
	unsigned short	closing_wait; /* time to wait before closing */
	unsigned short	closing_wait2; /* no longer used... */
	unsigned char	*iomem_base;
	unsigned short	iomem_reg_shift;
	unsigned int	port_high;
	unsigned long	iomap_base;	/* cookie passed into ioremap */
};

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_open(JNIEnv *env, jobject object, jstring port, jint baud) {
    const char *actualPort = (*env)->GetStringUTFChars(env, port, NULL);
    int fd = open(actualPort, O_RDWR | O_NOCTTY | O_NDELAY | O_NONBLOCK);
    if (fd == -1) {
        return fd;
    } else {
        // Get port options.
        struct termios options;
        tcgetattr(fd, &options);

        // Set I/O baud.
        cfsetispeed(&options, baud);
        cfsetospeed(&options, baud);

        // Something about receivers and local modes.
        options.c_cflag |= (CLOCAL | CREAD);
        options.c_oflag &= ~OPOST;

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
    int status = write(fd, buffer, (*env)->GetArrayLength(env, message));
    (*env)->ReleaseByteArrayElements(env, message, buffer, 0);
    return status;
}

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_getBytesInOutputBuffer(JNIEnv *env, jobject object, jint fd) {
    // Read bytes in the queue.
    int bytes;
    ioctl(fd, TIOCOUTQ, &bytes);

    // Return the difference.
    return bytes;
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

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_writeFrom(JNIEnv *env, jobject object, jint fd, jbyteArray rawBuffer, jint start, jint maxLength) {
    jbyte* buffer = (*env)->GetByteArrayElements(env, rawBuffer, NULL);
    int status = write(fd, buffer, maxLength);
    (*env)->ReleaseByteArrayElements(env, rawBuffer, buffer, 0);
    return status;
}

JNIEXPORT jint JNICALL Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_writeFromTwo(JNIEnv *env, jobject object, jint fd,
                        jbyteArray rawBuffer1, jint start1, jint maxLength1,
                        jbyteArray rawBuffer2, jint start2, jint maxLength2) {

    int len1 = Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_writeFrom(env, object, fd, rawBuffer1, start1, maxLength1);

    if (len1 != -1) {
        int len2 = Java_com_ociweb_pronghorn_iot_rs232_RS232NativeLinuxBacking_writeFrom(env, object, fd, rawBuffer2, start2, maxLength2);
        if (len2 != -1) {
            return len1 + len2;
        } else {
            return len1;
        }
    } else {
        return -1;
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