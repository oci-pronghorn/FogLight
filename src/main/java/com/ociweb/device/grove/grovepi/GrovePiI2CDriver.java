package com.ociweb.device.grove.grovepi;


/**
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class GrovePiI2CDriver {

    private static final int I2CSPEED = 100;
    private static boolean started = false;

//    void I2C_delay();
//    bool read_SCL(); // Set SCL as input and return current level of line, 0 or 1
//    bool read_SDA(); // Set SDA as input and return current level of line, 0 or 1
//    void set_SCL();
//    void clear_SCL(); // Actively drive SCL signal low
//    void set_SDA(); // Actively drive SDA signal high
//    void clear_SDA(); // Actively drive SDA signal low
//    void arbitration_lost();
    public static boolean read_SCL() { return false; }
    public static boolean read_SDA() { return false; }
    public static void set_SCL() { }
    public static void clear_SCL() { }
    public static void set_SDA() { }
    public static void clear_SDA() { }
    public static void arbitration_lost() { }

    public static void i2c_start_cond ()
    {
      if( started ) 
      { 
        // if started, do a restart cond
        // set SDA to 1
        set_SDA();
        I2C_delay();

        while( read_SCL() == false ) //0 
        {  // Clock stretching
          // You should add timeout to this loop
        }

        // Repeated start setup time, minimum 4.7us
        I2C_delay();

      }

      if( read_SDA() == false ) //0 
      {
        arbitration_lost();
      }

      // SCL is high, set SDA from 1 to 0.
      clear_SDA();
      I2C_delay();
      clear_SCL();
      started = true;

    }

    public static void i2c_stop_cond()
    {
      // set SDA to 0
      clear_SDA();
      I2C_delay();

      // Clock stretching
      while( read_SCL() == false ) //0 
      {
        // add timeout to this loop.
      }

      // Stop bit setup time, minimum 4us
      I2C_delay();

      // SCL is high, set SDA from 0 to 1
      set_SDA();
      I2C_delay();

      if( read_SDA() == false ) // 0 
      {
        arbitration_lost();
      }

      I2C_delay();
      started = false;

    }

    // Write a bit to I2C bus
    public static void i2c_write_bit( boolean bit ) 
    {
      if(bit) 
      {
        set_SDA();
      } 
      else 
      {
        clear_SDA();
      }

      // SDA change propagation delay
      I2C_delay();  

      // Set SCL high to indicate a new valid SDA value is available
      set_SCL();

      // Wait for SDA value to be read by slave, minimum of 4us for standard mode
      I2C_delay();

      while( read_SCL() == false ) //0 
      { // Clock stretching
        // You should add timeout to this loop
      }

      // SCL is high, now data is valid
      // If SDA is high, check that nobody else is driving SDA
      if( bit && ( read_SDA() == false ) ) //0
      {
        arbitration_lost();
      }

      // Clear the SCL to low in preparation for next change
      clear_SCL();
    }

    // Read a bit from I2C bus
    public static boolean i2c_read_bit() 
    {
      boolean bit;

      // Let the slave drive data
      set_SDA();

      // Wait for SDA value to be written by slave, minimum of 4us for standard mode
      I2C_delay();

      // Set SCL high to indicate a new valid SDA value is available
      set_SCL();

      while( read_SCL() == false ) //0 
      { // Clock stretching
        // You should add timeout to this loop
      }

      // Wait for SDA value to be read by slave, minimum of 4us for standard mode
      I2C_delay();

      // SCL is high, read out bit
      bit = read_SDA();

      // Set SCL low in preparation for next operation
      clear_SCL();

      return bit;

    }

    // Write a byte to I2C bus. Return 0 if ack by the slave.
    public static boolean i2c_write_byte(boolean send_start ,
                                         boolean send_stop  ,
                                         char b) 
    {
      int bit;
      boolean nack;

      if( send_start ) 
      {
        i2c_start_cond();
      }

      for( bit = 0; bit < 8; bit++ ) 
      {
        i2c_write_bit( (b & 0x80) != 0);
        b <<= 1;
      }

      nack = i2c_read_bit();

      if (send_stop) 
      {
        i2c_stop_cond();
      }

      return nack;

    }

    // Read a byte from I2C bus
    public static char i2c_read_byte(boolean nack , boolean send_stop) 
    {
      char b = 0;
      char bit;

      for( bit = 0; bit < 8; bit++ ) 
      {
        b = (char) (( b << 1 ) | (i2c_read_bit() ? 1 : 0));
      }

      i2c_write_bit( nack );

      if( send_stop ) 
      {
        i2c_stop_cond();
      }

      return b;
    }

    public static void I2C_delay() 
    { 
      int v;
      int i;

      for( i = 0; i < I2CSPEED / 2; i++ ) {
          //v;
      }
    }
}
