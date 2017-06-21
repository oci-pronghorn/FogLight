package com.ocweb.grove;

import com.ociweb.iot.maker.I2CListener;

public class XYZReader implements I2CListener {

	private final XYZListener listener;
	
	public XYZReader(XYZListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void i2cEvent(int addr, int register, long time, 
			             byte[] backing, int position, int length,
			             int mask) {
		
		//we read our x, y z values
		
		listener.setCoord(3, 4, 5);

		
	}
}
