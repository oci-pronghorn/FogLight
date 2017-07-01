package com.ociweb.oe.floglight.api;

import com.ociweb.iot.maker.FogRuntime;

public class FogLight {

	public static void main(String[] args) {
		FogRuntime.run(new MQTTClient());
	}
	
}
