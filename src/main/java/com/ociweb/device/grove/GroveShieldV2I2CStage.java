package com.ociweb.device.grove;

import java.nio.ByteBuffer;

import com.ociweb.device.impl.EdisonPinManager;
import com.ociweb.device.impl.Grove_LCD_RGB;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GroveShieldV2I2CStage extends PronghornStage {

    public GroveShieldV2I2CStage(GraphManager gm, Pipe<GroveRequestSchema> pipe) {
        super(gm, NONE, pipe);
        
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 10*1000*1000, this);
        GraphManager.addNota(gm, GraphManager.PRODUCER, GraphManager.PRODUCER, this);
        
    };
    
    
    @Override
    public void startup() {
        
        
    }
    
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void shutdown() {
        
        
    }

    
    //Not needed here this should be moved to the publish stage not sensor stage
//  i = config.digitalOutputs.length;
//  while (--i>=0) {
//      configDigitalOutput(config.digitalOutputs[i]); //take from pipe and write, get type and field from pipe
//      
//      script[reverseBits(sliceCount++)] = ((MASK_DO_PORT&config.digitalOutputs[i])<<SHIFT_DO_PORT) |
//                                          ((MASK_DO_JOB&DO_DATA_WRITE)<<SHIFT_DO_JOB );
//  }
  
  //Not needed here this should be moved to the dedicated i2c stage
//  if (config.configI2C) {
//      configI2C();                        //I2C process 2x
//      int port = 0;
//      script[reverseBits(sliceCount++)] = ((MASK_DO_PORT&port)<<SHIFT_DO_PORT) |
//                                          ((MASK_DO_JOB&DO_I2C_READ)<<SHIFT_DO_JOB );
//      script[reverseBits(sliceCount++)] = ((MASK_DO_PORT&port)<<SHIFT_DO_PORT) |
//                                          ((MASK_DO_JOB&DO_I2C_WRITE)<<SHIFT_DO_JOB ); //TODO: THIS MAY BE WRONG AND NOT NEEDED.
//  }
    
    
    

//  private void configUART(int dPort) {
//      if (dPort<0 || 3==dPort || dPort>4) {
//          //only 0, 1, 2 and 4
//          throw new UnsupportedOperationException("UART only available on 0, 1, 2 or 4");
//      }
//        
//      //TODO: UART, there are very few Grove sensors using this. To be implemented later  
//      throw new UnsupportedOperationException();
//      
//  }   
    
  
    
//    private static void pause() {
//      //Human time for monitoring
////        try {
////            Thread.sleep(500);
////        } catch (InterruptedException e) {
////            // TODO Auto-generated catch block
////            e.printStackTrace();
////        }
//        
//      try {
//          Thread.sleep(0,10000); //100K  1,000,000,000/100,000 = 10,000;
//      } catch (InterruptedException e) {
//         Thread.currentThread().interrupt();
//      }
//      
//        //park nanos is NOT working. (DO NOT USE until tested again later)
//    //   LockSupport.parkNanos(10000000);//no faster than  100hz
//    //   LockSupport.parkNanos(1000000);//no faster than  1K
//    //   LockSupport.parkNanos(100000);//no faster than  10K        
//    //   LockSupport.parkNanos( 10000);//no faster than 100K
//    //   LockSupport.parkNanos(  2500);//no faster than 400K
//        
//    }
//    
//    private static final int I2C_DATA = 18;
//    private static final int I2C_CLOCK = 19;
//    private static final ByteBuffer I2C_HIGH = ByteBuffer.wrap(VALUE_HIGH);
//    private static final ByteBuffer I2C_LOW  = ByteBuffer.wrap(VALUE_LOW);
//    //TODO: extract this logic as its own stage just for I2C IO.
//
//    public static boolean start_I2C(EdisonPinManager d) {
//      //  GroveShieldV2EdisonStage.configI2CIn();
//        writeValue(I2C_CLOCK, I2C_HIGH, d);
//        while (0==readBit(I2C_CLOCK,d)) {
//            //client may hold down clock must wait until it goes back up.
//            //This is a spinning block dependent upon the other end of i2c
//        }             
//        if (0==readBit(I2C_DATA,d)) {
//            System.out.println("failure, unable to be master, data line should be high");
//            return false;
//        }
//
//        writeValue(I2C_DATA, I2C_LOW, d); //lower data while clock is high
//        pause();
//
//        if (0==readBit(I2C_CLOCK,d)) {
//            throw new RuntimeException("expected clock to be high");
//        }
//        writeValue(I2C_CLOCK, I2C_LOW, d);
//        pause();
//        
//        if (1==readBit(I2C_DATA,d)) {
//            System.out.println("failure, unable to be master SDA");
//            return false;
//        }
//        if (1==readBit(I2C_CLOCK,d)) {
//            System.out.println("failure, unable to be master SCL");
//            return false;
//        }
//        
//        //System.out.println("I am the master");
//        return true;
//    }
//    
//    public static void stop_I2C(EdisonPinManager d) {
//        writeValue(I2C_CLOCK, I2C_HIGH, d);
//        while (0==readBit(I2C_CLOCK,d)) {
//            //This is a spinning block dependent upon the other end of i2c
//        }
//        pause();
//        writeValue(I2C_DATA, I2C_HIGH, d);
//        pause();
//    }
//    
//    private static void writeI2CBit(int bit, EdisonPinManager d) {
//
//        if (0==bit) {
//            writeValue(I2C_DATA, I2C_LOW, d);
//        } else {
//            writeValue(I2C_DATA, I2C_HIGH, d);
//        }        
//        writeValue(I2C_CLOCK, I2C_HIGH, d);
//        while (0==readBit(I2C_CLOCK,d)) {
//            //This is a spinning block dependent upon the other end of i2c
//        }        
//        pause(); //provide time to read this bit
//
//        if (0!=bit && readBit(I2C_DATA, d)==0 ) {
//            throw new RuntimeException("Unable to confirm data set high");           
//        }
//
//        writeValue(I2C_CLOCK, I2C_LOW, d);
//        pause();
//                
//    }
//    
//    private static int readI2CBit(EdisonPinManager d) {
//        
//        
//        //wait for clock to be high
//        long limit = System.currentTimeMillis()+300;
//        while (0==readBit(I2C_CLOCK,d) ) {
//            if (System.currentTimeMillis()>limit) {
//                System.err.println("timeout from device");
//                return 1;//nothing
//            }
//            
//          //This is a spinning block dependent upon the other end of i2c
//        }
//        //clock is now high
//        int result = readBit(I2C_DATA,d);
//        pause();
//        //done with read so lower clock
//        writeValue(I2C_CLOCK, I2C_LOW, d);
//        pause();
//        return result;
//    }
//    
//    private static int sendI2CByte(EdisonPinManager d,  int value) {
//        int i = 8;
//        while (--i>=0) {    
//          //  System.out.println("write bit "+i);
//            writeI2CBit(1 & (value>>i), d);            
//        }
//
//       //new test clock is low
//        writeValue(I2C_DATA, I2C_HIGH, d); //slave must set to zero on trailing edge for failure        
//        writeValue(I2C_CLOCK, I2C_HIGH, d);
//        while (0==readBit(I2C_CLOCK,d)) {
//            //This is a spinning block dependent upon the other end of i2c
//        }
//        GroveShieldV2EdisonSensorStage.configI2CDataIn();//needed so we can read the ack.
//        pause();
//        
//        writeValue(I2C_CLOCK, I2C_LOW, d);        
//      
//        int result = readBit(I2C_DATA,d);
//        GroveShieldV2EdisonSensorStage.configI2CDataOut();
//
//        pause();
//        writeValue(I2C_DATA, I2C_HIGH, d);
//        
//        pause();
//        
//        return result;
//    }
//    
//    public static boolean readMsg(EdisonPinManager d, int addr) {
//        //request data.
//        if (!start_I2C(d) ) {
//            //unable to master the bus 
//            System.out.println("unable to master the bus");
//            return false;
//        };
//        int value =  (addr<<1) | 0; //zero for write
//        int nack = sendI2CByte(d, value); 
//        System.out.println("ADDR NACK:"+nack);
//
//        int register = 1;
//        nack = sendI2CByte(d, register);
//        System.out.println("register NACK:"+nack);
//        
//        if (!start_I2C(d) ) {
//            //unable to master the bus 
//            System.out.println("unable to master the bus");
//            return false;
//        };  
//        
//        value =  (addr<<1 ) | 1; //one for write
//        nack = sendI2CByte(d, value);        
//        System.out.println("ADDR NACK:"+nack);
//        
//        //read data
//        int dat  = 0;
//        int i = 32;
//        while (--i>=0) {
//            dat = ((dat<<1)|readI2CBit(d));            
//        }
//        System.out.println("read:"+dat);
//        
//        stop_I2C(d);        
//        
//        return true;
//    }
//    
//    public static void sendMsg(EdisonPinManager d, int addr, byte ... msg) {
//        if (!start_I2C(d) ) {
//            //unable to master the bus 
//            System.out.println("unable to master the bus");
//            
//        } else {
//        
//            sendAddr(d, addr, 0 /*1 is read*/);
//            sendBytes(d, msg);
//        
//            stop_I2C(d);
//        }
//    }
//
//    private static void sendAddr(EdisonPinManager d, int addr, int isRead) {
//        int value =  (addr<<1 ) | isRead; //zero for write
//        int nack = sendI2CByte(d, value);        
//       // System.out.println("ignored ADDR NACK:"+nack+"  from  "+Integer.toHexString(addr));
//    }
//
//    private static void sendBytes(EdisonPinManager d, byte... msg) {
//        int nack;
//        int i = 0;
//        while (i<msg.length) {
//            nack = sendI2CByte(d, msg[i]);
//            
//           // System.out.println("ignored NACK:"+nack+"  sent "+msg[i]);
//            
//            i++;
//        }
//    }
//    
//    
//    private static void delay() {
//        try {
//            Thread.sleep(1000);//500);//  //1500);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//    
//    private static void delay(int microSecond) {
//        try {
//            int ns =  microSecond*1000;
//            
//            
//            Thread.sleep((ns/1000000), (ns%1000000) );//500);//  //1500);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//    
//    
//    
//    public static void writeI2C(byte[] data, EdisonPinManager d) {
//        //this is always on 18/19
////        System.err.println("write to I2C");
////        ByteBuffer src = ByteBuffer.allocate(80);
////        
////        //all Edison i2c communication in on device 6
////       // Path i2cDevice = Paths.get("/sys/class/i2c-dev/i2c-6"); //mapped to 18(SDA)/19(SCL)
////        
//        
//        
////        ///beep works
//        //TODO:   build stage to play multiple notes out buzzer
//        //TODO:   build schema patttern after midd format
////        int j = 100;
////        while (--j>=0) {
////            writeValue(4, I2C_HIGH, d);
////            pause();
////            writeValue(4, I2C_LOW, d);
////            pause();
////        }
//        
//        
//        delay();
//        delay();
//        
////        //setup starting state with both high.
//        GroveShieldV2EdisonSensorStage.configI2COut(); //we are now the master of the bus, i2cdetect will no longer work
//        writeValue(I2C_DATA, I2C_HIGH, d);
//        writeValue(I2C_CLOCK, I2C_HIGH, d);
//        pause();
//
//        delay();
//        delay();
//        
//        boolean testGuage = false;
//        if (testGuage) {        
//            
//            //When data line goes high the clock is dropped
//            testCodeForLevelMonitoring(d); //NOTE: returns both to low
//            
//            writeValue(I2C_DATA, I2C_HIGH, d);
//            writeValue(I2C_CLOCK, I2C_HIGH, d);
//            pause();
//        }
//                
//        //Start case must have both high
//        if (0==readBit(I2C_CLOCK,d) ) {
//            throw new RuntimeException("expected clock to be high for start");
//        }        
//        if (0==readBit(I2C_DATA,d) ) {
//            throw new RuntimeException("expected data to be high for start");
//        }
//        
//      //Init so we can adjust each color.
//      sendMsg(d, Grove_LCD_RGB.RGB_ADDRESS, (byte)0, (byte)0);     
//      sendMsg(d, Grove_LCD_RGB.RGB_ADDRESS, (byte)1, (byte)0);
//      sendMsg(d, Grove_LCD_RGB.RGB_ADDRESS, (byte)Grove_LCD_RGB.REG_OUTPUT, (byte)0xAA);
//        
//        setBacklightColor(d, (byte)0xFF, (byte)0x00, (byte)0x00);      
//        System.out.println("red");        
//
//        //        try {
////            Thread.sleep(1000);
////        } catch (InterruptedException e) {
////        }
////        
////        
////        setBacklightColor(d, (byte)0x00, (byte)0xFF, (byte)0x00);      
////        System.out.println("green");        
////        try {
////            Thread.sleep(1000);
////        } catch (InterruptedException e) {
////        }
////        
////        
////        setBacklightColor(d, (byte)0x00, (byte)0x0, (byte)0xFF);
////        System.out.println("blue");
//      
//        
//
//        
//      //      top bit is always 1 for following commands except on last call #7
//      //NOTE: we only have 2 regesters 0 and 1 in the rs bit                 #6
//      
////      start_I2C(d);
////      sendAddr(d, Grove_LCD_RGB.LCD_ADDRESS, 0 /*1 is read*/);
////      delay();
////      delay();
////      sendBytes(d, (byte)0x80, (byte)0b00111100);
////     // delay(40);
////      delay();
////      delay();
////      sendBytes(d,  (byte)0x80,(byte)0b00001101); //last blit is blink on
////      //delay(40);
////      delay();
////      delay();
////      sendBytes(d,  (byte)0x80,(byte)0b00000001); //clear display 
////    //  delay(1531);
////      delay();
////      delay();
////      sendBytes(d,  (byte)0x80,(byte)0b00000110); //data entry inc data shift off.
////      delay();
////      delay();
////      //stop_I2C(d);
//      
//
//        
//        
//   //   int displayFunction = Grove_LCD_RGB.LCD_2LINE | Grove_LCD_RGB.LCD_FUNCTIONSET;
//
//      //send multiple times to ensure it sticks.
//   //   sendAddr(d, Grove_LCD_RGB.LCD_ADDRESS, 0);  
//       int line = Grove_LCD_RGB.LCD_2LINE;//LCD_5x10DOTS;
//      sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_FUNCTIONSET | line));
//      delay(50000);// micro
//      sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_FUNCTIONSET | line));
//      delay(4500);// miro
//      sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_FUNCTIONSET | line));
//      delay(150);// micro
//      sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_FUNCTIONSET | line));
//      sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_FUNCTIONSET | line));
////      
////      delay();
////      sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_DISPLAYCONTROL | 
////              (Grove_LCD_RGB.LCD_DISPLAYON /*| Grove_LCD_RGB.LCD_CURSOROFF | Grove_LCD_RGB.LCD_BLINKOFF)) */ )));     
//      delay();
//      sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_DISPLAYCONTROL |    (Grove_LCD_RGB.LCD_DISPLAYOFF | Grove_LCD_RGB.LCD_CURSOROFF )));     
//      delay();
//      sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)Grove_LCD_RGB.LCD_CLEARDISPLAY);
//      
//      
//////      
////      //standard for romance languages
////      sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_ENTRYMODESET | ( Grove_LCD_RGB.LCD_ENTRYLEFT | Grove_LCD_RGB.LCD_ENTRYSHIFTDECREMENT) ));
////      delay();
////
////      start_I2C(d);
////      sendAddr(d, Grove_LCD_RGB.LCD_ADDRESS, 0);  
////      sendBytes(d, (byte)0x40); 
////      int i = 50;
////      while (--i>30) {
////          delay();
////          sendBytes(d, (byte)i);
////          System.out.println(i);
////      }
////
////      stop_I2C(d);
//
//          
//        setBacklightColor(d, (byte)0x00, (byte)0x00, (byte)0xFF);
//    
//            
//        System.err.println("finished I2C");
//    }
//
//    public static void setBacklightColor(EdisonPinManager d, byte r, byte g, byte b) {
//
//
//          sendMsg(d, Grove_LCD_RGB.RGB_ADDRESS, (byte)Grove_LCD_RGB.REG_RED, (byte)0);
//          sendMsg(d, Grove_LCD_RGB.RGB_ADDRESS, (byte)Grove_LCD_RGB.REG_GREEN, (byte)0);
//          sendMsg(d, Grove_LCD_RGB.RGB_ADDRESS, (byte)Grove_LCD_RGB.REG_BLUE, (byte)0);
//          sendMsg(d, Grove_LCD_RGB.RGB_ADDRESS, (byte)Grove_LCD_RGB.REG_RED, r);
//          delay();
//          sendMsg(d, Grove_LCD_RGB.RGB_ADDRESS, (byte)Grove_LCD_RGB.REG_GREEN, g);
//          sendMsg(d, Grove_LCD_RGB.RGB_ADDRESS, (byte)Grove_LCD_RGB.REG_BLUE, b);
//
//    }
//
//    private static void testCodeForLevelMonitoring(EdisonPinManager d) {
//        //start with both high then both low.
//        int j = 2;
//        while (--j>=0) {
//        
//            if (j==1) {
//                writeValue(I2C_DATA, I2C_HIGH, d);
//                writeValue(I2C_CLOCK, I2C_HIGH, d);                
//            } else {
//                writeValue(I2C_DATA, I2C_LOW, d);
//                writeValue(I2C_CLOCK, I2C_LOW, d);
//            }
//                                 
//            
//            int i = 20;
//            while (--i>=0) {
//                if (0==(1&i)) {
//                    writeValue(I2C_DATA, I2C_HIGH, d);
//                } else {
//                    writeValue(I2C_DATA, I2C_LOW, d);
//                }
//                
//                try {
//                    Thread.sleep(80);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//            
//            if (j==1) {
//                writeValue(I2C_DATA, I2C_HIGH, d);
//                writeValue(I2C_CLOCK, I2C_HIGH, d);                
//            } else {
//                writeValue(I2C_DATA, I2C_LOW, d);
//                writeValue(I2C_CLOCK, I2C_LOW, d);
//            }
//            
//            i = 20;
//            while (--i>=0) {
//                if (0==(1&i)) {
//                    writeValue(I2C_CLOCK, I2C_HIGH, d);
//                } else {
//                    writeValue(I2C_CLOCK, I2C_LOW, d);
//                }
//                try {
//                    Thread.sleep(80);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }            
//       
//          
//        }
//    }
  
    
}
