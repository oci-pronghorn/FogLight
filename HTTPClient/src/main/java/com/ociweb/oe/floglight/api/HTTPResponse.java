package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.HTTPResponseListener;
import com.ociweb.gl.api.HTTPResponseReader;
import com.ociweb.pronghorn.network.config.HTTPContentType;

public class HTTPResponse implements HTTPResponseListener {

	@Override
	public boolean responseHTTP(CharSequence host, int port, short statusCode,
			                    HTTPContentType type, HTTPResponseReader reader) {
		
		System.out.println(host+":"+port+" status:"+statusCode);
		System.out.println(type);
		
		//TODO: not sure this is the right interface for this?
		System.out.println(reader.readUTFOfLength(reader.available()));
		
		return true;
	}

}
