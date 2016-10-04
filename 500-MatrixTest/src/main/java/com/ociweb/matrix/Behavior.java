package com.ociweb.matrix;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.StartupListener;

public class Behavior implements StartupListener
{
	private final DeviceRuntime runtime;
	

	public Behavior(DeviceRuntime runtime) {
		this.runtime = runtime;		
	}

	@Override
	public void startup() {
		
		System.out.println("hello world");
		
		runtime.shutdownDevice();
	}

}
