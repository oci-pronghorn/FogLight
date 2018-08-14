package com.ociweb.iot.hardware;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.maker.Port;

public interface ADIODevice extends IODevice {
	public <F extends IODeviceTransducer> F newTransducer(Port p, FogCommandChannel... ch);

}
