#!/usr/bin/env bash
echo "This script MUST be run on the platform you are building for with JAVA_HOME properly configured."
gcc -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/darwin" -shared -o src/main/resources/jni/arm-linux/rs232.so -fPIC src/main/c/RS232.c
cp src/main/resources/jni/arm-linux/rs232.so rs232.so