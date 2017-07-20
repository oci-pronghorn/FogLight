package com.ociweb.iot.facade;

import com.ociweb.gl.impl.StateChangeListenerBase;

public interface StateChangeListenerFacade <E extends Enum<E>> extends ListenerFacade, StateChangeListenerBase <E> {

}
