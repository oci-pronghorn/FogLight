package com.ociweb.iot.valveManifold;

import static com.ociweb.iot.valveManifold.ValveDataParserStage.DATA_END;
import static com.ociweb.iot.valveManifold.ValveDataParserStage.DATA_START;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import com.ociweb.iot.valveManifold.schema.ValveSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.monitor.PipeMonitorCollectorStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;
import com.ociweb.pronghorn.util.TrieParser;
import com.ociweb.pronghorn.util.TrieParserReader;

public class UARTDataParserTest {


	@Test
	public void coreParserTest() {
		
		TrieParser trie = ValveDataParserStage.buildParser();
		
		//System.out.println(trie);		
		
		
		String example = "[st1sn100100pn\"NX-DCV-SM-BLU-1-1-VO-L1-SO-OO\"lr-100cc184587lf0pf\"L\"vf0sp80]";
		byte[] exampleBytes = example.getBytes();		
		
		TrieParserReader reader = new TrieParserReader(10);
		
		TrieParserReader.parseSetup(reader, exampleBytes, 0, exampleBytes.length, Integer.MAX_VALUE);
		
		int actualStartId = (int)TrieParserReader.parseNext(reader, trie);
		assertEquals(DATA_START, actualStartId);
		
		assertEquals(1, TrieParserReader.capturedLongField(reader, 0));
		
		assertEquals(ValveSchema.MSG_VALVESERIALNUMBER_311, (int)TrieParserReader.parseNext(reader, trie));
		
		assertEquals(100100, TrieParserReader.capturedLongField(reader, 0));
				
		assertEquals(ValveSchema.MSG_PARTNUMBER_330, (int)TrieParserReader.parseNext(reader, trie));
		
		StringBuilder target = new StringBuilder();
		TrieParserReader.capturedFieldBytesAsUTF8(reader, 0, target);
		assertEquals("NX-DCV-SM-BLU-1-1-VO-L1-SO-OO",target.toString());
		
		assertEquals(ValveSchema.MSG_RESIDUALOFDYNAMICANALYSIS_318, (int)TrieParserReader.parseNext(reader, trie));
		
		assertEquals(-100, TrieParserReader.capturedLongField(reader, 0));
		
		assertEquals(ValveSchema.MSG_LIFECYCLECOUNT_312, (int)TrieParserReader.parseNext(reader, trie));
		assertEquals(184587, TrieParserReader.capturedLongField(reader, 0));
				
		assertEquals(ValveSchema.MSG_LEAKFAULT_360, (int)TrieParserReader.parseNext(reader, trie));
		assertEquals(ValveSchema.MSG_PRESSUREFAULT_350, (int)TrieParserReader.parseNext(reader, trie));
		assertEquals(ValveSchema.MSG_VALVEFAULT_340, (int)TrieParserReader.parseNext(reader, trie));
		
		assertEquals(ValveSchema.MSG_SUPPLYPRESSURE_313, (int)TrieParserReader.parseNext(reader, trie));
		assertEquals(80, TrieParserReader.capturedLongField(reader, 0));
		
		assertEquals(DATA_END, (int)TrieParserReader.parseNext(reader, trie));
		
		assertEquals(-1, (int)TrieParserReader.parseNext(reader, trie));
		
		
	}

	@Test
	public void ParserStageTest() {
		
		GraphManager gm = new GraphManager();
		
		Pipe<RawDataSchema> input = RawDataSchema.instance.newPipe(4, 512);
		Pipe<ValveSchema> filter = ValveSchema.instance.newPipe(64, 128);
		Pipe<ValveSchema> output = ValveSchema.instance.newPipe(64, 128);

		ByteArrayOutputStream results = new ByteArrayOutputStream();
		
		ValveDataParserStage.newInstance(gm, input, filter);
		FilterStage.newInstance(gm, filter, output);
		ConsoleJSONDumpStage.newInstance(gm, output, new PrintStream(results));
		
		//MonitorConsoleStage.attach(gm);
		
		NonThreadScheduler scheduler = new NonThreadScheduler(gm);
		
		scheduler.startup();
		
		//////////////////
		////setup the test data
		//////////////////
		Pipe.addMsgIdx(input, RawDataSchema.MSG_CHUNKEDSTREAM_1);

		String message =
				"587lf5pf\"L\"vf0sp55] [" +
				"st1" +
				"sn100100" +
				"pn\"NX-DCV-SM-BLU-1-1-VO-L1-SO-OO\"" +
				"lr-100" +
				"cc184587" +
				"lf0" +
				"pf\"L\"" +
				"vf0" +
				"sp80" +
				"vf0" +
				"sp42" +
				"]";

		Pipe.addUTF8(message, input);

		Pipe.confirmLowLevelWrite(input, Pipe.sizeOf(input, RawDataSchema.MSG_CHUNKEDSTREAM_1));
		Pipe.publishWrites(input);

		//////////////////		
		
		int i = 1000;
		while (--i>=0) {
			scheduler.run();
		}
		
		scheduler.shutdown();
				
		
		////////////////
		///confirm the results
		////////////////
		
		String stringResults = new String(results.toByteArray()); 
		
		//for debug to inspect the values
		System.err.println(stringResults);
		
		assertFalse(stringResults.contains("55"));
		assertTrue(stringResults.contains("{\"PartNumber\":\"NX-DCV-SM-BLU-1-1-VO-L1-SO-OO\"}"));
		assertTrue(stringResults.contains("{\"ResidualOfDynamicAnalysis\":4294967196}"));

		int firstSP =  stringResults.indexOf("{\"SupplyPressure\":80}");
		assertTrue(firstSP != -1);
		int secondSP =  stringResults.indexOf("{\"SupplyPressure\":42}", firstSP + 1);
		assertTrue(secondSP != -1);

		int firstVP =  stringResults.indexOf("{\"ValveFault\":0}");
		assertTrue(firstVP != -1);
		int secondVP =  stringResults.indexOf("{\"ValveFault\":0}", firstVP + 1);
		assertTrue(secondVP == -1);
		
	}
	

	
}
