package com.ociweb.iot.grove.mp3_v2;

import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.*;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.AudioStorageDevice.*;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.maker.SerialWritable;
import com.ociweb.iot.transducer.SerialListenerTransducer;
import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.pipe.BlobWriter;

public class MP3_V2_Transducer implements SerialListenerTransducer, IODeviceTransducer {
	private final FogCommandChannel ch;
	private int[] output_array = 
		{		
				msgStartByte,
				versionInfoByte,
				6,
				0x00,
				0x00,
				0x00,
				0x00,
				msgStopByte		
		};
	
	private SerialWritable serialWriter = new SerialWritable() {
		@Override
		public void write(BlobWriter writer){
			for (int b: output_array){
				writer.writeByte(b);	
				System.out.print("0x " + Integer.toHexString(b) + " ");
			}
			System.out.println();//RAY's PRINTING
		}
	};
	
	
	public MP3_V2_Transducer(FogCommandChannel ch){
		this.ch = ch;
		this.ch.ensureSerialWriting();		
		
	}

	@Override
	public int message(BlobReader reader) {
		return 0;
	}
	
	public boolean  nextSong(){
		output_array[3] = playNextSong;
		output_array[4] = 0x00;
		output_array[5] = 0x00;
		output_array[6] = 0x00;
		return ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	
	public boolean  prevSong(){
		output_array[3] = playPreviousSong;
		output_array[4] = 0x00;
		output_array[5] = 0x00;
		output_array[6] = 0x00;
		return  ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	public boolean sendZeros(){
		output_array[0] = 0;
		output_array[1] = 0;
		output_array[2] = 0;
		output_array[3] = 0;
		output_array[4] = 0;
		output_array[5] = 0;
		output_array[6] = 0;
		output_array[7] = 0;
		return  ch.publishSerial(serialWriter) && ch.block(20_000_000L);
		
	}
	public boolean specifyMusic(int index){
		output_array[3] = playSpecificSong;
		output_array[4] = 0x00;	
		output_array[5] = (index>> 8) & 0xFF; 
		output_array[6] = index & 0xFF; 
		return  ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	
	public boolean playSongInMP3Folder(int index){
		output_array[3] = specifySongInMP3Folder;
		output_array[4] = 0x00;	
		output_array[5] = (index>> 8) & 0xFF; 
		output_array[6] = index & 0xFF; 
		return ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	public boolean pause(){
		output_array[3] = pauseSong;
		output_array[4] = 0x00;	
		output_array[5] = 0x00;
		output_array[6] = 0x00;
		return ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	
	public boolean resume(){
		output_array[3] = playSong;
		output_array[4] = 0x00;	
		output_array[5] = 0x00;
		output_array[6] = 0x00;
		return  ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	
	public boolean volumeUp(){
		output_array[3] = turnVolumeUp;
		output_array[4] = 0x00;	
		output_array[5] = 0x00;
		output_array[6] = 0x00;
		return  ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	
	public boolean volumeDown(){
		output_array[3] = turnVolumeDown;
		output_array[4] = 0x00;	
		output_array[5] = 0x00;
		output_array[6] = 0x00;
		return  ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	
	public boolean setVolume(int vol){
		output_array[3] = specificVolume;
		output_array[4] = 0x00;	
		output_array[5] = 0x00;
		output_array[6] = vol;
		return  ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	
	public boolean specifyFolderToPlay(int folder, int songIndex){
		output_array[3] =  specifyDirectory;
		output_array[4] = 0x00;
		output_array[5] = folder;
		output_array[6] = songIndex;
		return  ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	
	public boolean selectDevice(AudioStorageDevice a){
		output_array[3] = chooseSpecificPlaybackDevice;
		output_array[4] = 0x00;
		output_array[5] = 0x00;
		output_array[6] = a.val;
		return  ch.publishSerial(serialWriter) && ch.block(200_000_000L);//delay 200ms for the hardwire to choose th device, as per data sheet's instruction.
	}
	
	public boolean startLooping(){
		output_array[3] = loopAllMusic;
		output_array[4] = 0x00;
		output_array[5] = 0x00;
		output_array[6] = 0x01; //start
		return  ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
	
	public boolean stopLooping(){
		output_array[3] = loopAllMusic;
		output_array[4] = 0x00;
		output_array[5] = 0x00;
		output_array[6] = 0x00; //top
		return  ch.publishSerial(serialWriter) && ch.block(20_000_000L);
	}
}
