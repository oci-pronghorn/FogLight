package com.ociweb.pronghorn.iot.rs232;

public interface RS232Clientable {

	int readInto(byte[] array, int position, int remaining, byte[] array2, int position2, int remaining2);

	int writeFrom(byte[] backing, int pos, int length);

}
