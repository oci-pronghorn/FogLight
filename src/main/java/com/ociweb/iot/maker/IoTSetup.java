package com.ociweb.iot.maker;

import com.ociweb.iot.hardware.Hardware;

public interface IoTSetup {

    public void specifyConnections(Hardware c);
    public void declareBehavior(IOTDeviceRuntime runtime);
    
}
