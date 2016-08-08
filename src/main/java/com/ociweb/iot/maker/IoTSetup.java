package com.ociweb.iot.maker;

public interface IoTSetup {

    public void declareConnections(Hardware c);
    public void declareBehavior(DeviceRuntime runtime);
    
}
