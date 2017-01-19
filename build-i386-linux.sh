#!/usr/bin/env bash
echo "This script MUST be run on the platform you are building for."
gcc -I/usr/lib/jvm/java-1.7.0-openjdk-armhf/include -shared -o src/main/resources/jni/i386-linux/rs232.so -fPIC src/main/c/RS232.c
