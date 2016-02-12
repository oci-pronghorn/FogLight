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
    public final Path[] gpioDebugCurrentPinMux;
    public final Path[] gpioValue;
    public final SeekableByteChannel[] gpioChannel;    
    
    public final short[]    gpioPinInt;
    public final String[] gpioPinString;
    public FileSystemProvider provider;

    
    public static final Path[] PATH_A = new Path[] {
             Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage0_raw"),
             Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage1_raw"),
             Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage2_raw"),
             Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage3_raw"), 
             Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage4_raw"), //TODO: what if we read these data lines for I2C?
             Paths.get("/sys/bus/iio/devices/iio:device1/in_voltage5_raw")  //TODO: what if we read these data lines for I2C?
             
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
    
    public static final ByteBuffer I2C_LOW  = ByteBuffer.wrap(VALUE_LOW);
    public static final ByteBuffer I2C_HIGH = ByteBuffer.wrap(VALUE_HIGH);
    public static final ByteBuffer I2C_OUT  = ByteBuffer.wrap(OUT);
    public static final ByteBuffer I2C_IN   = ByteBuffer.wrap(IN);
    public static final ByteBuffer I2C_DIRECTION_LOW  = ByteBuffer.wrap(DIRECTION_LOW);
    public static final ByteBuffer I2C_DIRECTION_HIGH = ByteBuffer.wrap(DRECTION_HIGH); 
    
    public static final int I2C_CLOCK = 19;
    public static final int I2C_DATA = 18;
        
    private static final Set<OpenOption> readOptions = new HashSet<OpenOption>();
    private static final Set<OpenOption> i2cOptions = new HashSet<OpenOption>();

    private static ByteBuffer[] readIntBuffer;
    private static ByteBuffer[] readBitBuffer;
    
    static {
        i2cOptions.add(StandardOpenOption.READ);
        i2cOptions.add(StandardOpenOption.WRITE);
        
        readOptions.add(StandardOpenOption.READ);        

        int a = PATH_A.length;
        readIntBuffer = new ByteBuffer[a];
        while (--a>=0) {
            readIntBuffer[a] = ByteBuffer.allocate(16);            
        }
        
        int b = EdisonConstants.GPIO_PINS.length;
        readBitBuffer = new ByteBuffer[b];
        while (--b>=0) {
            readBitBuffer[b] = ByteBuffer.allocate(1);
        }
    }
    
    public EdisonPinManager(short[] pins) {
        
        gpioPinInt = pins;
        gpio = new Path[pins.length];    
        gpioDirection = new Path[pins.length];
        gpioValue = new Path[pins.length];
        gpioChannel = new SeekableByteChannel[pins.length];
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
        write(this,gpioDirection[i], I2C_DIRECTION_LOW);
    }

    public void setDirectionHigh(int i) {
        write(this,gpioDirection[i], I2C_DIRECTION_HIGH);
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
        write(this,gpioDirection[i], I2C_IN);
    }

    public void setDirectionOut(int i) {
        write(this,gpioDirection[i], I2C_OUT);
    }

    public void setValueHigh(int i) {
        write(this,gpioValue[i], I2C_HIGH);
    }


    public void setValueLow(int i) {
        write(this,gpioValue[i], I2C_LOW);
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
                //TODO: TEST IF THIS CHANNEL CAN BE KEPT INSTEAD OF RE-CREATING ONE EACH TIME.
                
                SeekableByteChannel channel = d.gpioChannel[port];
                if (null == channel) {
                        
                  channel = d.provider.newByteChannel(d.gpioValue[port],i2cOptions);
                  d.gpioChannel[port] = channel;
                 
               }
                channel.position(0);
                do {
                    channel.write(data);
                } while (data.hasRemaining());//Caution, this is blocking.
                data.flip();
            //    channel.close();
            
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

    }

    private static void write(EdisonPinManager d, Path path, ByteBuffer data) {
        try {
            //TODO: TEST IF THIS CHANNEL CAN BE KEPT INSTEAD OF RE-CREATING ONE EACH TIME.
            SeekableByteChannel channel = d.provider.newByteChannel(path,i2cOptions);
            do {
                channel.write(data);
            } while (data.hasRemaining());//Caution, this is blocking.
            data.flip();
            channel.close();
        
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
  


    
    
    public static int readInt(int idx) {

            try {
                ByteBuffer buffer = readIntBuffer[idx];
                
                loadValueIntoBuffer(idx, buffer);
                
                int i = buffer.remaining();
                int result = 0;    
                byte c;
                while (--i>=0 && ((c=buffer.get())>='0')) {
                    result= (result*10)+(c-'0');
                }
        
               return result;  
           } catch (IOException e) {
               throw new RuntimeException(e);
           }
    }

    private static void loadValueIntoBuffer(int idx, ByteBuffer buffer) throws IOException {
        do {            
            SeekableByteChannel bc =EdisonGPIO.gpioLinuxPins.provider.newByteChannel(PATH_A[idx], readOptions);
            buffer.clear();
            while (bc.read(buffer)>=0){}
            bc.close();
            buffer.flip();              
            //if length is 0 read this again.
        } while (buffer.limit()==0 || (buffer.get(0)<'0'));
    }
    
    
    static SeekableByteChannel bc1 = null;
    static SeekableByteChannel bc2 = null;

    public static int readBit(int idx) {
            try {        
                //System.err.println(idx);
                
                SeekableByteChannel bc;
                if (18==idx) {
                    if (bc1 == null) {
                        bc1 = EdisonGPIO.gpioLinuxPins.provider.newByteChannel(EdisonGPIO.gpioLinuxPins.gpioValue[idx], readOptions);
                    }
                    bc = bc1;
                    
                } else if (19==idx) {
                    if (bc2 == null) {
                        bc2 = EdisonGPIO.gpioLinuxPins.provider.newByteChannel(EdisonGPIO.gpioLinuxPins.gpioValue[idx], readOptions);
                    }
                    bc = bc2;
                } else {
                                
                    bc =EdisonGPIO.gpioLinuxPins.provider.newByteChannel(EdisonGPIO.gpioLinuxPins.gpioValue[idx], readOptions);
                }
                
                bc.position(0);
                ByteBuffer buffer = readBitBuffer[idx];
                buffer.clear();
                while (bc.read(buffer)==0){}//only need 1
               // bc.close(); //HACK TEST IF ITS NOT 18 OR 19 WE SHOULD STILL CLOSE.
                buffer.flip();
                return buffer.get()&0x1;
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
            

    }

}
