package com.ociweb.grove;


import com.ociweb.iot.grove.mini_motor_driver.MiniMotorDriverTwig;
import com.ociweb.iot.grove.mini_motor_driver.MiniMotorDriver_Facade;
import static com.ociweb.iot.grove.AnalogDigitalTwig.*;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }
    ///////////////////////
    //Connection constants
    ///////////////////////
    // // by using constants such as these you can easily use the right value to reference where the sensor was plugged in
    
    private static final Port ANGLE_SENSOR = A0;
    //private boolean motorFailed = false;
    
    @Override
    public void declareConnections(Hardware c) {
        c.useI2C();
        c.connect(MiniMotorDriverTwig.ReadFault);
        c.connect(AngleSensor,ANGLE_SENSOR);
        
        
    }
    
    @Override
    public void declareBehavior(FogRuntime g) {
        final FogCommandChannel c = g.newCommandChannel();
        
        MiniMotorDriver_Facade motorController = new MiniMotorDriver_Facade(c);
        
        g.addAnalogListener((port, time, durationMillis, average, value)->{
            //if(!motorFailed){
                System.out.println("value: "+value);
                int speed = (value-512)/8;
                
                motorController.setVelocity(1, speed);
                motorController.setVelocity(2, speed);
            //}
        }).includePorts(ANGLE_SENSOR);
        
        g.addI2CListener((int addr, int register, long time, byte[] backing, int position, int length, int mask)->{
            if(backing[position]>0){
                //motorFailed = true;
                System.out.println("Motor failure.");
                motorController.stop(1);
                motorController.stop(2);
            }
            
        }).excludeI2CConnections(4);
        


    }
}
