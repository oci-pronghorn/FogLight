#!/usr/bin/env bash
echo "This script MUST be run on the platform you are building for with JAVA_HOME properly configured."
i586-poky-linux-gcc -lmraa -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" -shared -o src/main/resources/jni/i386-Linux/rs232.so -fPIC src/main/c/RS232.c
