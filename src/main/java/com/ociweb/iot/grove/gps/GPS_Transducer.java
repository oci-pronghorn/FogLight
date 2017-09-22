package com.ociweb.iot.grove.gps;

import java.io.IOException;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.transducer.SerialListenerTransducer;
import com.ociweb.pronghorn.pipe.ChannelReader;

public class GPS_Transducer implements SerialListenerTransducer, IODeviceTransducer{
	
	private final FogCommandChannel ch;
	private GeoCoordinateListener l;
	private char[] input;
	public GPS_Transducer(FogCommandChannel ch, GeoCoordinateListener l){
		this.l = l;
		this.ch = ch;
	}
	
	public GPS_Transducer(FogCommandChannel ch){
		this.ch = ch;
	}

	@Override
	public int message(ChannelReader reader) {	
		System.out.println("SerialReader's message function is being triggered");
		int index = 0;
		while (reader.hasRemainingBytes()){
			try {
				input[index++] = reader.readChar();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		printMsg(index);
		l.coordinates(1, 2);
		return index;
	}
	
	private void printMsg(int length){
		for (int i = 0; i < length; i ++){
			System.out.print(input[i]);
		}
		System.out.println();
	}
}
