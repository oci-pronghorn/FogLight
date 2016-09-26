package com.ociweb.iot.maker;

/**
 * Functional interface for a handler of REST service events.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface RestListener {

    /**
     * TODO: No idea how this works.
     *
     * @param route
     * @param fieldsInPipe
     */
    void restRequest(int route, Object fieldsInPipe);
    
}
