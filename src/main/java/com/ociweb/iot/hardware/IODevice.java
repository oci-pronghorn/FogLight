package com.ociweb.iot.hardware;

public interface IODevice {

     public int           response(); //in ms, do not poll faster than this, 0 is continuous audio
     public boolean       isInput();
     public boolean       isOutput();
     public boolean       isPWM();
     public int           range(); //for PWM and for A2D read
     public boolean       isGrove();
     public I2CConnection getI2CConnection();
     
}
