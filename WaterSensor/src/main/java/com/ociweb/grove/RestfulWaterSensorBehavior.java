package com.ociweb.grove;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.HTTPFieldReader;
import com.ociweb.gl.api.HTTPRequestReader;
import com.ociweb.gl.api.RestListener;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.network.config.HTTPContentTypeDefaults;

public class RestfulWaterSensorBehavior implements AnalogListener, RestListener  {
	private int val = -1;
	private CommandChannel ch;
	
	public RestfulWaterSensorBehavior(DeviceRuntime runtime) {
		this.ch = runtime.newCommandChannel(GreenCommandChannel.NET_RESPONDER | GreenCommandChannel.DYNAMIC_MESSAGING); 
	}	

	@Override
	public boolean restRequest(HTTPRequestReader reader) {
		
		return ch.publishHTTPResponse(reader, 200, HTTPFieldReader.END_OF_RESPONSE | HTTPFieldReader.CLOSE_CONNECTION, HTTPContentTypeDefaults.HTML,
				(writer)->{
					writer.writeInt(this.val);
				});
		
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		if (port == RestfulWaterSensorConstants.WATER_SENSOR_PORT){
			this.val = value;
		}
	}

}
