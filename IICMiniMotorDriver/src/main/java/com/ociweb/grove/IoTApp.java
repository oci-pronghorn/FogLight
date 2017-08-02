package com.ociweb.grove;


import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.grove.mini_motor_driver.MiniMotorDriverTwig.*;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }
    
    private static final Port ANGLE_SENSOR = A0;
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(MiniMotorDriver.checkFaultCH1);
        c.connect(MiniMotorDriver.checkFaultCH2);
        c.connect(AngleSensor,ANGLE_SENSOR);
    }
    
    @Override
    public void declareBehavior(FogRuntime g) {
        g.registerListener(new MiniMotorBehavior(g));
    }
}
