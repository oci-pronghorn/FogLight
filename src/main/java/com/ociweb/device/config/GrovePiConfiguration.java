package com.ociweb.device.config;

import com.ociweb.device.grove.GroveConnect;

/**
 * TODO:
 */
public class GrovePiConfiguration extends GroveConnectionConfiguration
{
    /**
     * TODO:
     *
     * @param publishTime
     * @param configI2C
     * @param encoderInputs
     * @param digitalInputs
     * @param digitalOutputs
     * @param pwmOutputs
     * @param analogInputs
     */
    public GrovePiConfiguration(boolean publishTime, boolean configI2C, GroveConnect[] encoderInputs, GroveConnect[] digitalInputs, GroveConnect[] digitalOutputs, GroveConnect[] pwmOutputs, GroveConnect[] analogInputs)
    {
        //TODO: Auto-generated Constructor.
        super(publishTime, configI2C, encoderInputs, digitalInputs, digitalOutputs,
              pwmOutputs, analogInputs);
    }

    //Member Function: readBit
    @Override
    public int readBit(int connector)
    {
        //TODO: Auto-generated Method Statement.
        return 0;
    }

    //Member Function: readInt
    @Override
    public int readInt(int connector)
    {
        //TODO: Auto-generated Method Statement.
        return 0;
    }

    //Member Function: configurePinsForDigitalInput
    @Override
    public void configurePinsForDigitalInput(byte connection)
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: configurePinsForAnalogInput
    @Override
    public void configurePinsForAnalogInput(byte connection)
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: configurePinsForI2C
    @Override
    public void configurePinsForI2C()
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: i2cSetClockLow
    @Override
    public void i2cSetClockLow()
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: i2cSetClockHigh
    @Override
    public void i2cSetClockHigh()
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: i2cSetDataLow
    @Override
    public void i2cSetDataLow()
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: i2cSetDataHigh
    @Override
    public void i2cSetDataHigh()
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: i2cReadData
    @Override
    public int i2cReadData()
    {
        //TODO: Auto-generated Method Statement.
        return 0;
    }

    //Member Function: i2cReadClock
    @Override
    public int i2cReadClock()
    {
        //TODO: Auto-generated Method Statement.
        return 0;
    }

    //Member Function: i2cDataIn
    @Override
    public void i2cDataIn()
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: i2cDataOut
    @Override
    public void i2cDataOut()
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: i2cClockIn
    @Override
    public void i2cClockIn()
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: i2cClockOut
    @Override
    public void i2cClockOut()
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: i2cReadAck
    @Override
    public boolean i2cReadAck()
    {
        //TODO: Auto-generated Method Statement.
        return false;
    }

    //Member Function: coldSetup
    @Override
    public void coldSetup()
    {
        //TODO: Auto-generated Method Statement.
        
    }

    //Member Function: cleanup
    @Override
    public void cleanup()
    {
        //TODO: Auto-generated Method Statement.
        
    }
}
