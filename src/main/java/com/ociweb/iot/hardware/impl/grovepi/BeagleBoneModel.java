package com.ociweb.iot.hardware.impl.grovepi;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.util.TrieParser;
import com.ociweb.pronghorn.util.TrieParserReader;

public enum BeagleBoneModel {

	Unknown((String)null),
	Black("Black"),
	Green("Green"),
	GreenWireless("Green Wireless");

	private final String[] product;

	private static final Logger logger = LoggerFactory.getLogger(BeagleBoneModel.class);

	private BeagleBoneModel(String... product) {
		this.product = product;
	}

	static TrieParser trie = new TrieParser(256, false);
	static TrieParserReader reader = new TrieParserReader(2, true);
	static {
		trie.setUTF8Value("    product: TI AM335x BeagleBone %b\n", 1);
		trie.setUTF8Value("%b\n",2);
		trie.setUTF8Value("\n",2);
	}

	public static synchronized BeagleBoneModel detect() {

		long start = System.currentTimeMillis();
		try {

			// TODO: this is not GC free, should be updated to use a pipe
			byte[] buffer = new byte[1 << 16]; // enough to get the revision

			int lastPos = 0;
			try {
				Process process = Runtime.getRuntime().exec("lshw");

				InputStream stream = process.getInputStream();
				int len = 0;
				do {
					len = stream.read(buffer, lastPos, buffer.length - lastPos);
					if (len >= 0) {
						lastPos += len;
					} else {
						break;
					}
				} while (lastPos != buffer.length);

				//System.out.println("DETECT DATA\n"+new String(buffer,0,lastPos));
				 
			} catch (Exception e) {
				//logger.trace("unable to detect model.", e);
				return Unknown;
			}

			TrieParserReader.parseSetup(reader, buffer, 0, lastPos, buffer.length - 1);

			int token;
			do {
				token = (int) TrieParserReader.parseNext(reader, trie);
			} while (token != 1 && token != -1);
			if (1 == token) {
				StringBuilder value = TrieParserReader.capturedFieldBytesAsUTF8(reader, 0, new StringBuilder());
				if (value.indexOf(Black.product[0]) >= 0) {
					return Black;
				} else if(value.indexOf(GreenWireless.product[0]) >= 0){
					return GreenWireless;
				} else if(value.indexOf(Green.product[0]) >= 0){
					return Green;
				}
			}
			return Unknown;
		} finally {
			logger.info("beaglebone detection duration {} ", System.currentTimeMillis() - start);
		}
	}
}