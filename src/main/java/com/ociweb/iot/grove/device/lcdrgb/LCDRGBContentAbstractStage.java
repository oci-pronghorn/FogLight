package com.ociweb.iot.grove.device.lcdrgb;

import java.io.IOException;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public abstract class LCDRGBContentAbstractStage extends PronghornStage {

    private final Pipe<I2CCommandSchema> output;
    private boolean isInit = false;
    protected DataOutputBlobWriter<I2CCommandSchema> writer;
    
    protected LCDRGBContentAbstractStage(GraphManager graphManager, Pipe<LCDRGBContentSchema> input, Pipe<I2CCommandSchema> output) {
        super(graphManager, input, output);
        this.output = output;
    }
    
    protected LCDRGBContentAbstractStage(GraphManager graphManager, Pipe<I2CCommandSchema> output) {
        super(graphManager, NONE, output);
        this.output = output;
    }

    @Override
    public void startup() {
        writer = new DataOutputBlobWriter<>(output);
    }
        

    protected void requestText(DataInputBlobReader<?> reader) {
        
        Pipe.addMsgIdx(output, I2CCommandSchema.MSG_COMMAND_1);
        writer.openField();
        
        if (!isInit) {
            insertInitCommands();
        }
               
        try {
            positionedTextHeader(0,0);                       
            
            int i = 0;
            for(; i < reader.available()-1 ; i++) {
                writer.write((byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80);
                writer.writeByte(reader.readByte());
            }
            writer.write((byte)Grove_LCD_RGB.LCD_SETCGRAMADDR);
            writer.writeByte(reader.readByte());
                        
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        finishLowLevelWrite(); 
        
    }

    protected final boolean requestText(CharSequence text) {
        
        int commandSize = Pipe.sizeOf(output, I2CCommandSchema.MSG_COMMAND_1);        
        int commandsCount = 1;
        
        int totalSize = commandSize*commandsCount;
                
        if (Pipe.hasRoomForWrite(output, totalSize)) {

            
            if (!isInit) {
                insertInitCommands(); //4 
            }
            positionedTextHeader(0,16); //2 commands
            writeText(text); //1 command
            
            return true;
            
        } else {
            return false;
        }
        
    }

    private void writeText(CharSequence text) {
                
        try {
            Pipe.addMsgIdx(output, I2CCommandSchema.MSG_COMMAND_1);
            writer.openField();

            int i = 0;
            for(; i < text.length()-1 ; i++) {
                writer.write((byte)Grove_LCD_RGB.LCD_SETCGRAMADDR | (byte)0x80);
                writer.write((byte)text.charAt(i));
            }
            writer.write((byte)Grove_LCD_RGB.LCD_SETCGRAMADDR);
            writer.write((byte)text.charAt(i));           
            
            writer.closeLowLevelField();
            Pipe.publishWrites(output);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
        
    }

    private void finishLowLevelWrite() {
        writer.closeLowLevelField();
        Pipe.confirmLowLevelWrite(output, Pipe.sizeOf(output, I2CCommandSchema.MSG_COMMAND_1) );
        Pipe.publishWrites(output);
    }

    private void positionedTextHeader(int row, int column) {        
        
        sendSimpleCommand((byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)Grove_LCD_RGB.LCD_CLEARDISPLAY);        
        sendSimpleCommand((Grove_LCD_RGB.LCD_ADDRESS<<1)|0, (byte)0x80, (byte)(row == 0 ? (column | Grove_LCD_RGB.LCD_SETDDRAMADDR) : (column | Grove_LCD_RGB.LCD_SETDDRAMADDR | Grove_LCD_RGB.LCD_SETCGRAMADDR))  );
                
    }

    private void insertInitCommands() {
        
        sendSimpleCommand((byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x3F);
        sendSimpleCommand((byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x0F);
        sendSimpleCommand((byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x01);
        sendSimpleCommand((byte)((Grove_LCD_RGB.LCD_ADDRESS<<1)|0), (byte)0, (byte)0x07);

        isInit = true;
    }
        
    private void sendSimpleCommand(int addrs, int reg, int value) {
        try {
            Pipe.addMsgIdx(output, I2CCommandSchema.MSG_COMMAND_1);
            writer.openField();
            writer.write(addrs);
            writer.write(reg);
            writer.write(value);
            writer.closeLowLevelField();
            Pipe.publishWrites(output);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

}
