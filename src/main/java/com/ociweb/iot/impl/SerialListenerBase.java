package com.ociweb.iot.impl;

import com.ociweb.pronghorn.pipe.BlobReader;

public interface SerialListenerBase {
	public int message(BlobReader reader);
}
