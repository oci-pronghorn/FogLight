package com.ociweb.iot.hardware.impl.grovepi;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.util.TrieParser;
import com.ociweb.pronghorn.util.TrieParserReader;

public enum MacModel {

	Unknown(),
	Mac();
		

	private static final Logger logger  = LoggerFactory.getLogger(MacModel.class);
	
	static TrieParser trie = new TrieParser(256,false);
	static TrieParserReader reader = new TrieParserReader(2,true);
	static {
		//Any UTF8 value with a 1 is a string that will be looked for by the parse reader,
		//any value with 2 will be skipped over by the parse reader
		trie.setUTF8Value("%bi3%b\n", 1);
		trie.setUTF8Value("%bi5%b\n", 1);
		trie.setUTF8Value("%bi7%b\n", 1);
		trie.setUTF8Value("%bi9%b\n", 1);
		trie.setUTF8Value("%b: %b\n"       , 2);
		trie.setUTF8Value("%b\n", 3);		
	}
	
	public static synchronized MacModel detect() {
		
		long start = System.currentTimeMillis();
		try {
			
			//TODO: this is not GC free, should be updated to use a pipe		
			byte[] buffer = new byte[1<<16]; //enough to get the revision		
			
				
			int lastPos = 0;
			try {
				Process process = Runtime.getRuntime().exec("sysctl -n machdep.cpu.brand_string");
						    			
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
				
				

			} catch (Throwable e) {
				//logger.trace("unable to detect model.",e);
				return Unknown;
			}
			
			TrieParserReader.parseSetup(reader, buffer, 0, lastPos, buffer.length-1);
			
			int token;
			do {
				token = (int)reader.parseNext(reader, trie);		
			} while (token!=1 && token!=-1);
			if (1==token) {
				return Mac;
			}
			
			return Unknown;
		} finally {
			logger.info("mac detection duration {} ", System.currentTimeMillis()-start);
		}
	}
}