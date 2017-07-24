package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.HTTPResponseListener;
import com.ociweb.gl.api.HTTPResponseReader;
import com.ociweb.gl.api.Payloadable;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.network.config.HTTPContentType;
import com.ociweb.pronghorn.pipe.BlobReader;

public class HTTPGetBehaviorSingle implements StartupListener, HTTPResponseListener {

	
	private final FogCommandChannel cmd;

	public HTTPGetBehaviorSingle(FogRuntime runtime) {
		cmd = runtime.newCommandChannel(NET_REQUESTER);
	}

	@Override
	public void startup() {
		cmd.httpGet("www.google.com","/");//"objectcomputing.com", "/");
	}

	@Override
	public boolean responseHTTP(short statusCode, 
			                    HTTPContentType type, HTTPResponseReader 
			                    reader) {
		
		System.out.println(" status:"+statusCode);
		System.out.println("   type:"+type);
		
//		//TODO: this is not yet implemented.. Under active work...
//		Payloadable payload = new Payloadable() {
//			@Override
//			public void read(BlobReader reader) {
//				System.out.println(reader.readUTFOfLength(reader.available()));
//			}
//		};
//		//TODO: This index location does not appear to be right
//		reader.openPayloadData(payload );
		
		return true;
	}

}
