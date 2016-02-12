package com.ociweb.device.testApps;

import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.device.impl.Grove_LCD_RGB;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class I2CCommandStage extends PronghornStage {
    
    private Pipe<I2CCommandSchema> output;
    private byte[] rawData;
    private int pos;
    
    protected I2CCommandStage(GraphManager graphManager, Pipe<I2CCommandSchema> output) {
        super(graphManager, NONE, output);
        this.output = output;
    }

    
    @Override 
    public void startup() {
        pos = 0;
        int row = 1;
        int column = 4;
        rawData = new byte[]{
                
                //we do get false negatives of ack falure so ignrore
                

                
               
           //            (byte) (0b00000110), //(byte)0xA5, (byte)0x5A, //reset cmmand
                       
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_MODE1, 0,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_MODE2, 0, 
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_OUTPUT, (byte)0xAA, 
                       
                  //     (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), (byte)0b10100010, (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_RED,    (byte)0x00,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,  (byte)0xFF,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0x00,
                    
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_RED,    (byte)0xF1,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,  (byte)0x00,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0x00,
                
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_RED,    (byte)0x00,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,  (byte)0x00,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0xF0,
                       
//                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0x80, (byte)0x01, //clear
//                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0x80, (byte)(0x08 | 0x04), //# display on, no cursor
//                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0x80, (byte)(0x28), //two lines
                       
                       
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x3F,
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x0F,
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x01,
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x07,
                       
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)Grove_LCD_RGB.LCD_CLEARDISPLAY,
                       
                       
                       //NOTE: this works!!
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)(row == 0 ? (column | Grove_LCD_RGB.LCD_SETDDRAMADDR) :
                           (column | Grove_LCD_RGB.LCD_SETDDRAMADDR | Grove_LCD_RGB.LCD_SETCGRAMADDR)),
                       
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR,
                                                                 (byte)'h',
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR,
                                                                 (byte)'i',                                         
                                                                 (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR,
                                                                 (byte)('A'+(System.currentTimeMillis()&0xF)), 
                       
          //              (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)Grove_LCD_RGB.LCD_DISPLAYOFF,
                                                                 
                       
                       
                     //  (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), 
                    //           (byte)0, 
                    //           (byte)(Grove_LCD_RGB.LCD_FUNCTIONSET | Grove_LCD_RGB.LCD_2LINE),
                               //0x30  0x0f
                                                
                 //      (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), 
                 //              (byte)0, 
                //               (byte)(0x0F /*Grove_LCD_RGB.LCD_DISPLAYCONTROL |    (Grove_LCD_RGB.LCD_DISPLAYOFF | Grove_LCD_RGB.LCD_CURSOROFF )*/),
                     
          
                      
               //        (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), 
                 //              (byte)0, 
                   //            (byte)(Grove_LCD_RGB.LCD_CLEARDISPLAY),        
                               ///  1
                     
                     
                     //  0
                       
                      // (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)(Grove_LCD_RGB.LCD_FUNCTIONSET | Grove_LCD_RGB.LCD_4BITMODE | Grove_LCD_RGB.LCD_2LINE | Grove_LCD_RGB.LCD_5x8DOTS),
                      // (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte) (Grove_LCD_RGB.LCD_DISPLAYCONTROL | Grove_LCD_RGB.LCD_DISPLAYON | Grove_LCD_RGB.LCD_CURSOROFF | Grove_LCD_RGB.LCD_BLINKOFF),
                                           
                      
                       
                       
////                   sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_DISPLAYCONTROL | 
////                   (Grove_LCD_RGB.LCD_DISPLAYON /*| Grove_LCD_RGB.LCD_CURSOROFF | Grove_LCD_RGB.LCD_BLINKOFF)) */ )));     
//           delay();
//           sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_DISPLAYCONTROL |    (Grove_LCD_RGB.LCD_DISPLAYOFF | Grove_LCD_RGB.LCD_CURSOROFF )));     
//           delay();
//           sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)Grove_LCD_RGB.LCD_CLEARDISPLAY);
                       
//                    
//                     sendMsg(d, Grove_LCD_RGB.LCD_ADDRESS, (byte)0, (byte)(Grove_LCD_RGB.LCD_FUNCTIONSET | line));
                       
                       
                       
                //       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0),      
                       //Grove_LCD_RGB.REG_MODE1, 0,
                       
                 //      (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_MODE1, 0,
                 //      (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_MODE2, 0, 
                       
                      // (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), 7,  (byte)0XFF, 
                      // (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,    (byte)0XFF, 
                      // (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0XFF   
                 };
        
        
//        //TODO: need a bus can function to find those addresses which return
//        int x = 0x9F;
//        rawData = new byte[x];
//        while (--x>=0) {
//            rawData[x]=(byte)(x);
//        }
//        pos = 0xF;
////        
        
        //NOTE: Grove LCD is using SMBus I2C so
        //      Minimum clock is 10K and maximum is 100K
        //      timeout of 35ms
        
        
    }
    
    
    @Override
    public void run() {  // 0x06
                
        if (pos<rawData.length) {
           
            if ( Pipe.contentRemaining(output)==0 ) { 
            
                int group = 3;
                if (pos>=35) {
                  //  group = 1;
             
               //     sendDelay(0, 1_600_000);//should be 1.53 ms. but never above the 35 timeout
                   
                }
                
                
                pos = sendCommand(rawData,pos, group); //we just have 3 byte commands for now.
            }
        }
        
        if (pos>=rawData.length) {
            
            PipeWriter.publishEOF(output);
            
            requestShutdown();
        }
        
        
        
    }


    private int sendCommand(byte[] source, int offset, int length) {
        

        if (PipeWriter.tryWriteFragment(output, I2CCommandSchema.MSG_COMMAND_1)) {
            
            PipeWriter.writeBytes(output, I2CCommandSchema.MSG_COMMAND_1_FIELD_BYTEARRAY_2, source, offset, length, 0x7FFF_FFFF);
            PipeWriter.publishWrites(output);
            return offset+=length;
        }        
        return offset;
    }
    
    private boolean sendDelay(int offset, int delay) {
        

        if (PipeWriter.tryWriteFragment(output, I2CCommandSchema.MSG_SETDELAY_10)) {
            
            PipeWriter.writeInt(output, I2CCommandSchema.MSG_SETDELAY_10_FIELD_BEFOREBYTEOFFSET_12, offset);
            PipeWriter.writeInt(output, I2CCommandSchema.MSG_SETDELAY_10_FIELD_DELAYINNANOSECONDS_13, delay);
                    
            PipeWriter.publishWrites(output);
            return true;
        }    
        return false;
    }
    
    
    

}
