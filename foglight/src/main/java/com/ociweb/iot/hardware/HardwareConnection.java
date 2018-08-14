package com.ociweb.iot.hardware;

public class HardwareConnection {

	public final IODevice twig;
	public final int responseMS;
    public final int movingAverageWindowMS;
    public final boolean sendEveryValue;
    public final byte register;

    static final int DEFAULT_AVERAGE_WINDOW_MS = 1000;
	static final int UNKOWN_REGISTER = -1;

	public HardwareConnection(IODevice twig, int register, int pullRateMS, int movingAverageWindowMS, boolean sendEveryValue) {
		this.twig = twig;
		this.responseMS = pullRateMS;
		this.movingAverageWindowMS = movingAverageWindowMS;
		this.sendEveryValue = sendEveryValue;
		this.register = (byte)register;
	}

    public HardwareConnection(IODevice twig, int connection) {
    	this(twig, connection, twig.defaultPullRateMS(), DEFAULT_AVERAGE_WINDOW_MS, false);
    }
}
