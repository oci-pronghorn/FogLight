package com.ociweb.iot.hardware.impl.edison;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EdisonPinManager {

    private static final Logger logger = LoggerFactory.getLogger(EdisonPinManager.class);
    
    public final Path[] primaryDevicePath; 
    public final Path[] gpioDirection;
    public final SeekableByteChannel[] gpioDirectionChannel;   
    
    
    public final Path[] gpioDebugCurrentPinMux;
    public final Path[] gpioValue;
    public final SeekableByteChannel[] gpioChannel;   
    
    public final short[]    gpioPinInt;
    public final String[] exportIdText;
    public FileSystemProvider provider;

    
    public static final Path[] PATH_A = new Path[] {
             FileSystems.getDefault().getPath("/sys/bus/iio/devices/iio:device1", "in_voltage0_raw"),
             FileSystems.getDefault().getPath("/sys/bus/iio/devices/iio:device1", "in_voltage1_raw"),
             FileSystems.getDefault().getPath("/sys/bus/iio/devices/iio:device1", "in_voltage2_raw"),
             FileSystems.getDefault().getPath("/sys/bus/iio/devices/iio:device1", "in_voltage3_raw"),
             FileSystems.getDefault().getPath("/sys/bus/iio/devices/iio:device1", "in_voltage4_raw"), //for I2C
             FileSystems.getDefault().getPath("/sys/bus/iio/devices/iio:device1", "in_voltage5_raw"), //for I2C
    };

    public static final Path[] PATH_PWM        = new Path[EdisonConstants.PWM_PINS.length];
    public static final Path[] PATH_PWM_PERIOD = new Path[EdisonConstants.PWM_PINS.length];
    public static final Path[] PATH_PWM_DUTY   = new Path[EdisonConstants.PWM_PINS.length];
    public static final Path[] PATH_PWM_ENABLE = new Path[EdisonConstants.PWM_PINS.length];
    
    
    
  //  public static final Path[] PATH_PWM_PERIOD = new Path[EdisonConstants.PWM_PINS.length];
    
   // Path p = FileSystems.getDefault().getPath("/sys/class/pwm/pwmchip0/pwm1", "period");
    // Path e = FileSystems.getDefault().getPath("/sys/class/pwm/pwmchip0/pwm1", "enable");

    // Path d = FileSystems.getDefault().getPath("/sys/class/pwm/pwmchip0/pwm1", "duty_cycle");
    
    static {
       //build array of all the paths based on the pin definitions in the constants class. 
       //This ensures these two match each other.
       int j = EdisonConstants.PWM_PINS.length;
       while (--j >= 0) {
           int v = EdisonConstants.PWM_PINS[j];
           if (v>=0) {
               String root = "/sys/class/pwm/pwmchip0/pwm"+v;
               PATH_PWM[j] = FileSystems.getDefault().getPath(root);
               
               PATH_PWM_PERIOD[j] = FileSystems.getDefault().getPath(root+"/period");
               PATH_PWM_DUTY[j] = FileSystems.getDefault().getPath(root+"/duty_cycle");
               PATH_PWM_ENABLE[j] = FileSystems.getDefault().getPath(root+"/enable");
               
           }
       }
        //  Path pmwPath = Paths.get("/sys/class/pwm/pwmchip0/pwm"+exportIdText[i]);//TODO: hack, make static 
    }
    
    
    public final SeekableByteChannel[] pathAChannel = new SeekableByteChannel[6];   
    
    private static final Path PATH_PWM_EXPORT   = Paths.get("/sys/class/pwm/pwmchip0/export");
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
    
    public static final ByteBuffer I2C_LOW;
    public static final ByteBuffer I2C_HIGH;
    public static final ByteBuffer I2C_OUT;
    public static final ByteBuffer I2C_IN;
    public static final ByteBuffer I2C_DIRECTION_LOW;
    public static final ByteBuffer I2C_DIRECTION_HIGH;
    public static final ByteBuffer[] BIT_BYTES;

    
    public static final int I2C_CLOCK = 19;
    public static final int I2C_DATA = 18;
        
    private static final Set<OpenOption> readOptions = new HashSet<OpenOption>();
    private static final Set<OpenOption> i2cOptions = new HashSet<OpenOption>();

    private static ByteBuffer[] readIntBuffer;
    private static ByteBuffer[] readBitBuffer;
    private static ByteBuffer[] writePWMBuffer;
    
    static {
        
        I2C_LOW = ByteBuffer.allocateDirect(VALUE_LOW.length);
        I2C_LOW.put(VALUE_LOW);
        I2C_LOW.clear();
        
        I2C_HIGH = ByteBuffer.allocateDirect(VALUE_HIGH.length);
        I2C_HIGH.put(VALUE_HIGH);
        I2C_HIGH.clear();
        
        I2C_OUT = ByteBuffer.allocateDirect(OUT.length);
        I2C_OUT.put(OUT);
        I2C_OUT.clear();
        
        I2C_IN = ByteBuffer.allocateDirect(IN.length);
        I2C_IN.put(IN);
        I2C_IN.clear();
        
        I2C_DIRECTION_LOW = ByteBuffer.allocateDirect(DIRECTION_LOW.length);
        I2C_DIRECTION_LOW.put(DIRECTION_LOW);
        I2C_DIRECTION_LOW.clear();
        
        I2C_DIRECTION_HIGH = ByteBuffer.allocateDirect(DRECTION_HIGH.length);
        I2C_DIRECTION_HIGH.put(DRECTION_HIGH);
        I2C_DIRECTION_HIGH.clear();
        
        BIT_BYTES = new ByteBuffer[]{I2C_LOW, I2C_HIGH};
        
        i2cOptions.add(StandardOpenOption.READ);
        i2cOptions.add(StandardOpenOption.WRITE);
        i2cOptions.add(StandardOpenOption.SYNC);
        
        readOptions.add(StandardOpenOption.READ);        

        int p = PATH_PWM.length;
        writePWMBuffer = new ByteBuffer[p];
        while (--p>=0) {
            writePWMBuffer[p] = ByteBuffer.allocate(16);   
        }
        
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
        primaryDevicePath = new Path[pins.length];    
        gpioDirection = new Path[pins.length];
        gpioDirectionChannel = new SeekableByteChannel[pins.length];
        gpioValue = new Path[pins.length];
        gpioChannel = new SeekableByteChannel[pins.length];
        exportIdText = new String[pins.length];
        gpioDebugCurrentPinMux = new Path[pins.length];//NOTE only needed for mode array
        
        
        FileSystem fileSystem = FileSystems.getDefault();
        this.provider = fileSystem.provider();
        
        int i = pins.length;
        StringBuilder sb = new StringBuilder();
        sb.append("/sys/class/gpio/gpio");
        int baseLen = sb.length();
        while (--i>=0) {
            
            if (pins[i]>=0) {
                exportIdText[i] = Integer.toString(pins[i]);
                
                gpioDebugCurrentPinMux[i] = fileSystem.getPath("/sys/kernel/debug/gpio_debug/gpio"+exportIdText[i]+"/current_pinmux");

                sb.setLength(baseLen);
                sb.append(exportIdText[i]);
                primaryDevicePath[i]          = fileSystem.getPath(sb.toString());
                                
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
        writeDirection(i,I2C_DIRECTION_LOW,this);
    }

    public void setDirectionHigh(int i) {
        writeDirection(i,I2C_DIRECTION_HIGH,this);
    }
    
    
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
        writeDirection(i,I2C_IN,this);
    }

    public void setDirectionOut(int i) {
        writeDirection(i,I2C_OUT,this);
    }

    public void setValueHigh(int i) {
        writeValue(i,I2C_HIGH, this);
    }


    public void setValueLow(int i) {
        writeValue(i,I2C_LOW, this);
    }

    public void ensureDevice(int i) {
        
        if (null!=primaryDevicePath[i] && !primaryDevicePath[i].toFile().exists()) {
            try {
                Files.write(PATH_GPIO_EXPORT, exportIdText[i].getBytes());
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
        }
                
    }
    
    public void ensurePMWDevice(int i, int periodInNS) {

            try {
                if (null!=PATH_PWM[i] && !PATH_PWM[i].toFile().exists()) {
                    System.out.println("did not find "+PATH_PWM[i]);
                    System.out.println("now exporting "+exportIdText[i]);
                    Files.write(PATH_PWM_EXPORT, exportIdText[i].getBytes());
                    
                }
                
                //this is in NS
                EdisonPinManager.writePWMPeriod(i, periodInNS);
                EdisonPinManager.writePWMDuty(i, 0);
                
                Files.write(PATH_PWM_ENABLE[i], "1".getBytes());

            } catch (IOException e) {
               throw new RuntimeException(e);
            }
    }
    
    public void removeDevice(int i) {
        if (null!=primaryDevicePath[i] && !primaryDevicePath[i].toFile().exists()) {
            try {
                Files.write(PATH_GPIO_UNEXPORT, exportIdText[i].getBytes());
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
        }
    }
    
    public static void writeValue(int port, ByteBuffer data, EdisonPinManager d) {
            try {
                SeekableByteChannel channel = d.gpioChannel[port];
                if (null == channel) {
                        
                  channel = d.provider.newByteChannel(d.gpioValue[port],i2cOptions);
                  d.gpioChannel[port] = channel;
                 
                }
                
                data.clear();
                int limit = data.limit();
                do {
                    limit -= channel.write(data);
                } while (limit>0);//Caution, this is blocking.
                channel.position(0);

            
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
    

    
    
    public static void writeDirection(int port, ByteBuffer data, EdisonPinManager d) {
        if (null == d.gpioDirection[port]) {
            //nothing needs to be set.
            return;
        }
        
        try {
            SeekableByteChannel channel = d.gpioDirectionChannel[port];
            if (null == channel) {
                    
              channel = d.provider.newByteChannel(d.gpioDirection[port],i2cOptions);
              d.gpioDirectionChannel[port] = channel;
                           
           }
            data.clear();
            do {
                channel.write(data);
            } while (data.hasRemaining());//Caution, this is blocking.
            channel.position(0);

        
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

   }

   public static void digitalWrite(int connector, int value, EdisonPinManager gpiolinuxpins) {
        writeValue(connector, BIT_BYTES[value], gpiolinuxpins);
        
    }
    

    public static void writePWMPeriod(int idx, int periodInNS) {
        if (periodInNS<1_000_000) {
            logger.warn("Unable to set PWM period ns, only values 1,000,000 or larger are supported. Passed in {} ",periodInNS);
            return;// do not change
        }
        
        try {
            SeekableByteChannel chnl = EdisonGPIO.pwmPins.provider.newByteChannel( PATH_PWM_PERIOD[idx],i2cOptions);     
                       
            ByteBuffer data = writePWMBuffer[idx];
            populateWithInt(data, Math.abs(periodInNS));
            
            do {
                chnl.write(data);
            } while (data.hasRemaining());//Caution, this is blocking.
            chnl.position(0);
           
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
    }

    private static void populateWithInt(ByteBuffer data, int v) {
        assert(v>=0);
        data.clear();
        int p = data.limit();
        do {
            int a = v/10;
            int b = v%10;
            data.put(--p, (byte)('0'+b));
            v = a;
        } while (v!=0);
        data.position(p);
    }

    public static void writePWMDuty(int idx, int units) {
                
        try {
            
            SeekableByteChannel chnl = EdisonGPIO.pwmPins.provider.newByteChannel( PATH_PWM_DUTY[idx],i2cOptions); 
        
            ByteBuffer data = writePWMBuffer[idx];
            populateWithInt(data, Math.abs(units));           
            do {
                chnl.write(data);
            } while (data.hasRemaining());//Caution, this is blocking.
            chnl.position(0);
           
        } catch (IOException ex) {
            
            if (ex.getMessage().contains("Invalid argument")) {
                //not serious so, do not shut down the JVM instance. 
                logger.error("check the period and duty for connection {}, duty {} is out of bounds",idx,units);
            } else {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public static int analogRead(int idx) {

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

        
        SeekableByteChannel bc = EdisonGPIO.gpioLinuxPins.pathAChannel[idx];
        if (null == bc) {
            
            /////////////
            //R&D code not ready for use
            //attempting to memory map this driver
            ////////////
//            FileChannel fc = null;
//            try {
//                fc = FileChannel.open(PATH_A[idx], StandardOpenOption.READ, StandardOpenOption.CREATE_NEW);
//            } catch (Exception e) {
//                System.out.println("Error opening file: " + e.getMessage());
//            }
//            MappedByteBuffer mbb = null;
//            try {
//                mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, 100);
//                
//                
//            } catch (IOException e) {
//                System.out.println("Error mapping file: " + e.getMessage()+"   "+PATH_A[idx]);
//            }
            
            
            
            EdisonGPIO.gpioLinuxPins.pathAChannel[idx] = bc =EdisonGPIO.gpioLinuxPins.provider.newByteChannel(PATH_A[idx], readOptions);
        }
        
        buffer.clear();
        
    //    long start = System.nanoTime();
        do {            
            
            while (bc.read(buffer)>=0){}//read everything available
 
            //if length is 0 read this again.
        } while (buffer.position()==0);// || (buffer.get(0)<'0'));
  //      long duration = System.nanoTime()-start;
        
        buffer.flip();              
        bc.position(0);
        
        
    //    System.out.println("read duration "+duration+"ns");
        
        
        
    }
    
    
    public static int digitalRead(int idx) {
            try {        
                
                SeekableByteChannel bc = EdisonGPIO.gpioLinuxPins.gpioChannel[idx];
                if (null == bc) {
                    bc = EdisonGPIO.gpioLinuxPins.provider.newByteChannel(EdisonGPIO.gpioLinuxPins.gpioValue[idx],i2cOptions);
                    EdisonGPIO.gpioLinuxPins.gpioChannel[idx] = bc;
               }
                
                ByteBuffer buffer = readBitBuffer[idx];
                buffer.clear();
                while (bc.read(buffer)==0){}//only need 1
             
                buffer.flip();
                bc.position(0);
                return buffer.get()&0x1;
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
            

    }


}
