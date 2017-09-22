package com.ociweb.iot.impl;

import com.ociweb.pronghorn.pipe.ChannelReader;

public interface SerialListenerBase {
	public int message(ChannelReader reader);
}
