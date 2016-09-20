package com.ociweb.iot.maker;

/**
 * Base interface for a maker's IoT application.
 *
 * An implementation of this interface should be supplied
 * to {@link DeviceRuntime#run(IoTSetup)} in order to declare
 * the connections (digital, analog, etc.) and behavior (listeners)
 * that an IoT application will use during its lifecycle.
 *
 * @author Nathan Tippy
 */
public interface IoTSetup {

    /**
     * Invoked when this IoTSetup is asked to declare any connections it needs.
     *
     * This method should perform all of its connection declarations directly on the
     * passed {@link Hardware} instance; any other changes will have no
     * effect on the final IoT runtime.
     *
     * @param hardware {@link Hardware} instance to declare connections on.
     *
     * @see Hardware
     */
    void declareConnections(Hardware hardware);

    /**
     * Invoked when this IoTSetup is asked to declare any behavior that it has.
     *
     * This method should should perform all of its behavior declarations directly
     * on the passed {@link DeviceRuntime} instance; any other changes will have
     * no effect on the final IoT runtime.
     *
     * @param runtime {@link DeviceRuntime} instance to declare behavior on.
     *
     * @see DeviceRuntime
     */
    void declareBehavior(DeviceRuntime runtime);
}
