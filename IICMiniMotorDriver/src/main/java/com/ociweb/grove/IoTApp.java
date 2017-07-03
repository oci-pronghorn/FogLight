package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.*;
import static com.ociweb.iot.grove.I2CGroveTwig.*;
import com.ociweb.iot.grove.mini_i2c_motor.*;
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
        c.connect(Mini_I2C_Motor);
        c.connect(AngleSensor,ANGLE_SENSOR);
        
        
    }
    
    @Override
    public void declareBehavior(FogRuntime g) {
        final FogCommandChannel c = g.newCommandChannel();
        
        Mini_I2C_Motor motorController = new Mini_I2C_Motor(c);
        
        g.addAnalogListener((port, time, durationMillis, average, value)->{
            //if(!motorFailed){
                System.out.println("value: "+value);
                int speed = (value-512)/8;
                
                motorController.driveMotor1(speed);
                motorController.driveMotor2(speed);
            //}
        }).includePorts(ANGLE_SENSOR);
        
        g.addI2CListener((int addr, int register, long time, byte[] backing, int position, int length, int mask)->{
            if(backing[position]>0){
                //motorFailed = true;
                System.out.println("Motor failure.");
                motorController.stopMotor1();
                motorController.stopMotor2();
            }
            
        }).excludeI2CConnections(4);
        
//      g.addDigitalListener((port, connection, time, value)->{
//            if(value==1){
//                System.out.println("starting Motor 1");
//                motorController.driveMotor1(50);
//                System.out.println("starting Motor 2");
//                motorController.driveMotor2(50);
//            }else{
//                motorController.stopMotor1();
//                motorController.stopMotor2();
//            }
//        });

    }
}
