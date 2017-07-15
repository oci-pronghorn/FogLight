package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.HTTPFieldReader;
import com.ociweb.gl.api.HTTPRequestReader;
import com.ociweb.gl.api.NetResponseWriter;
import com.ociweb.gl.api.NetWritable;
import com.ociweb.gl.api.Payloadable;
import com.ociweb.gl.api.RestListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.network.config.HTTPContentTypeDefaults;
import com.ociweb.pronghorn.pipe.BlobReader;

public class RestBehaviorLargeResponse implements RestListener {

	private final FogCommandChannel cmd;
	private int partNeeded = 0;
	
	public RestBehaviorLargeResponse(FogRuntime runtime) {	
		this.cmd = runtime.newCommandChannel(NET_RESPONDER);
	}
	
	Payloadable reader = new Payloadable() {
		
		@Override
		public void read(BlobReader reader) {
			
			System.out.println("POST: "+reader.readUTFOfLength(reader.available()));
			
		}			
	};


	NetWritable writableA = new NetWritable() {
		
		@Override
		public void write(NetResponseWriter writer) {
			writer.writeUTF8Text("beginning of text file\n");//23 in length
		}
		
	};
	
	NetWritable writableB = new NetWritable() {
		
		@Override
		public void write(NetResponseWriter writer) {
			writer.writeUTF8Text("ending of text file\n");//20 in length
		}
		
	};
	
	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
		if (request.isVerbPost()) {
			request.openPayloadData(reader);
		}
		
		if (0 == partNeeded) {
			boolean okA = cmd.publishHTTPResponse(request, 200, 
									request.getRequestContext(),
					                HTTPContentTypeDefaults.TXT,
					                writableA);
			if (!okA) {
				return false;
			} 
		}
				
		//////
		//NB: this block is here for demo reasons however one could
		//    publish a topic back to this behavior to complete the
		//    continuaton at a future time
		//////
	
		boolean okB = cmd.publishHTTPResponseContinuation(request,
						 		request.getRequestContext() | HTTPFieldReader.END_OF_RESPONSE,
						 		writableB);
		if (okB) {
			partNeeded = 0;
			return true;
		} else {
			partNeeded = 1;
			return false;
		}
	}

}
