package com.ociweb.iot.maker;

import com.ociweb.pronghorn.stage.network.config.HTTPContentType;

public interface HTTPResponseListener {

	//-1 statusCode indicates the network connection was lost
	void responseHTTP(CharSequence host, int port, short statusCode, HTTPContentType type, PayloadReader reader);

}
