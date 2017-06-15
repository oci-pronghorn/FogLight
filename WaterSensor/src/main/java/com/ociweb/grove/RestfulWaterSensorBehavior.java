package com.ociweb.grove;


import com.ociweb.gl.api.*;
import com.ociweb.iot.maker.*;
import com.ociweb.pronghorn.network.config.HTTPContentTypeDefaults;

public class RestfulWaterSensorBehavior implements AnalogListener, RestListener  {
	private int val = -1;
	private FogCommandChannel ch;
	
	public RestfulWaterSensorBehavior(FogRuntime runtime) {
		this.ch = runtime.newCommandChannel(GreenCommandChannel.NET_RESPONDER | GreenCommandChannel.DYNAMIC_MESSAGING); 
	}	

	@Override
	public boolean restRequest(HTTPRequestReader reader) {
		return ch.publishHTTPResponse(reader, 200, HTTPFieldReader.END_OF_RESPONSE | HTTPFieldReader.CLOSE_CONNECTION, HTTPContentTypeDefaults.HTML,
				(writer)->{
					writer.append(Integer.toString(this.val));
				});
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
			System.out.println(val);
			this.val = value;
	}

}
