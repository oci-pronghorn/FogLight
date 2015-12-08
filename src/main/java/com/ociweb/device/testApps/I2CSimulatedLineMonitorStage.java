package com.ociweb.device.testApps;

import java.io.IOException;

import com.ociweb.device.grove.schema.I2CBusSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;

public class I2CSimulatedLineMonitorStage extends PronghornStage {

    private final Pipe<I2CBusSchema> input;
    private final Appendable out = System.out;
    private long lastTime = 0;
    private final int NAONS_PER_SECOND = 1000*1000*1000; 
    private final int CYCLES_PER_SECOND = 100*1000;//safe limit for i2c
    private final int SAFE_NANO_DIF = NAONS_PER_SECOND / CYCLES_PER_SECOND; 
    
    protected I2CSimulatedLineMonitorStage(GraphManager graphManager, Pipe<I2CBusSchema> input) {
        super(graphManager, input, NONE);
        this.input = input;
    }

    @Override
    public void startup() {
        
        try {
            out.append("required nanos per change ");
            Appendables.appendFixedDecimalDigits(out, SAFE_NANO_DIF, 100000).append('\n');
            
            out.append("sc.mil.mcr.nan Clk Dat  \n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    @Override
    public void run() {
        
        while (PipeReader.tryReadFragment(input)) {
                        
            int msgIdx = PipeReader.getMsgIdx(input);
            switch(msgIdx) {
                case I2CBusSchema.MSG_POINT_100:
                    outputPointDetails();
                break;
                case I2CBusSchema.MSG_STATE_200:
                    outputStateDetails();
                break;    
            }
            
            //pull common time and check that its always incrementing.
            //PipeReader.readLong(input, I2CBusSchema.MSG_STATE_200_FIELD_TIME_103)
            
            
            PipeReader.releaseReadLock(input);
            
            
        }
    }
    
    private void outputStateDetails() {
        try {
           
            appendTime(PipeReader.readLong(input, I2CBusSchema.MSG_STATE_200_FIELD_TIME_103), out);
            out.append("              ").append('S').append(':');
            
            Appendables.appendFixedDecimalDigits(out, PipeReader.readInt(input,  I2CBusSchema.MSG_STATE_200_FIELD_TASK_201), 10).append(':');
            Appendables.appendFixedDecimalDigits(out, PipeReader.readInt(input,  I2CBusSchema.MSG_STATE_200_FIELD_STEP_202), 10).append(' ').append('B').append(':');
            Appendables.appendFixedHexDigits(out, 0xFF & PipeReader.readInt(input,  I2CBusSchema.MSG_STATE_200_FIELD_BYTE_202), 8).append('\n');
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }        
        
    }

    private void outputPointDetails() {
        long time = PipeReader.readLong(input, I2CBusSchema.MSG_POINT_100_FIELD_TIME_103);
        try {
            
            appendTime(time, out).append(' ');
            Appendables.appendFixedDecimalDigits(out, PipeReader.readInt(input,  I2CBusSchema.MSG_POINT_100_FIELD_CLOCK_101), 1).append(' ').append(' ').append(' ');
            Appendables.appendFixedDecimalDigits(out, PipeReader.readInt(input,  I2CBusSchema.MSG_POINT_100_FIELD_DATA_102), 1);
            
            if (0 != lastTime) {
                int dif = (int)(time-lastTime);
                if (dif<SAFE_NANO_DIF) {
                    out.append(" *** Line change was too soon ");
                }
            }
            
            out.append('\n');
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        lastTime = time;
    }

    private Appendable appendTime(long nanoTime, Appendable target) throws IOException {
        
        //this becomes a single operation
        long totalMicro  = nanoTime/1000;
        long       nanos = nanoTime%1000;
        
        long totalMili  = totalMicro/1000;
        long     micros = totalMicro%1000;
        
        long totalSec = totalMili/1000;
        long    milis = totalMili%1000;
        
        long min = totalSec/60;
        long sec = totalSec%60;
        
        Appendables.appendFixedDecimalDigits(target, (int)sec,      10).append('.');
        Appendables.appendFixedDecimalDigits(target, (int)milis,   100).append('.');
        Appendables.appendFixedDecimalDigits(target, (int)micros,  100).append('.');
        Appendables.appendFixedDecimalDigits(target, (int)nanos,   100).append(' ');
      
        return target;
    }


    
    
}
