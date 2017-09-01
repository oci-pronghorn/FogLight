package com.ociweb.iot.hardware.impl.grovepi;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.util.TrieParser;
import com.ociweb.pronghorn.util.TrieParserReader;

public enum PiModel {

	Unknown(0,null,-1, null),
	ModelBRev1(256,"/dev/ttyAMA0",1,null,"0002","0003"),
	ModelBRev2_256M(256,"/dev/ttyAMA0", 1,null,"0004","0005","0006"),
	ModelA(256,"/dev/ttyAMA0", 1,null,"0007","0008","0009"),
	ModelBRev2_512M(512,"/dev/ttyAMA0", 1,null,"000d","000e","000f"),
	ModelBPlus(512,"/dev/ttyAMA0", 1, null,"0010","0013","900032"),
	ComputeModule(512,"/dev/ttyAMA0",1, null,"0011","0014"),
	ModelAPlus_256M(256,"/dev/ttyAMA0", 1, null,"0012","0015"),	
	ModelAPlus_512M(512,"/dev/ttyAMA0",1, null,"0015"),
	
	Pi2ModelBv1_1(1024,"/dev/ttyAMA0",1, null,"a01041","a21041"),
	Pi2ModelBv1_2(1024,"/dev/ttyAMA0", 1,null,"a22042"),
	
	PiZerov1_2(512,"/dev/ttyAMA0", 1, null,"900092"),
	PiZerov1_3(512,"/dev/ttyAMA0",1, null,"900093"),
	
	PiZeroW(512,"/dev/ttyS0", 1, "/dev/ttyAMA0","0x9000c1", "9000c1"), //PiZeroW uses the 0th bus
	Pi3ModelB(1024,"/dev/ttyS0", 1, "/dev/ttyAMA0","a02082","a22082");
	
	private final int mb;
	private final String[] revisionCodes;
	private final int i2cBus;
	private final String serialDevice;
	private final String bluetoothDevice;
		
	private static final Logger logger  = LoggerFactory.getLogger(PiModel.class);
	
	private PiModel(int mb,
			        String serial, 
			        int i2cBus,
			        String bt, 
			        String ... revisions) {
		this.i2cBus = i2cBus;
		this.mb = mb;
		this.serialDevice = serial;
	    this.bluetoothDevice = bt;
		this.revisionCodes = revisions;
	}
	public int i2cBus(){
		return i2cBus;
	}
	public int mb(){
		return mb;
	}
	public String serialDevice() {
		return serialDevice;
	}
	public String bluetoothDevice() {
		return bluetoothDevice;
	}
	
	static TrieParser trie = new TrieParser(256,false);
	static TrieParserReader reader = new TrieParserReader(2,true);
	static {
		trie.setUTF8Value("Revision	: %b\n", 1);
		trie.setUTF8Value("%b: %b\n"       , 2);
		trie.setUTF8Value("\n", 3);	
	}
	
	public static synchronized PiModel detect() {
		
		long start = System.currentTimeMillis();
		try {
			
			//TODO: this is not GC free, should be updated to use a pipe		
			byte[] buffer = new byte[1<<16]; //enough to get the revision		
			
				
			int lastPos = 0;
			try {
				Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
						    			
				InputStream stream = process.getInputStream();
				int len = 0;
				do {
					len = stream.read(buffer, lastPos, buffer.length-lastPos);
					if (len>=0) {
						lastPos+=len;
					} else {
						break;
					}
				} while (lastPos!=buffer.length);
				
				//System.out.println("DETECT DATA\n"+new String(buffer,0,lastPos));
				
				
			} catch (Exception e) {
				//logger.trace("unable to detect model.",e);
				return Unknown;
			}
			
			TrieParserReader.parseSetup(reader, buffer, 0, lastPos, buffer.length-1);
			
			int token;
			do {
				token = (int)reader.parseNext(reader, trie);		
			} while (token!=1 && token!=-1);
			
			if (1==token) {
				StringBuilder value = reader.capturedFieldBytesAsUTF8(reader, 0, new StringBuilder());
				
				PiModel[] all = PiModel.values();
				int i = all.length;
				while (--i >= 0) {		
					String[] codes = all[i].revisionCodes;
					int j = codes.length;
					while (--j >= 0) {		
						if (value.indexOf(codes[j])>=0) {
							return all[i];
						}
					}
				}
			}
			return Unknown;
		} finally {
			logger.info("pi detection duration {} ", System.currentTimeMillis()-start);
		}
	}
}
