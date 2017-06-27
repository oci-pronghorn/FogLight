package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.grove.mini_i2c_motor.Grove_Mini_I2CMotor;
import com.ociweb.iot.grove.OLED.LCD_RGB.Grove_LCD_RGB;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.I2CListener;
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
    
    //private static final Port BUTTON_PORT = D3;
    //private static final Port LED_PORT    = D4;
    //private static final Port RELAY_PORT  = D7;
    //private static final Port LIGHT_SENSOR_PORT= A2;
//    private static final Port ANGLE_SENSOR = A0;
    
    @Override
    public void declareConnections(Hardware c) {
        c.useI2C();
        //c.setTriggerRate(5000);
        //c.connect(Button,D2);
        //c.connectI2C(I2C);
        c.connect(Button,D3);
        c.connectI2C(Grove_Mini_I2CMotor.instance);
//        c.connect(AngleSensor,ANGLE_SENSOR);


    }
    
    @Override
    public void declareBehavior(FogRuntime g) {
        final FogCommandChannel c = g.newCommandChannel();
        
        g.addDigitalListener((port, connection, time, value)->{
            if(value==1){
                System.out.println("starting Motor 1");
                Grove_Mini_I2CMotor.driveMotor1(c,50);
                System.out.println("starting Motor 2");
                Grove_Mini_I2CMotor.driveMotor2(c,50);
            }else{
                Grove_Mini_I2CMotor.stopMotor1(c);
                Grove_Mini_I2CMotor.stopMotor2(c);
            }
        });
//        
//        g.addAnalogListener((port, time, durationMillis, average, value)->{
//            System.out.println("value: "+value);
//            int speed = (value-512)/8;
//            Grove_LCD_RGB.commandForColor(c, 200, 200, 180);
//            Grove_LCD_RGB.commandForText(c,Integer.toString(speed));
//            
//            Grove_Mini_I2CMotor.driveMotor1(c,speed);
//            Grove_Mini_I2CMotor.driveMotor2(c,speed);
//            
//        }).includePorts(ANGLE_SENSOR);
        g.addI2CListener((int addr, int register, long time, byte[] backing, int position, int length, int mask)->{
            System.out.println("addr: "+addr);
            System.out.println("reg: "+register);
            System.out.println("backing: "+Integer.toHexString(backing[position]));
            System.out.println("position: "+position);
            System.out.println("length: "+length);
                    
        }).excludeI2CConnections(4);
        
//        g.addStartupListener(()->{
//            Grove_LCD_RGB.commandForColor(c, 200, 200, 180);
//            Grove_LCD_RGB.commandForText(c,Integer.toString(123));
//            
//            Grove_Mini_I2CMotor.driveMotor1(c,50);
//        });
        
//        g.addTimeListener((time,instance)-> {
//            System.out.println("starting Motor 1");
//            Grove_Mini_I2CMotor.driveMotor1(c,50);
//            System.out.println("starting Motor 2");
//            Grove_Mini_I2CMotor.driveMotor2(c,-50);
//
//        });
    }
    
    
}
