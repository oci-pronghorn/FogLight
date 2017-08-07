package com.ociweb.grove;

import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.AudioStorageDevice.SD_Card;
import static com.ociweb.iot.maker.FogCommandChannel.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.grove.mp3_v2.MP3_V2_Transducer;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.SerialListener;
import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.util.Appendables;


public class MP3Behavior implements Behavior, StartupListener, TimeListener{
	Logger logger = LoggerFactory.getLogger(MP3Behavior.class);
	
	private final MP3_V2_Transducer s;

	public MP3Behavior(FogRuntime rt){
		s = new MP3_V2_Transducer(rt.newCommandChannel(SERIAL_WRITER));//SERIAL_WRITER));
	}

	@Override
	public void startup() {
		//logger.warn("Selecting device {} ",s.selectDevice(SD_Card));
		//logger.warn("Setting volume {}",s.setVolume(28));
		//logger.warn("Playing song in MP3 folder {}" , s.playSongInMP3Folder(0001));
	//	logger.warn("Resuming {}" ,s.resume());
		
	}

	@Override
	public void timeEvent(long time, int iteration) {
		s.sendOnes();
		s.sendZeros();
		System.out.println("Sending 0s and 1s to try and get this to blink :(");
	}

	//	@Override
	//	public int message(BlobReader reader) {
	//		System.out.println("Reading serial messages");
	//		if (reader.available() > 0){
	//			int consumed = 0;
	//			while (reader.hasRemainingBytes()){
	//				System.out.print("Input:" + Integer.toHexString( reader.read()));
	//				consumed++;
	//			}
	//			System.out.println();
	//			return consumed;
	//		}
	//		return 0;
	//	}



}
