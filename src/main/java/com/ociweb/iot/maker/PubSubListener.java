package com.ociweb.iot.maker;

/**
 * Functional interface for a publish-subscribe subscriber registered
 * with the {@link DeviceRuntime}.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface PubSubListener {

    /**
     * Invoked when a new publication is received from the {@link DeviceRuntime}.
     *
     * @param topic Topic of the publication.
     * @param payload {@link PayloadReader} for the topic contents.
     */
    void message(CharSequence topic, PayloadReader payload);
}
