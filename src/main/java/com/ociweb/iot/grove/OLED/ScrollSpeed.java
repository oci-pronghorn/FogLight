package com.ociweb.iot.grove.OLED;

public enum ScrollSpeed{
	Scroll_2Frames(0x07),
	Scroll_3Frames(0x04),
	Scroll_4Frames(0x05),
	Scroll_5Frames(0x00),
	Scroll_25Frames(0x06),
	Scroll_64Frames(0x01),
	Scroll_128Frames(0x02),
	Scroll_256Frames(0x03);

	public final int COMMAND;
	private ScrollSpeed(int command){ 
		this.COMMAND = command;
	};

};

