package com.ociweb.pronghorn.iot.rs232;

public interface RS232Clientable {

	/**
	 * 
	 * @param array first array with which the input data is to be populated in.
	 * @param position the index of the first array where data begin to be populated at.
	 * @param remaining 
	 * @param array2 second array with which the input data is to be populated in.
	 * @param position2  the index of the second array where data begin to be populated at.
	 * @param remaining2

	 */
	int readInto(byte[] array, int position, int remaining, byte[] array2, int position2, int remaining2);

	int writeFrom(byte[] backing, int pos, int length);

	int write(byte[] data);

}
