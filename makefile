default:
	@echo "Building developer instant-gratification files. Run the edison or pi targets for building those respective artifacts. Run the clean target before publishing changes."
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/linux -shared -o rs232.so -fPIC src/main/c/RS232.c
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/linux -shared -o raspicam4j.so -fPIC src/main/c/raspicam4j.c -lv4l2

clean:
	@echo "Clearing developer instant-gratification files."
	@rm rs232.so
	@rm raspicam4j.so

edison:
	@echo "This target MUST be run on a platform with JAVA_HOME properly configured and with the Edison cross compiler installed."
	@echo "If this build fails, make sure you remembered to source the edison environmental variables before running."
	@i586-poky-linux-gcc -lmraa -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" -shared -o src/main/resources/jni/i386-Linux/rs232.so -fPIC src/main/c/RS232.c
	@i586-poky-linux-gcc -lmraa -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" -shared -o src/main/resources/jni/i386-Linux/raspicam4j.so -fPIC src/main/c/raspicam4j.c -lv4l2

pi:
	@echo "This target MUST be run on a platform with JAVA_HOME properly configured and the g++-arm-linux-gnuabihf cross compiler package installed."
	@arm-linux-gnueabihf-gcc -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" -shared -o src/main/resources/jni/arm-Linux/rs232.so -fPIC src/main/c/RS232.c
	@arm-linux-gnueabihf-gcc -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" -shared -o src/main/resources/jni/arm-Linux/raspicam4j.so -fPIC src/main/c/raspicam4j.c -lv4l2

pi-native:
	@echo "This target MUST be run on a Raspberry Pi with JAVA_HOME properly configured and libv4l-dev installed."
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/linux -shared -o src/main/resources/jni/arm-Linux/rs232.so -fPIC src/main/c/RS232.c
	gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/linux -shared -o src/main/resources/jni/arm-Linux/raspicam4j.so -fPIC src/main/c/raspicam4j.c -lv4l2

devices: edison pi
