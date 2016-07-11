package com.ociweb.iot.maker;

import com.ociweb.iot.hardware.Hardware;

public interface IoTApp {

    public void specifyConnections(Hardware c);
    public void setup(IOTDeviceRuntime runtime);
    
}
