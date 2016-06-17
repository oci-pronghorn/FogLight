package com.ociweb.iot.grove.device.lcdrgb;

import com.ociweb.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class LCDRGBBacklightStage extends LCDRGBBacklightAbstractStage {

    private final Pipe<LCDRGBBacklightSchema> input;
    private final Pipe<I2CCommandSchema> output;
    
    public LCDRGBBacklightStage(GraphManager graphManager, Pipe<LCDRGBBacklightSchema> input, Pipe<I2CCommandSchema> output) {
        super(graphManager, input, output);
        this.input = input;
        this.output = output;
    }

    
    @Override
    public void run() {
        
        while (Pipe.hasRoomForWrite(output) &&  PipeReader.tryReadFragment(input)) {
            
            switch (PipeReader.getMsgIdx(input)) {
                case -1:
                    requestShutdown();
                break;
                case LCDRGBBacklightSchema.MSG_LCDRGBBACKLIGHT_200:
                    
                    requestBacklightRGB(
                                        PipeReader.readInt(input, LCDRGBBacklightSchema.MSG_LCDRGBBACKLIGHT_200_FIELD_RED_201),
                                        PipeReader.readInt(input, LCDRGBBacklightSchema.MSG_LCDRGBBACKLIGHT_200_FIELD_GREED_202),
                                        PipeReader.readInt(input, LCDRGBBacklightSchema.MSG_LCDRGBBACKLIGHT_200_FIELD_BLUE_203)
                                       );
                    PipeReader.releaseReadLock(input);
                break;
            
            }
            
        }
        
    }


}
