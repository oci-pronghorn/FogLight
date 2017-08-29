package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.HTTPRequestReader;
import com.ociweb.gl.api.Headable;
import com.ociweb.gl.api.Payloadable;
import com.ociweb.gl.api.RestListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.network.config.HTTPHeaderDefaults;
import com.ociweb.pronghorn.pipe.BlobReader;

public class RestBehaviorEmptyResponse implements RestListener {

	final byte[] cookieHeader = HTTPHeaderDefaults.COOKIE.rootBytes();
	final byte[] fieldName;
	private final FogCommandChannel cmd;
	
	public RestBehaviorEmptyResponse(FogRuntime runtime, byte[] myArgName) {
		this.fieldName = myArgName;		
		this.cmd = runtime.newCommandChannel(NET_RESPONDER);
	}
	
	Payloadable reader = new Payloadable() {
		
		@Override
		public void read(BlobReader reader) {
			
			System.out.println("POST: "+reader.readUTFOfLength(reader.available()));
			
		}			
	};

	private Headable headReader = new Headable() {

		@Override
		public void read(int id, BlobReader reader) { 
			
			System.out.println("COOKIE: "+reader.readUTFOfLength(reader.available()));
						
		}
		
	};


	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
	    int argInt = request.getInt(fieldName);
	    System.out.println("Arg Int: "+argInt);
		
		request.openHeaderData(cookieHeader, headReader);
		
		if (request.isVerbPost()) {
			request.openPayloadData(reader );
		}
		
		//no body just a 200 ok response.
		return cmd.publishHTTPResponse(request, 200);

	}

}
