package com.ociweb.iot.maker;

import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.impl.AnalogListenerBase;

/**
 * Functional interface for analog events registered with the
 * {@link FogRuntime}.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface AnalogListener extends Behavior, AnalogListenerBase {

}
