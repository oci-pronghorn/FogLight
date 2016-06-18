package com.ociweb.iot.grove.device.lcdrgb;

import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class LCDRGBContentStage extends LCDRGBContentAbstractStage {

    private final Pipe<LCDRGBContentSchema> input;
    private final Pipe<I2CCommandSchema> output;
    private DataInputBlobReader<LCDRGBContentSchema> reader;
    
    public LCDRGBContentStage(GraphManager graphManager, Pipe<LCDRGBContentSchema> input, Pipe<I2CCommandSchema> output) {
        super(graphManager, input, output);
        this.input = input;
        this.output = output;
    }
    
    @Override
    public void startup() {
        reader = new DataInputBlobReader<LCDRGBContentSchema>(input);
    }
    
    @Override
    public void run() {
        
        while (Pipe.hasRoomForWrite(output) &&  PipeReader.tryReadFragment(input)) {
            
            switch (PipeReader.getMsgIdx(input)) {
                case -1:
                    requestShutdown();
                break;
                case LCDRGBContentSchema.MSG_LCDRGBTEXT_210:
                    
                    reader.openHighLevelAPIField(LCDRGBContentSchema.MSG_LCDRGBTEXT_210_FIELD_TEXT_211);
                    
                    requestText(reader);
                    
                    PipeReader.releaseReadLock(input);
                break;
            
            }
            
        }
        
    }



}
