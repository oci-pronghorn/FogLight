package com.ociweb.iot.grove.mp3_v2;

import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.chooseSpecificPlaybackDevice;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.loopAllMusic;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.msgStartByte;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.msgStopByte;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.pauseSong;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.playNextSong;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.playPreviousSong;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.playSong;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.playSpecificSong;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.specificVolume;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.specifyDirectory;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.specifySongInMP3Folder;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.turnVolumeDown;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.turnVolumeUp;
import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.versionInfoByte;

import com.ociweb.gl.api.Writable;
import com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.AudioStorageDevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.transducer.SerialListenerTransducer;
import com.ociweb.pronghorn.pipe.ChannelReader;
import com.ociweb.pronghorn.pipe.ChannelWriter;


/**
 * 
 * @author Ray Lo, raylo@wustl.edu
 *
 */
public class MP3_V2_Transducer implements SerialListenerTransducer, IODeviceTransducer {
	private final FogCommandChannel ch;
	private int testByte = 0x00;
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
	
	private Writable serialWriter = new Writable() {
		@Override
		public void write(ChannelWriter writer){
			for (int b: output_array){
				writer.writeByte(b);	
				System.out.print("0x" + Integer.toHexString(b) + " ");
			}
			System.out.println();//RAY's PRINTING
		}
	};
	
	private Writable testWriter = new Writable() {
		@Override
		public void write(ChannelWriter writer){
			for (int i = 0; i < 500; i ++){
				writer.writeByte(testByte);	
				System.out.println("Test byte: " + Integer.toHexString(testByte));		
			}		
		}
	};
	
	
	public MP3_V2_Transducer(FogCommandChannel ch){
		this.ch = ch;
		this.ch.ensureSerialWriting();		
		
	}

	@Override
	public int message(ChannelReader reader) {
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
		testByte = 0x00;
		return  ch.publishSerial(testWriter);
		
	}
	
	public boolean sendOnes(){
		testByte = 0xFF;
		return  ch.publishSerial(testWriter);
		
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
