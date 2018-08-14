package com.ociweb.iot.maker;

import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.impl.RotaryListenerBase;

/**
 * Functional interface for a listener that receives rotary events from
 * encoders registered with a {@link FogRuntime}.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface RotaryListener extends Behavior, RotaryListenerBase {

}
