package com.ociweb.pronghorn.iot.i2c;

import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.i2c.impl.I2CGroveJavaBacking;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO:
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public final class I2CStageFactory {
//Private//////////////////////////////////////////////////////////////////////

    private static final Map<String, Class<? extends I2CBacking>> backings;
    static {
        backings = new HashMap<String, Class<? extends I2CBacking>>();
        backings.put("native_linux", I2CNativeLinuxBacking.class);
        backings.put("java_grove", I2CGroveJavaBacking.class);
    }

//Public///////////////////////////////////////////////////////////////////////

    public static I2CStage get(GraphManager graphManager, Pipe<I2CCommandSchema> i2cBusPipe) {
        return null;
    }
}
