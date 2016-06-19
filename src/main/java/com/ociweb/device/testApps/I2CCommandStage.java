package com.ociweb.device.testApps;

import com.ociweb.iot.grove.device.Grove_LCD_RGB;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class I2CCommandStage extends PronghornStage {
    
    private Pipe<I2CCommandSchema> output;
    private byte[] rawData;
    private int pos;
    
    public I2CCommandStage(GraphManager graphManager, Pipe<I2CCommandSchema> output) {
        super(graphManager, NONE, output);
        this.output = output;
    }

    
    @Override 
    public void startup() {
        pos = 0;

        int rowA = 0;
        int columnA = 17;

        int rowB = 1;
        int columnB = 17;

        rawData = new byte[]{
                
                //we do get false negatives of ack falure so ignrore
                

                
               
           //            (byte) (0b00000110), //(byte)0xA5, (byte)0x5A, //reset cmmand
                       
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_MODE1, 0,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_MODE2, 0,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_OUTPUT, (byte)0xAA,
                       
              //         (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), (byte)0b10100010, (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_RED,    (byte)0x00,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,  (byte)0xFF,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0x00,
//
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_RED,    (byte)0xF1,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,  (byte)0xF1,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0xF1,


                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_RED,    (byte)0x33,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,  (byte)0x33,
                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0x33,

//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_RED,    (byte)0x00,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,  (byte)0xFF,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0x00,
//
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_RED,    (byte)0xF1,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,  (byte)0x00,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0x00,
//
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_RED,    (byte)0xF1,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,  (byte)0xF1,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0xF1,
//
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_RED,    (byte)0x00,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_GREEN,  (byte)0x00,
//                       (byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_BLUE,   (byte)0xF0,

                       
//                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0x80, (byte)0x01, //clear
//                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0x80, (byte)(0x08 | 0x04), //# display on, no cursor
//                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0x80, (byte)(0x28), //two lines
                       
//                       
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x3F,
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x0F,
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x01,
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x07,
                       
                      // (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)Grove_LCD_RGB.LCD_CLEARDISPLAY,

                       
                       //NOTE: this works!!
                       (byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0),
                       (byte)0x80, (byte)Grove_LCD_RGB.LCD_CLEARDISPLAY,
                       
                       (byte)0x80, (byte)(rowA == 0 ? (columnA | Grove_LCD_RGB.LCD_SETDDRAMADDR) :
                           (columnA | Grove_LCD_RGB.LCD_SETDDRAMADDR | Grove_LCD_RGB.LCD_SETCGRAMADDR)),

                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'E',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'m',

                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'b',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'e',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'d',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'d',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'e',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'d',
                       
                       
                       (byte)0x80, (byte)(rowB == 0 ? (columnB | Grove_LCD_RGB.LCD_SETDDRAMADDR) :
                           (columnB | Grove_LCD_RGB.LCD_SETDDRAMADDR | Grove_LCD_RGB.LCD_SETCGRAMADDR)),

                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'Z',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'u',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'l',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'u',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)' ',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'J',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'a',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80, (byte)'v',
                       (byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x0, (byte)'a',


        };


    }
    
    
    @Override
    public void run() {  // 0x06
                
        if (pos<rawData.length) {
           
            if ( Pipe.contentRemaining(output)==0 ) { 
            
                int group = 3;

                if (pos>=rawData.length-41) {
                   group = 41;
             
                //    sendDelay(0, 1_600_000);//should be 1.53 ms. but never above the 35 timeout
                   
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
