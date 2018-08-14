package com.ociweb.iot.transducer;

import com.ociweb.gl.api.ListenerTransducer;
import com.ociweb.gl.impl.StateChangeListenerBase;

public interface StateChangeListenerTransducer <E extends Enum<E>> extends ListenerTransducer, StateChangeListenerBase <E> {

}
