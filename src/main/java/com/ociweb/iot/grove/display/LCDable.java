package com.ociweb.iot.grove.display;

public interface LCDable extends Seven_Segmentable {
	public void switchPixel(boolean on, int row, int col);
	
}
