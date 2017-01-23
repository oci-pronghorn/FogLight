#!/usr/bin/env bash
echo "This script MUST be run on the platform you are building for with JAVA_HOME properly configured."
gcc -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/darwin" -I"${JAVA_HOME}/include/linux}" -shared -o src/main/resources/jni/i386-linux/rs232.so -fPIC src/main/c/RS232.c
