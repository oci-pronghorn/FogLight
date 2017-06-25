package com.ociweb.iot.maker;

public enum Baud {

    // Standard baud rates.
    B_____9600(13),
    B____19200(14),
    B____38400(15),
    B____57600(4097),
    B___115200(4098),
    B___230400(4099),
    B___460800(4100),
    B___500000(4101),
    B___576000(4102),
    B___921600(4103),
    B__1000000(4104),
    B__1152000(4105),
    B__1500000(4106),
    B__2000000(4107),
    B__2500000(4108),
    B__3000000(4109),
    B__3500000(4110),
    B__4000000(4111);
    
	private int code;
	
	private Baud(int code) {
		this.code = code;
	}
	
	public int code() {
		return code;
	}
}
