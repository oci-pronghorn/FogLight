package com.ociweb.iot.maker;

import com.ociweb.gl.api.Builder;
import com.ociweb.iot.hardware.ADIODevice;
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
     * Sets the frequency at which images are taken on this hardware.
     *
     * @param triggerRateMillis Number of milliseconds between image capture events.
     *
     * @throws RuntimeException if the trigger rate is less than 1,250.
     */
    public void setImageTriggerRate(int triggerRateMillis) throws RuntimeException;

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
    Hardware connect(ADIODevice device, Port port, int customRateMS, int customAvgWinMS, boolean everyValue);
    /**
     * 
     * @param device {@link IODevice} to connect.
     * @param port {@link Port} to connect the device to.
     * @param customRateMS Optional rate in milliseconds to update the device data.
     * @param customAvgWinMS Optional rate in milliseconds to sample device data.
     * @return  A reference to this hardware instance.
     */
    Hardware connect(ADIODevice device, Port port, int customRateMS, int customAvgWinMS);
    /**
     * 
     * @param device {@link IODevice} to connect.
     * @param port {@link Port} to connect the device to.
     * @param customRateMS Optional rate in milliseconds to update the device data.
     * @return A reference to this hardware instance.
     */
    Hardware connect(ADIODevice device, Port port, int customRateMS);
    /**
     * 
     * @param device {@link IODevice} to connect.
     * @param port {@link Port} to connect the device to.
     * @param customRateMS Optional rate in milliseconds to update the device data.
     * @param everyValue Optional; if set to true, will cause the device to trigger events on every update.
     * @return A reference to this hardware instance.
     */
    Hardware connect(ADIODevice device, Port port, int customRateMS, boolean everyValue);
    /**
     * 
     * @param device {@link IODevice} to connect.
     * @param port {@link Port} to connect the device to.
     * @return A reference to this hardware instance.
     */
    Hardware connect(ADIODevice device, Port port);

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
    
    Hardware useSerial(Baud baud);
    
	/**
     *             
	 * @param baud
	 * @param device Name of the port. On UNIX systems this will typically
     *             be of the form /dev/ttyX, where X is a port number. On
     *             Windows systems this will typically of the form COMX,
     *             where X is again a port number.
	 */
    Hardware useSerial(Baud baud, String device);
   
    
    boolean isTestHardware();
   
}
