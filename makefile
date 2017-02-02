default:
	@echo "Building developer instant-gratification files. Run the edison or pi targets for building those respective artifacts. Run the clean target before publishing changes."
	@gcc -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/darwin" -I"${JAVA_HOME}/include/linux" -shared -o rs232.so -fPIC src/main/c/RS232.c

clean:
	@echo "Clearing developer instant-gratification files."
	@rm rs232.so

edison:
	@echo "This target MUST be run on a platform with JAVA_HOME properly configured and with the Edison cross compiler installed."
	@i586-poky-linux-gcc -lmraa -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" -shared -o src/main/resources/jni/i386-Linux/rs232.so -fPIC src/main/c/RS232.c

pi:
	@echo "This target MUST be run on the platform you are building for with JAVA_HOME properly configured."
	@gcc -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" -shared -o src/main/resources/jni/arm-Linux/rs232.so -fPIC src/main/c/RS232.c