package com.ociweb.device.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashSet;
import java.util.Set;

public class GrovePiPinManager {

    public final Path[] gpio;
    public final Path[] gpioDirection;
    public final Path[] gpioValue;
    public final Path[] gpioDebugCurrentPinMux;

    public final short[] gpioPinInt;
    public final String[] gpioPinString;
    public FileSystemProvider provider;

    //TODO: Can't find these on the RPi w/ GrovePi+? I must be doing it wrong...
    //      The only devices found were under /sys/bus/sdio/devices
    public static final Path[] PATH_A = new Path[] {
        Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage0_raw"),
        Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage1_raw"),
        Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage2_raw"),
        Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage3_raw"),
        Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage4_raw"), //TODO: what if we read these data lines for I2C?
        Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage5_raw")  //TODO: what if we read these data lines for I2C?
    };

    private static final Path PATH_GPIO_EXPORT = Paths.get("/sys/class/gpio/export");
    private static final Path PATH_GPIO_UNEXPORT = Paths.get("/sys/class/gpio/unexport");

    public static final byte[] OUT = "out".getBytes();
    public static final byte[] IN = "in".getBytes();
    public static final byte[] DRECTION_HIGH = "high".getBytes();
    public static final byte[] DIRECTION_LOW = "low".getBytes();
    public static final byte[] VALUE_HIGH = "1".getBytes();
    public static final byte[] VALUE_LOW = "0".getBytes();
    public static final byte[] MODE_0 = "mode0".getBytes();
    public static final byte[] MODE_1 = "mode1".getBytes();
    public static final byte[] MODE_2 = "mode2".getBytes();
    public static final byte[] PULLUP = "pullup".getBytes();

    public static final ByteBuffer I2C_LOW = ByteBuffer.wrap(VALUE_LOW);
    public static final ByteBuffer I2C_HIGH = ByteBuffer.wrap(VALUE_HIGH);
    public static final int I2C_CLOCK = 5;
    public static final int I2C_DATA = 3;

    private static final Set<OpenOption> readOptions = new HashSet<OpenOption>();
    private static final Set<OpenOption> i2cOptions = new HashSet<OpenOption>();

    private static ByteBuffer[] readIntBuffer;
    private static ByteBuffer[] readBitBuffer;

    static {
        i2cOptions.add(StandardOpenOption.READ);
        i2cOptions.add(StandardOpenOption.WRITE);

        readOptions.add(StandardOpenOption.READ);
        readOptions.add(StandardOpenOption.SYNC);

        int a = PATH_A.length;
        readIntBuffer = new ByteBuffer[a];
        while (--a >= 0)
            readIntBuffer[a] = ByteBuffer.allocate(16);

        int b = EdisonConstants.GPIO_PINS.length;
        readBitBuffer = new ByteBuffer[b];
        while (--b >= 0)
            readBitBuffer[b] = ByteBuffer.allocate(1);
    }

    public GrovePiPinManager(final short[] pins) {

        gpioPinInt = pins;
        gpio = new Path[pins.length];
        gpioDirection = new Path[pins.length];
        gpioValue = new Path[pins.length];
        gpioPinString = new String[pins.length];
        gpioDebugCurrentPinMux = new Path[pins.length];//NOTE only needed for mode array

        final FileSystem fileSystem = FileSystems.getDefault();
        provider = fileSystem.provider();

        int i = pins.length;
        final StringBuilder sb = new StringBuilder();

        sb.append("/sys/class/gpio/gpio");

        final int baseLen = sb.length();
        while (--i >= 0) {
            if (pins[i] >= 0) {
                gpioPinString[i] = Integer.toString(pins[i]);

                //TODO: RPi default environment doesn't have this.
                gpioDebugCurrentPinMux[i] = fileSystem.getPath("/sys/kernel/debug/gpio_debug/gpio" + gpioPinString[i] + "/current_pinmux");

                sb.setLength(baseLen);
                sb.append(gpioPinString[i]);
                gpio[i] = fileSystem.getPath(sb.toString());

                final int withIdLen = sb.length();

                //TODO: Check for RPi compatibility.
                sb.append("/direction");
                gpioDirection[i] = fileSystem.getPath(sb.toString());

                sb.setLength(withIdLen);

                //TODO: Check for RPi compatibility.
                sb.append("/value");
                gpioValue[i] = fileSystem.getPath(sb.toString());

            }
        }
    }

    public void setDirectionLow(final int i) {
        if (null != gpioDirection[i]) try {
            Files.write(gpioDirection[i], DIRECTION_LOW);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDirectionHigh(final int i) {
        if (null != gpioDirection[i]) try {
            Files.write(gpioDirection[i], DRECTION_HIGH);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDebugCurrentPinmuxMode0(final int i) {
        if (null != gpioDebugCurrentPinMux[i]) try {
            Files.write(gpioDebugCurrentPinMux[i], MODE_0);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDebugCurrentPinmuxMode1(final int i) {
        try {
            Files.write(gpioDebugCurrentPinMux[i], MODE_1);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDebugCurrentPinmuxMode2(final int i) {
        try {
            Files.write(gpioDebugCurrentPinMux[i], MODE_2);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDirectionIn(final int i) {
        try {
            Files.write(gpioDirection[i], IN);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDirectionOut(final int i) {
        try {
            Files.write(gpioDirection[i], OUT);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValueHigh(final int i) {
        try {
            Files.write(gpioValue[i], VALUE_HIGH);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValueLow(final int i) {
        try {
            Files.write(gpioValue[i], VALUE_LOW);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void ensureDevice(final int i) {

        if ((null != gpio[i]) && !gpio[i].toFile().exists()) try {
            Files.write(PATH_GPIO_EXPORT, gpioPinString[i].getBytes());
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void removeDevice(final int i) {
        if ((null != gpio[i]) && !gpio[i].toFile().exists()) try {
            Files.write(PATH_GPIO_UNEXPORT, gpioPinString[i].getBytes());
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeValue(final int port, final ByteBuffer data, final EdisonPinManager d) {

        try {
            final SeekableByteChannel channel = d.provider.newByteChannel(d.gpioValue[port],
                                                                          i2cOptions);
            do
                channel.write(data);
            while (data.hasRemaining());//Caution, TODO: this is blocking.
            data.flip();
            channel.close();

        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static int readInt(final int idx) {

        try {
            final ByteBuffer buffer = readIntBuffer[idx];

            loadValueIntoBuffer(idx, buffer);

            int i = buffer.remaining();
            int result = 0;
            byte c;
            while ((--i >= 0) && ((c = buffer.get()) >= '0'))
                result = (result * 10) + (c - '0');

            return result;
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadValueIntoBuffer(final int idx, final ByteBuffer buffer) throws IOException {
        do {
            final SeekableByteChannel bc = GrovePiGPIO.gpioLinuxPins.provider.newByteChannel(PATH_A[idx],
                                                                                             readOptions);
            buffer.clear();
            while (bc.read(buffer) >= 0) {}
            bc.close();
            buffer.flip();
            //if length is 0 read this again.
        }
        while ((buffer.limit() == 0) || (buffer.get(0) < '0'));
    }

    public static int readBit(final int idx) {
        GrovePiGPIO.gpioLinuxPins.removeDevice(idx);
        GrovePiGPIO.gpioLinuxPins.ensureDevice(idx);

        try {
            final ByteBuffer buffer = readBitBuffer[idx];
            final SeekableByteChannel bc = GrovePiGPIO.gpioLinuxPins.provider.newByteChannel(GrovePiGPIO.gpioLinuxPins.gpioValue[idx],
                                                                                             readOptions);
            buffer.clear();
            while (bc.read(buffer) == 0) {}//only need 1
            bc.close();
            buffer.flip();
            return buffer.get() & 0x1;
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }
}
