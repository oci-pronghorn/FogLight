package com.ociweb.iot.maker;

import com.ociweb.gl.api.Builder;
import com.ociweb.iot.hardware.AnalogDigitalIODevice;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.hardware.IODevice;

/**
 * Base interface for an IoT device's hardware.
 * <p>
 * This interface is most commonly used in conjunction with a call
 * to IoTSetup declareConnections in order for
 * a maker's code to declare any hardware connections and resources
 * that it makes use of.
 *
 * @author Nathan Tippy
 */
public interface Hardware extends Builder {

    /**
     * Connects a new {@link IODevice} to this hardware on a given {@link Port}.
     *
     * @param device {@link IODevice} to connect.
     * @param port {@link Port} to connect the device to.
     * @param customRateMS Optional rate in milliseconds to update the device data. TODO: Correct?
     * @param customAvgWinMS Optional rate in milliseconds to sample device data. TODO: Correct?
     * @param everyValue Optional; if set to true, will cause the device to trigger events on every update. TODO: Correct?
     *
     * @return A reference to this hardware instance.
     */
    Hardware connect(AnalogDigitalIODevice device, Port port, int customRateMS, int customAvgWinMS, boolean everyValue);
    /**
     * 
     * @param device {@link IODevice} to connect.
     * @param port {@link Port} to connect the device to.
     * @param customRateMS Optional rate in milliseconds to update the device data.
     * @param customAvgWinMS Optional rate in milliseconds to sample device data.
     * @return  A reference to this hardware instance.
     */
    Hardware connect(AnalogDigitalIODevice device, Port port, int customRateMS, int customAvgWinMS);
    /**
     * 
     * @param device {@link IODevice} to connect.
     * @param port {@link Port} to connect the device to.
     * @param customRateMS Optional rate in milliseconds to update the device data.
     * @return A reference to this hardware instance.
     */
    Hardware connect(AnalogDigitalIODevice device, Port port, int customRateMS);
    /**
     * 
     * @param device {@link IODevice} to connect.
     * @param port {@link Port} to connect the device to.
     * @param customRateMS Optional rate in milliseconds to update the device data.
     * @param everyValue Optional; if set to true, will cause the device to trigger events on every update.
     * @return A reference to this hardware instance.
     */
    Hardware connect(AnalogDigitalIODevice device, Port port, int customRateMS, boolean everyValue);
    /**
     * 
     * @param device {@link IODevice} to connect.
     * @param port {@link Port} to connect the device to.
     * @return A reference to this hardware instance.
     */
    Hardware connect(AnalogDigitalIODevice device, Port port);

    /**
     * calls connectI2C to connect the I2C IODevice
     * @param device
     * @return A reference to this hardware instance
     */
    Hardware connect(I2CIODevice device);
    
    /**
     * calls connectI2C to connect the I2C IODevice
     * @param device
     * @param customRateMS
     * @return A reference to this hardware instance
     */
    
    Hardware connect(I2CIODevice device, int customRateMS);
    /**
     * Connects a new I2C {@link IODevice} to this hardware.
     *
     * @param device {@link IODevice} to connect.
     *
     * @return A reference to this hardware instance.
     */
    Hardware connectI2C(I2CIODevice device);
    
    /**
     * 
     * @param device {@link IODevice} to connect.
     * @param customRateMS Optional rate in milliseconds to read data from that register.
     * @return A reference to this hardware instance.
     */
    Hardware connectI2C(I2CIODevice device, int customRateMS);
    
    /**
     * Asks this hardware instance to enable I2C communications on the default I2C bus.
     *
     * @return A reference to this hardware instance.
     */
    Hardware useI2C();

    /**
     * Asks this hardware instance to enable I2C communications.
     *
     * @param bus I2C bus to use.
     *
     * @return A reference to this hardware instance.
     */
    Hardware useI2C(int bus);
    
   
   
}
