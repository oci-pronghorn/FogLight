package com.ociweb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;

public class FogLight {
	
	public static void main(String[] args) {
		
		InputStream stream = Thread.currentThread()
		      .getContextClassLoader()
		      .getResourceAsStream("FogLight.props");
		if (null==stream) {
			System.err.println("unable to launch FogLight, can not find launch properties resource");
		} else {
			
			try {
				Properties p = new Properties();
				p.load(stream);

				String className = p.getProperty("main");
				if (null==className) {
					System.err.println("unable to launch FogLight, can not find main in launch properties resource");
				} else {
				
					try {
						
						Class<?> clazz = Class.forName(className);					
						DeviceRuntime.run((IoTSetup) clazz.newInstance(),args);
						
					} catch (ClassNotFoundException e) {
						System.err.println("unable to launch FogLight, can not find class listed in launch properties resource");
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				
			} catch (IOException e) {
				System.err.println("unable to launch FogLight, can not read launch properties resource");
				e.printStackTrace();
			}
			
			
		}
		
	}

}
