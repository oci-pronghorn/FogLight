package com.ociweb.iot.hardware;

public interface IODevice {

     public int           response(); //in ms, do not poll faster than this
     public boolean       isInput();
     public boolean       isOutput();
     public boolean       isPWM();
     public int           range(); //for PWM and for A2D read
     public boolean       isGrove();
     public I2CConnection getI2CConnection(); //TODO: Grove Specific for non-I2C Devices
     public boolean		  isValid(byte[] backing, int position, int length, int mask);
     public int           pinsUsed();//count of contiguous pins used, eg almost always 1 but would be 2 for the grove rotary encoder
     
}
