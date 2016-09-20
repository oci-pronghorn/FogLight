package com.ociweb.iot.maker;

import com.ociweb.iot.hardware.IODevice;

/**
 * Base interface for an IoT device's hardware.
 * <p>
 * This interface is most commonly used in conjunction with a call
 * to {@link IoTSetup#declareConnections(Hardware)} in order for
 * a maker's code to declare any hardware connections and resources
 * that it makes use of.
 *
 * @author Nathan Tippy
 */
public interface Hardware {

    Hardware connect(IODevice t, Port port);
    Hardware connect(IODevice t, Port port, int customRateMS);
    Hardware connect(IODevice t, Port port, int customRateMS, int customAvgWinMS);
    Hardware connect(IODevice t, Port port, int customRateMS, int customAvgWinMS, boolean everyValue);

    Hardware connectI2C(IODevice t);

    <E extends Enum<E>> Hardware startStateMachineWith(E state);

    Hardware setTriggerRate(long rateInMS);
    Hardware setTriggerRate(TimeTrigger trigger);


    Hardware useI2C();
}
