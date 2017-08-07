package com.ociweb.grove;

import com.ociweb.iot.maker.SerialListener;
import com.ociweb.pronghorn.pipe.BlobReader;

public class MonitoringBehavior implements SerialListener {

	@Override
	public int message(BlobReader reader) {
		if (reader.available() > 0){
			int consumed = 0;
			while (reader.hasRemainingBytes()){
				System.out.print("Input:" + Integer.toHexString( reader.read()));
				consumed++;
			}
			System.out.println();
			return consumed;
		}
		return 0;
	}
	
}
