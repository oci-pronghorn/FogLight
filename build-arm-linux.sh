#!/usr/bin/env bash
echo "This script MUST be run on the platform you are building for."
gcc -I"/System/Library/Frameworks/JavaVM.framework/Headers" -shared -o src/main/resources/jni/arm-linux/rs232.so -fPIC src/main/c/rs232.c