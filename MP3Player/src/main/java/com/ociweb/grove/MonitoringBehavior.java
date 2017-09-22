package com.ociweb.grove;

import com.ociweb.iot.maker.SerialListener;
import com.ociweb.pronghorn.pipe.ChannelReader;

public class MonitoringBehavior implements SerialListener {

	@Override
	public int message(ChannelReader reader) {
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
