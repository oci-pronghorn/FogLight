package com.ociweb.grove;


import java.io.IOException;

import com.ociweb.gl.api.*;
import com.ociweb.iot.maker.*;
import com.ociweb.pronghorn.network.config.HTTPContentTypeDefaults;

public class RestfulWaterSensorBehavior implements AnalogListener, RestListener  {
	private int val = -1;
	private FogCommandChannel ch;

	public RestfulWaterSensorBehavior(FogRuntime runtime) {
		this.ch = runtime.newCommandChannel(MsgCommandChannel.NET_RESPONDER | MsgCommandChannel.DYNAMIC_MESSAGING); 
	}	

	@Override
	public boolean restRequest(HTTPRequestReader reader) {
		return ch.publishHTTPResponse(reader, 200, HTTPFieldReader.END_OF_RESPONSE | HTTPFieldReader.CLOSE_CONNECTION, HTTPContentTypeDefaults.HTML,
				(writer)->{
					try {
						writer.append(Integer.toString(this.val));
					} catch (Exception e) {

						e.printStackTrace();
					}		
				});
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		System.out.println(val);
		this.val = value;
	}

}
