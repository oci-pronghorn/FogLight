package com.ociweb.iot.impl;

public interface LocationListenerBase {

	boolean location(int location, long oddsOfRightLocation, long totalSum);

}
