package com.ociweb.device.grove.device.lcdrgb;

import java.io.IOException;

import com.ociweb.device.grove.Grove_LCD_RGB;
import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public abstract class LCDRGBBacklightAbstractStage extends PronghornStage {

    private final Pipe<I2CCommandSchema> output;
    private boolean isInit = false;
    protected DataOutputBlobWriter<I2CCommandSchema> writer;
    
    protected LCDRGBBacklightAbstractStage(GraphManager graphManager, Pipe<LCDRGBBacklightSchema> input, Pipe<I2CCommandSchema> output) {
        super(graphManager, input, output);
        this.output = output;
    }
    
    protected LCDRGBBacklightAbstractStage(GraphManager graphManager, Pipe<I2CCommandSchema> output) {
        super(graphManager, NONE, output);
        this.output = output;
    }

    @Override
    public void startup() {
        writer = new DataOutputBlobWriter<>(output);
    }
    
    
    private int pendingInitCommands() {
        return isInit ? 0 : 3;
    }

    protected final boolean requestBacklightRGB(int red, int green, int blue) {
        
        //add room for init commands
        
        int singleCommand = Pipe.sizeOf(output, I2CCommandSchema.MSG_COMMAND_1);
        
        int totalRequired = 3 + pendingInitCommands();
        
        //room for possible init and the RGB values
        int spaceRequired = totalRequired*singleCommand;
        
        
        if (Pipe.hasRoomForWrite(output, spaceRequired)) {
            
            if (!isInit) {
                sendInitCommands();
            }
            sendSimpleCommand((byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0),Grove_LCD_RGB.REG_RED,red);
            sendSimpleCommand((byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0),Grove_LCD_RGB.REG_GREEN,green);
            sendSimpleCommand((byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0),Grove_LCD_RGB.REG_BLUE,blue);
            
            //Call once with the total
            Pipe.confirmLowLevelWrite(output, spaceRequired);
            
            return true;
        } else {
            return false;
        }
    }

    private void sendInitCommands() {
        sendSimpleCommand((byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_MODE1, 0);
        sendSimpleCommand((byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_MODE2, 0);
        sendSimpleCommand((byte)((Grove_LCD_RGB.RGB_ADDRESS<<1)|0), Grove_LCD_RGB.REG_OUTPUT, (byte)0xAA);
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
