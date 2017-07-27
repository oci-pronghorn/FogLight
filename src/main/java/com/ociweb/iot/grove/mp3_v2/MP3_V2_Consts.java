package com.ociweb.iot.grove.mp3_v2;

public class MP3_V2_Consts {
	
	//Message Format：$S VER Length Command Feedback para1 para2 $o
	final public static int msgStartByte = 0x7E; // '$S'
	final public static int versionInfoByte = 0xFF; //The default version
	final public static int msgStopByte = 0xEF; //'$O'
	
	//Commands
	final public static int playNextSong = 0x01; //needs dataH (0x00), and dataL afterwards (0x00)
	final public static int playPreviousSong = 0x02; //needs dataH (0x00), and dataL afterwards (0x00)
	final public static int playSpecificSong = 0x03; //needs to send songID afterwards
	final public static int turnVolumeUp = 0x04; //needs dataH (0x00), and dataL afterwards (0x00)
	final public static int turnVolumeDown = 0x05; //needs dataH (0x00), and dataL afterwards (0x00)
	final public static int specificVolume = 0x06; //needs dataH (0x00), and dataL afterwards (0x00 ~ 0x1E)
	final public static int chooseEqualizer = 0x07;//needs a dataH(0x00), and dataL afterwards (an enum byte of the 6 presets)
	final public static int repeatCurrentTrack = 0x08; //needs a dataH(0x00)
	final public static int chooseSpecificPlaybackDevice = 0x09; // //needs a dataH(0x00), and a dataL (0x02) <-- microSD
	final public static int enterStandByMode = 0x0A; // needs a dataH (0x00) and a dataL (0x00)
	final public static int resetChip = 0x0C;  // needs a dataH (0x00) and a dataL (0x00
	final public static int playSong = 0x0D;  // needs a dataH (0x00) and a dataL (0x00);
	final public static int pauseSong = 0x0E;  // needs a dataH (0x00) and a dataL (0x00)
	final public static int specifyDirectory = 0x0F;
	final public static int loopAllMusic = 0x11; //needs a dataH (0x00) and a dataL (0x00 for stop, 0x01 for start looping)
	final public static int specifySongInMP3Folder = 0x12;
	final public static int insertAndPlayAnotherTrack = 0x13;
	final public static int specifyFileInsideFolder = 0x14;
	final public static int stopInsertedSongAndGoBackToOriginalTrackPlaying = 0x15;  // needs a dataH (0x00) and a dataL (0x00)
	final public static int stopPlayingMusic = 0x16;  // needs a dataH (0x00) and a dataL (0x00)
	final public static int specifyFolderForLooping = 0x17;
	final public static int repeatSingleTrack = 0x19;  // needs a dataH (0x00) and a dataL (0x00)
	final public static int specifyFolderToShuffle = 0x28;  // needs a dataH (0x00) and a dataL (0x00)

	public enum EQ {
		Normal(0x00),
		POP(0x01),
		ROCK(0x02),
		JAZZ(0x03),
		CLASSIC(0x04),
		BASS(0x05);
		
		final public int val;
		private EQ(int val){
			this.val = val;
		}
	}
	public enum AudioStorageDevice {
		SD_Card(0x02),
		DISK(0x01);
		public final int val;
		private AudioStorageDevice(int val){
			this.val = val;
		}
	}
}
