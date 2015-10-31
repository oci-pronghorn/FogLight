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
import java.util.concurrent.locks.LockSupport;

import com.ociweb.device.grove.GroveShieldV2ResponseStage;

public class EdisonPinManager {

    public final Path[] gpio; 
    public final Path[] gpioDirection;
    public final Path[] gpioValue;
    public final Path[] gpioDebugCurrentPinMux;
    
    
    public final short[]    gpioPinInt;
    public final String[] gpioPinString;
    public FileSystemProvider provider;
    
    public static final Path[] PATH_A = new Path[] {
             Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage0_raw"),
             Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage1_raw"),
             Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage2_raw"),
             Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage3_raw")
    };

    
    
    private static final Path PATH_GPIO_EXPORT   = Paths.get("/sys/class/gpio/export");
    private static final Path PATH_GPIO_UNEXPORT = Paths.get("/sys/class/gpio/unexport");
        
    public static final byte[] OUT            = "out".getBytes();
    public static final byte[] IN             = "in".getBytes();
    public static final byte[] DRECTION_HIGH  = "high".getBytes();
    public static final byte[] DIRECTION_LOW  = "low".getBytes();
    public static final byte[] VALUE_HIGH     = "1".getBytes();
    public static final byte[] VALUE_LOW      = "0".getBytes();  
    public static final byte[] MODE_0         = "mode0".getBytes();
    public static final byte[] MODE_1         = "mode1".getBytes();
    public static final byte[] MODE_2         = "mode2".getBytes();
    public static final byte[] PULLUP         = "pullup".getBytes();
    
        
    private static final ByteBuffer readBitBuffer = ByteBuffer.allocate(1);
    private static final Set<OpenOption> readOptions = new HashSet<OpenOption>();
    private static final Set<OpenOption> i2cOptions = new HashSet<OpenOption>();
    static {
        i2cOptions.add(StandardOpenOption.READ);
        i2cOptions.add(StandardOpenOption.WRITE);
    }
    private static final ByteBuffer readIntBuffer = ByteBuffer.allocate(16);
    private static final ByteBuffer writeIntBuffer = ByteBuffer.allocate(16);
    
    public EdisonPinManager(short[] pins) {
        
        gpioPinInt = pins;
        gpio = new Path[pins.length];    
        gpioDirection = new Path[pins.length];
        gpioValue = new Path[pins.length];
        gpioPinString = new String[pins.length];
        gpioDebugCurrentPinMux = new Path[pins.length];//NOTE only needed for mode array
        
        FileSystem fileSystem = FileSystems.getDefault();
        this.provider = fileSystem.provider();
        
        int i = pins.length;
        StringBuilder sb = new StringBuilder();
        sb.append("/sys/class/gpio/gpio");
        int baseLen = sb.length();
        while (--i>=0) {
            if (pins[i]>=0) {
                gpioPinString[i] = Integer.toString(pins[i]);
                
                gpioDebugCurrentPinMux[i] = fileSystem.getPath("/sys/kernel/debug/gpio_debug/gpio"+gpioPinString[i]+"/current_pinmux");

                sb.setLength(baseLen);
                sb.append(gpioPinString[i]);
                gpio[i]          = fileSystem.getPath(sb.toString());
                                
                int withIdLen = sb.length();
                sb.append("/direction");
                gpioDirection[i] = fileSystem.getPath(sb.toString());
                
                sb.setLength(withIdLen);
                sb.append("/value");
                gpioValue[i]    = fileSystem.getPath(sb.toString());   
                
                
            }
        }
    }

    public void setDirectionLow(int i) {
        if (null!=gpioDirection[i]) {
            try {
                Files.write(gpioDirection[i],DIRECTION_LOW);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setDirectionHigh(int i) {
        if (null!=gpioDirection[i]) {
            try {
                Files.write(gpioDirection[i],DRECTION_HIGH);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    

//    
//    public void setDebugCurrentPullModePullup(int i) {
//        if (null!= gpioDebugPullMode[i]) {
//            try {
//                Files.write(gpioDebugPullMode[i],PULLUP);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

    
    public void setDebugCurrentPinmuxMode0(int i) {
        if (null!= gpioDebugCurrentPinMux[i]) {
            try {
                Files.write(gpioDebugCurrentPinMux[i],MODE_0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setDebugCurrentPinmuxMode1(int i) {
        try {
            Files.write(gpioDebugCurrentPinMux[i],MODE_1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDebugCurrentPinmuxMode2(int i) {
        try {
            Files.write(gpioDebugCurrentPinMux[i],MODE_2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDirectionIn(int i) {
        try {
            Files.write(gpioDirection[i],IN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDirectionOut(int i) {
        try {
            Files.write(gpioDirection[i],OUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValueHigh(int i) {
        try {
            Files.write(gpioValue[i],VALUE_HIGH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void setValueLow(int i) {
        try {
            Files.write(gpioValue[i],VALUE_LOW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void ensureDevice(int i) {
        
        if (null!=gpio[i] && !gpio[i].toFile().exists()) {
            try {
                Files.write(PATH_GPIO_EXPORT, gpioPinString[i].getBytes());
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
        }
                
    }
    
    public void removeDevice(int i) {
        if (null!=gpio[i] && !gpio[i].toFile().exists()) {
            try {
                Files.write(PATH_GPIO_UNEXPORT, gpioPinString[i].getBytes());
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
        }
    }
    
    public static void writeValue(int port, ByteBuffer data, EdisonPinManager d) {

            try {
                SeekableByteChannel channel = d.provider.newByteChannel(d.gpioValue[port],i2cOptions);
                do {
                    channel.write(data);
                } while (data.hasRemaining());//Caution, TODO: this is blocking.
                data.flip();
                channel.close();
            
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

    }
    
    
  


    
    
    public static int readInt(int idx, EdisonPinManager d) {
        synchronized(readIntBuffer) {
            try {
                int result = -1;
        
                int i = 0;
                do {            
                    SeekableByteChannel bc =d.provider.newByteChannel(PATH_A[idx], readOptions);
                    readIntBuffer.clear();
                    while (bc.read(readIntBuffer)>=0);
                    bc.close();
                    readIntBuffer.flip();              
                    i = readIntBuffer.remaining();//if length is 0 read this again.
                } while (i==0 || (readIntBuffer.get(0)<'0'));
                
                result = 0;    
                byte c;
                while (--i>=0 && ((c=readIntBuffer.get())>='0')) {
                    result= (result*10)+(c-'0');
                }
        
               return result;  
           } catch (IOException e) {
               throw new RuntimeException(e);
           }
        }
    }

    public static int readBit(int idx, EdisonPinManager d) {
        synchronized(readBitBuffer) {
            try {        
                
                SeekableByteChannel bc =d.provider.newByteChannel(d.gpioValue[idx], readOptions);
                readBitBuffer.clear();
                while (bc.read(readBitBuffer)==0); //only need 1
                bc.close();
                readBitBuffer.flip();
                return readBitBuffer.get()&0x1;
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
        }
    }

}
