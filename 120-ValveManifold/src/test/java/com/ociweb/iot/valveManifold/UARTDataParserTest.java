package com.ociweb.iot.valveManifold;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.iot.valveManifold.schema.ValveSchema;
import com.ociweb.pronghorn.util.TrieParser;
import com.ociweb.pronghorn.util.TrieParserReader;
import static com.ociweb.iot.valveManifold.ValveDataParserStage.*;

public class UARTDataParserTest {



	
	
	
	@Test
	public void doTest() {
		
		TrieParser trie = ValveDataParserStage.buildParser();
		
		//System.out.println(trie);		
		
		
		String example = "[st1sn100100pn\"NX-DCV-SM-BLU-1-1-VO-L1-SO-OO\"lr-100cc184587lf0pf\"L\"vf0sp80]";
		byte[] exampleBytes = example.getBytes();		
		
		TrieParserReader reader = new TrieParserReader(10);
		
		TrieParserReader.parseSetup(reader, exampleBytes, 0, exampleBytes.length, Integer.MAX_VALUE);
		
		int actualStartId = (int)TrieParserReader.parseNext(reader, trie);
		assertEquals(DATA_START, actualStartId);
		
		assertEquals(1, TrieParserReader.capturedLongField(reader, 0));
		
		assertEquals(ValveSchema.MSG_VALVESERIALNUMBERMESSAGE_311, (int)TrieParserReader.parseNext(reader, trie));
		
		assertEquals(100100, TrieParserReader.capturedLongField(reader, 0));
				
		assertEquals(ValveSchema.MSG_PARTNUMBERMESSAGE_330, (int)TrieParserReader.parseNext(reader, trie));
		
		StringBuilder target = new StringBuilder();
		TrieParserReader.capturedFieldBytesAsUTF8(reader, 0, target);
		assertEquals("NX-DCV-SM-BLU-1-1-VO-L1-SO-OO",target.toString());
		
		assertEquals(ValveSchema.MSG_RESIDUALOFDYNAMICANALYSISMESSAGE_318, (int)TrieParserReader.parseNext(reader, trie));
		
		assertEquals(-100, TrieParserReader.capturedLongField(reader, 0));
		
		assertEquals(ValveSchema.MSG_LIFECYCLECOUNTMESSAGE_312, (int)TrieParserReader.parseNext(reader, trie));
		assertEquals(184587, TrieParserReader.capturedLongField(reader, 0));
				
		assertEquals(ValveSchema.MSG_LEAKFAULT_FALSE_360, (int)TrieParserReader.parseNext(reader, trie));		
		assertEquals(ValveSchema.MSG_PRESSUREFAULT_LOW_350, (int)TrieParserReader.parseNext(reader, trie));
		assertEquals(ValveSchema.MSG_VALUEFAULT_FALSE_340, (int)TrieParserReader.parseNext(reader, trie));
		
		assertEquals(ValveSchema.MSG_SUPPLYPRESSUREMESSAGE_313, (int)TrieParserReader.parseNext(reader, trie));
		assertEquals(80, TrieParserReader.capturedLongField(reader, 0));
		
		assertEquals(DATA_END, (int)TrieParserReader.parseNext(reader, trie));
		
		assertEquals(-1, (int)TrieParserReader.parseNext(reader, trie));
		
		
	}
	
	
}
