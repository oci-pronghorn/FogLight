package com.ociweb.iot.maker;

import com.ociweb.iot.hardware.Hardware;

public interface IoTSetup {

    public void declareConnections(Hardware c);
    public void declareBehavior(DeviceRuntime runtime);
    
}
