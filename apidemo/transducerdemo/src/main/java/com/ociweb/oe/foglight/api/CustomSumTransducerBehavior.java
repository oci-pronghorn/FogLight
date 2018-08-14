package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.maker.FogRuntime;

public class CustomSumTransducerBehavior implements Behavior {
	CustomSumTransducer cst;

	public CustomSumTransducerBehavior(FogRuntime runtime) {
		cst = new CustomSumTransducer(runtime);
	}
}
