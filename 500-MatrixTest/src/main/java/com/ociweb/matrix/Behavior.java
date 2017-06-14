package com.ociweb.matrix;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogRuntime;

public class Behavior implements StartupListener
{
	private final FogRuntime runtime;
	

	public Behavior(FogRuntime runtime) {
		this.runtime = runtime;		
	}

	@Override
	public void startup() {
		
		System.out.println("hello world");
		
		runtime.shutdownRuntime();
	}

}
