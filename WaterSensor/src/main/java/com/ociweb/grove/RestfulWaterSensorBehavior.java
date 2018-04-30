package com.ociweb.grove;


import java.io.IOException;

import com.ociweb.gl.api.*;
import com.ociweb.iot.maker.*;
import com.ociweb.pronghorn.network.config.HTTPContentTypeDefaults;

public class RestfulWaterSensorBehavior implements AnalogListener, RestListener  {
	private int val = -1;
	private final PubSubService pubSubService;
	private final HTTPResponseService httpResponseService;


	public RestfulWaterSensorBehavior(FogRuntime runtime) {
		FogCommandChannel ch = runtime.newCommandChannel();
		pubSubService = ch.newPubSubService();
		httpResponseService = ch.newHTTPResponseService();
	}

	@Override
	public boolean restRequest(HTTPRequestReader reader) {
		return httpResponseService.publishHTTPResponse(reader, 200, false, HTTPContentTypeDefaults.HTML,
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
