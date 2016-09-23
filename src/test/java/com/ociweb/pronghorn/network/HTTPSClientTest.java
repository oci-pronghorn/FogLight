package com.ociweb.pronghorn.network;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.pronghorn.iot.HTTPClientRequestStage;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.util.hash.IntHashTable;
import com.ociweb.pronghorn.schema.ClientNetRequestSchema;
import com.ociweb.pronghorn.schema.ClientNetResponseSchema;
import com.ociweb.pronghorn.schema.NetParseAckSchema;
import com.ociweb.pronghorn.schema.NetRequestSchema;
import com.ociweb.pronghorn.schema.NetResponseSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.monitor.MonitorConsoleStage;
import com.ociweb.pronghorn.stage.network.config.HTTPSpecification;
import com.ociweb.pronghorn.stage.route.SplitterStage;
import com.ociweb.pronghorn.stage.scheduling.FixedThreadsScheduler;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.stream.ToOutputStreamStage;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;
import com.ociweb.pronghorn.stage.test.PipeCleanerStage;

public class HTTPSClientTest {

	Logger log = LoggerFactory.getLogger(HTTPSClientTest.class);
	
	
	@Ignore
	public void baselinetTest() {
		
		byte[] buffer = new byte[1024];
		try {
			long runStart = System.currentTimeMillis();
			
			StringBuilder builder = new StringBuilder();
			
			int testSize = 100;
			int iterations = testSize;
			
			while (--iterations>=0) {
				//long start = System.currentTimeMillis();
				URL u = new URL("https://encrypted.google.com");
				
				InputStream in = u.openStream();
				
				int temp = 0;
				do {
				  temp = in.read(buffer);
				
				  if (temp>0) {
					  builder.append(new String(buffer,0,temp));
				  }
				  
				} while (temp!=-1);
				in.close();
				//long duration = System.currentTimeMillis()-start;
				//System.out.println("blocking duration: "+duration);
			}
			
			long avgDuration = (System.currentTimeMillis()-runStart)/testSize;
			
			System.out.println("total bytes "+builder.length()+"   per msg   "+avgDuration+" ms"); //57  54 50
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * WARNING: integration test that requires access to www.google.com
	 */
	@Ignore
	public void HTTPClientGoogleIntegrationTest() {
				
		
		GraphManager gm = new GraphManager();		
		HardwareImpl hardware = new TestHardware(gm);
		
		int inputsCount = 2;
		int base2SimultaniousConnections = 3;
		int outputsCount = 2;//must be < connections
		int maxPartialResponses = 2;
		int maxListeners = 1<<base2SimultaniousConnections;
		
		GraphManager.addDefaultNota(gm, GraphManager.SCHEDULE_RATE, 20_000);
		
		ClientConnectionManager ccm = new ClientConnectionManager(base2SimultaniousConnections,inputsCount);
		IntHashTable listenerPipeLookup = new IntHashTable(base2SimultaniousConnections+2);
		IntHashTable.setItem(listenerPipeLookup, 42, 0);//put on pipe 0
		
		PipeConfig<NetRequestSchema> netREquestConfig = new PipeConfig<NetRequestSchema>(NetRequestSchema.instance, 2,1<<9);		
		PipeConfig<TrafficReleaseSchema> trafficReleaseConfig = new PipeConfig<TrafficReleaseSchema>(TrafficReleaseSchema.instance, 2);
		PipeConfig<TrafficAckSchema> trafficAckConfig = new PipeConfig<TrafficAckSchema>(TrafficAckSchema.instance, 2);
		PipeConfig<ClientNetRequestSchema> clientNetRequestConfig = new PipeConfig<ClientNetRequestSchema>(ClientNetRequestSchema.instance,4,16000); 
		PipeConfig<NetParseAckSchema> parseAckConfig = new PipeConfig<NetParseAckSchema>(NetParseAckSchema.instance, 2);
		
		PipeConfig<ClientNetResponseSchema> clientNetResponseConfig = new PipeConfig<ClientNetResponseSchema>(ClientNetResponseSchema.instance, 2, 1<<14); 		
		PipeConfig<NetResponseSchema> netResponseConfig = new PipeConfig<NetResponseSchema>(NetResponseSchema.instance, 2, 1<<14);
		
		//holds new requests
		Pipe<NetRequestSchema>[] input = new Pipe[inputsCount];		
		//new requests are not release until this is sent
		Pipe<TrafficReleaseSchema>[] goPipe = new Pipe[inputsCount]; 
		//this is the ack back that the request was sent
		Pipe<TrafficAckSchema>[] ackPipe = new Pipe[inputsCount]; 
		//this is the fully formed request to be wrapped
		Pipe<ClientNetRequestSchema>[] clientRequests = new Pipe[outputsCount];
		Pipe<ClientNetRequestSchema>[] clientRequestsLive = new Pipe[outputsCount];
		Pipe<ClientNetRequestSchema>[] clientRequestsTest = new Pipe[outputsCount];
		
		//this is the encrypted (aka wrapped) fully formed reqests
		Pipe<ClientNetRequestSchema>[] wrappedClientRequests = new Pipe[outputsCount];
		Pipe<ClientNetRequestSchema>[] wrappedClientRequestsLive = new Pipe[outputsCount];
		Pipe<ClientNetRequestSchema>[] wrappedClientRequestsTest = new Pipe[outputsCount];
		

		Pipe<NetParseAckSchema> parseAck = new Pipe<NetParseAckSchema>(parseAckConfig);
		Pipe<ClientNetResponseSchema>[] socketResponse = new Pipe[maxPartialResponses];
		Pipe<ClientNetResponseSchema>[] clearResponse = new Pipe[maxPartialResponses];
		Pipe<ClientNetResponseSchema>[] clearResponseLive = new Pipe[maxPartialResponses];
		Pipe<ClientNetResponseSchema>[] clearResponseTest = new Pipe[maxPartialResponses];
		
		Pipe<NetResponseSchema>[] toReactor = new Pipe[maxListeners];
		
		///////////////////
		//create pipes and dump listeners
		//////////////////
		
		ByteArrayOutputStream contentEncrypted = new ByteArrayOutputStream();
		ByteArrayOutputStream contentAck = new ByteArrayOutputStream();
		ByteArrayOutputStream contentPlain = new ByteArrayOutputStream();
		ByteArrayOutputStream contentResponse = new ByteArrayOutputStream();
		ByteArrayOutputStream finalContent = new ByteArrayOutputStream();
		
		
		int m = maxListeners;
		while (--m>=0) {
			toReactor[m] = new Pipe<NetResponseSchema>(netResponseConfig);
			
			ConsoleJSONDumpStage<NetResponseSchema> dumpC = new ConsoleJSONDumpStage<NetResponseSchema>(gm, toReactor[m], new PrintStream(finalContent));
	
		}
				
		int k = maxPartialResponses;
		while (--k>=0) {
			socketResponse[k] = new Pipe<ClientNetResponseSchema>(clientNetResponseConfig);
			clearResponse[k] = new Pipe<ClientNetResponseSchema>(clientNetResponseConfig);
			clearResponseLive[k] = new Pipe<ClientNetResponseSchema>(clientNetResponseConfig.grow2x());
			clearResponseTest[k] = new Pipe<ClientNetResponseSchema>(clientNetResponseConfig.grow2x());
			
			SplitterStage<ClientNetResponseSchema> requestSplitter = new SplitterStage<ClientNetResponseSchema>(gm, clearResponse[k], clearResponseLive[k], clearResponseTest[k]); 
			
			ConsoleJSONDumpStage<ClientNetResponseSchema> dumpB = new ConsoleJSONDumpStage<ClientNetResponseSchema>(gm, clearResponseTest[k], new PrintStream(contentResponse));

		}
		
		int j = outputsCount;
		while (--j>=0) {
			clientRequests[j] = new Pipe<ClientNetRequestSchema>(clientNetRequestConfig);
			clientRequestsLive[j] = new Pipe<ClientNetRequestSchema>(clientNetRequestConfig.grow2x());
			clientRequestsTest[j] = new Pipe<ClientNetRequestSchema>(clientNetRequestConfig.grow2x());
						
			SplitterStage<ClientNetRequestSchema> requestSplitter = new SplitterStage<ClientNetRequestSchema>(gm, clientRequests[j], clientRequestsLive[j], clientRequestsTest[j]); 
			ConsoleJSONDumpStage<ClientNetRequestSchema> requestDump = new ConsoleJSONDumpStage<ClientNetRequestSchema>(gm, clientRequestsTest[j], new PrintStream(contentPlain));
			
			wrappedClientRequests[j] = new Pipe<ClientNetRequestSchema>(clientNetRequestConfig);
			wrappedClientRequestsLive[j] = new Pipe<ClientNetRequestSchema>(clientNetRequestConfig.grow2x());
			wrappedClientRequestsTest[j] = new Pipe<ClientNetRequestSchema>(clientNetRequestConfig.grow2x());
			
			SplitterStage<ClientNetRequestSchema> encryptedSplitter = new SplitterStage<ClientNetRequestSchema>(gm, wrappedClientRequests[j], wrappedClientRequestsLive[j], wrappedClientRequestsTest[j]); 
			
			ConsoleJSONDumpStage<ClientNetRequestSchema> encryptedDump = new ConsoleJSONDumpStage<ClientNetRequestSchema>(gm, wrappedClientRequestsTest[j], new PrintStream(contentEncrypted));

		}
		
		int i = inputsCount;
		while (--i>=0) {
			ackPipe[i] = new Pipe<TrafficAckSchema>(trafficAckConfig);
			goPipe[i] = new Pipe<TrafficReleaseSchema>(trafficReleaseConfig); 
            input[i] = new Pipe<NetRequestSchema>(netREquestConfig);			
			ConsoleJSONDumpStage<TrafficAckSchema> dump = new ConsoleJSONDumpStage<TrafficAckSchema>(gm, ackPipe[i], new PrintStream(contentAck));
		}
				
		
		///////////////////
		//add the stage under test
		////////////////////
		
		HTTPClientRequestStage requestStage = new HTTPClientRequestStage(gm, hardware, ccm, input, goPipe, ackPipe, clientRequests);
		//splitter is between these two 
		SSLEngineWrapStage wrapStage = new  SSLEngineWrapStage(gm,ccm,clientRequestsLive, wrappedClientRequests );
		//splitter is between these two
		ClientSocketWriterStage socketWriteStage = new ClientSocketWriterStage(gm, ccm, wrappedClientRequestsLive);
		//the data was sent by this stage but the next stage is responsible for responding to the results.
		
		ClientSocketReaderStage socketReaderStage = new ClientSocketReaderStage(gm, ccm, parseAck, socketResponse);
        // 	GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 0, socketReaderStage); //may be required for 10Gb+ connections
				
		//the responding reading data is encrypted so there is not much to be tested
		//we will test after the unwrap
		SSLEngineUnWrapStage unwrapStage = new SSLEngineUnWrapStage(gm, ccm, socketResponse, clearResponse); 
	
		HTTPResponseParserStage parser = new HTTPResponseParserStage(gm, clearResponseLive, toReactor, parseAck, listenerPipeLookup, ccm, HTTPSpecification.defaultSpec());
		

	
		//gm.writeAsDOT(gm, System.out);
		
	 //   MonitorConsoleStage.attach(gm);
		
     	//StageScheduler scheduler = new ThreadPerStageScheduler(gm);
		StageScheduler scheduler = new FixedThreadsScheduler(gm, 8);
		
		
		scheduler.startup();
		System.out.println("done with startup");
		
	    int testIter = 30;
	    
		while(--testIter>=0) {
		
			contentEncrypted.reset();
			contentAck.reset();
			contentPlain.reset();
			contentResponse.reset();
			finalContent.reset();
			
			assertTrue(!(new String(finalContent.toByteArray()).contains("0x3c,0x2f,0x68,0x74,0x6d,0x6c,0x3e")));
			
		long start = System.currentTimeMillis();

			PipeWriter.tryWriteFragment(input[0], NetRequestSchema.MSG_HTTPGET_100);
			PipeWriter.writeUTF8(input[0], NetRequestSchema.MSG_HTTPGET_100_FIELD_HOST_2, "encrypted.google.com");
			PipeWriter.writeInt(input[0], NetRequestSchema.MSG_HTTPGET_100_FIELD_LISTENER_10, 42);
			PipeWriter.writeUTF8(input[0], NetRequestSchema.MSG_HTTPGET_100_FIELD_PATH_3, "/");
			PipeWriter.writeInt(input[0], NetRequestSchema.MSG_HTTPGET_100_FIELD_PORT_1, 443);
			PipeWriter.publishWrites(input[0]);
			
			PipeWriter.tryWriteFragment(goPipe[0], TrafficReleaseSchema.MSG_RELEASE_20);
			PipeWriter.writeInt(goPipe[0], TrafficReleaseSchema.MSG_RELEASE_20_FIELD_COUNT_22, 1);
			PipeWriter.publishWrites(goPipe[0]);
		
			
			int maxCycles = 1000;
			while (--maxCycles>=0 && !(new String(finalContent.toByteArray()).contains("0x3c,0x2f,0x68,0x74,0x6d,0x6c,0x3e"))) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					break;
				}
			}
				
			long duration = System.currentTimeMillis()-start;
			System.err.println("request duration "+duration);
	
			String encryptedActual = new String(contentEncrypted.toByteArray());		
			String ackActual = new String(contentAck.toByteArray());
			String plainActual = new String(contentPlain.toByteArray());
			
			//confirm ack
			assertTrue(HTTPClientRequestStage.class.getName()+" did not finish sending request",ackActual.contains("Done"));
				
			//confirm raw request
			assertTrue(plainActual, plainActual.contains("SimpleRequest"));
			assertTrue(plainActual, plainActual.contains("{\"ConnectionId\":1}"));
			assertTrue(plainActual, plainActual.contains("{\"Payload\":\"GET / HTTP/1."));
			assertTrue(plainActual, plainActual.contains("Connection: keep-alive\r\n\r\n\""));
	
			//confirm encrypted request
			assertTrue(encryptedActual.contains("EncryptedRequest"));
			assertTrue(encryptedActual.contains("{\"ConnectionId\":1}"));
			assertTrue(encryptedActual.contains("{\"Encrypted\":\""));
			
			
			String s = new String(finalContent.toByteArray());			
			
			boolean foundEnd = s.contains("0x3c,0x2f,0x68,0x74,0x6d,0x6c,0x3e");
			if (!foundEnd) {
				assertTrue("Server sent no response", contentResponse.size()>0);
				
				System.out.println("decrypted data from server: "+new String(contentResponse.toByteArray()));
				
				//not always an error because sometimes </html> gets split across messages.
				assertTrue(new String(contentResponse.toByteArray()),(new String(contentResponse.toByteArray()).contains("</html>"))); //we got the end of the message from the server.
			}
			assertTrue("Did not parse header body\n"+s, foundEnd);//hex for </html>  we parsed the HTTP response and pulled out the body
			
		}
		
		
		scheduler.shutdown();
		scheduler.awaitTermination(2, TimeUnit.SECONDS);

		
		
	}
		
	@Test
	public void buildPipeline() {
		
		//forced sequential calls, send next after previous returns.
		boolean sequential = true;
		
		//only build minimum for the pipeline
		
		GraphManager gm = new GraphManager();
		HardwareImpl hardware = new TestHardware(gm);
		
		final int inputsCount = 2;
		
		int base2SimultaniousConnections = 1;
		final int outputsCount = 2;//must be < connections
		int maxPartialResponses = 2;
		int maxListeners = 1<<base2SimultaniousConnections;

		GraphManager.addDefaultNota(gm, GraphManager.SCHEDULE_RATE, 20_000);
		
		ClientConnectionManager ccm = new ClientConnectionManager(base2SimultaniousConnections,inputsCount);
		IntHashTable listenerPipeLookup = new IntHashTable(base2SimultaniousConnections+2);
		IntHashTable.setItem(listenerPipeLookup, 42, 0);//put on pipe 0
		
		
		PipeConfig<NetRequestSchema> netREquestConfig = new PipeConfig<NetRequestSchema>(NetRequestSchema.instance, 30,1<<9);		
		PipeConfig<TrafficReleaseSchema> trafficReleaseConfig = new PipeConfig<TrafficReleaseSchema>(TrafficReleaseSchema.instance, 30);
		PipeConfig<TrafficAckSchema> trafficAckConfig = new PipeConfig<TrafficAckSchema>(TrafficAckSchema.instance, 4);
		PipeConfig<ClientNetRequestSchema> clientNetRequestConfig = new PipeConfig<ClientNetRequestSchema>(ClientNetRequestSchema.instance,4,16000); 
		PipeConfig<NetParseAckSchema> parseAckConfig = new PipeConfig<NetParseAckSchema>(NetParseAckSchema.instance, 4);
		
		PipeConfig<ClientNetResponseSchema> clientNetResponseConfig = new PipeConfig<ClientNetResponseSchema>(ClientNetResponseSchema.instance, 10, 1<<16); 		
		PipeConfig<NetResponseSchema> netResponseConfig = new PipeConfig<NetResponseSchema>(NetResponseSchema.instance, 10, 1<<15); //if this backs up we get an error TODO: fix

		
		//holds new requests
		Pipe<NetRequestSchema>[] input = new Pipe[inputsCount];		
		//new requests are not release until this is sent
		Pipe<TrafficReleaseSchema>[] goPipe = new Pipe[inputsCount];
		//responses from the server	
		Pipe<NetResponseSchema>[] toReactor = new Pipe[maxListeners];
		//this is the ack back that the request was sent
		Pipe<TrafficAckSchema>[] ackPipe = new Pipe[inputsCount]; 
	
		
		
		int m = maxListeners;
		while (--m>=0) {
			toReactor[m] = new Pipe<NetResponseSchema>(netResponseConfig);
		}
		
		
		int k = inputsCount;
		while (--k>=0) {
			ackPipe[k] = new Pipe<TrafficAckSchema>(trafficAckConfig);
			goPipe[k] = new Pipe<TrafficReleaseSchema>(trafficReleaseConfig); 
            input[k] = new Pipe<NetRequestSchema>(netREquestConfig);	
		}	
		
		Pipe<ClientNetRequestSchema>[] clientRequests = new Pipe[outputsCount];
		int r = outputsCount;
		while (--r>=0) {
			clientRequests[r] = new Pipe<ClientNetRequestSchema>(clientNetRequestConfig);		
		}
		HTTPClientRequestStage requestStage = new HTTPClientRequestStage(gm, hardware, ccm, input, goPipe, ackPipe, clientRequests);
		
		
		NetGraphBuilder.buildHTTPClientGraph(gm, outputsCount, maxPartialResponses, ccm, listenerPipeLookup, clientNetRequestConfig,
				parseAckConfig, clientNetResponseConfig, clientRequests, toReactor);
		
		int i = toReactor.length;
		PipeCleanerStage[] cleaners = new PipeCleanerStage[i];
		while (--i>=0) {
			cleaners[i] = new PipeCleanerStage<>(gm, toReactor[i]); 
		}

		int j = ackPipe.length;
		while (--j>=0) {
			new PipeCleanerStage<>(gm, ackPipe[j]);			
		}
		
		

		
		//MonitorConsoleStage.attach(gm);
		//GraphManager.enableBatching(gm);
		
		//TODO: why is this scheduler taking so long to stop.
		StageScheduler scheduler = new FixedThreadsScheduler(gm, 5);
		//StageScheduler scheduler = new ThreadPerStageScheduler(gm);
		
		
		
		scheduler.startup();		


		
		final int MSG_SIZE = 6;
		
		int testSize = 50; 
		
		int requests = testSize;
		
		
		long timeout = System.currentTimeMillis()+(testSize*1500); //reasonable timeout
		
		long start = System.currentTimeMillis();
		int d = 0;
		while (requests>0 && System.currentTimeMillis()<timeout) {
			

			if (sequential) {
				
				System.out.println("received "+d);
				while (doneCount(cleaners)<d && System.currentTimeMillis()<timeout){
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
			
			Pipe<NetRequestSchema> pipe = input[0];
			if (PipeWriter.tryWriteFragment(pipe, NetRequestSchema.MSG_HTTPGET_100)) {
				PipeWriter.writeUTF8(pipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_HOST_2, "encrypted.google.com");
				PipeWriter.writeInt(pipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_LISTENER_10, 42);
				PipeWriter.writeUTF8(pipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_PATH_3, "/");
				PipeWriter.writeInt(pipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_PORT_1, 443);
				PipeWriter.publishWrites(pipe);
				
				Pipe<TrafficReleaseSchema> pipe2 = goPipe[0];
				while (!PipeWriter.tryWriteFragment(pipe2, TrafficReleaseSchema.MSG_RELEASE_20)) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							break;
						}
					}
				PipeWriter.writeInt(pipe2, TrafficReleaseSchema.MSG_RELEASE_20_FIELD_COUNT_22, 1);
				PipeWriter.publishWrites(pipe2);
				requests--;
			
				d+=MSG_SIZE;
		
			}
		}
		

		//count total messages, we know the parser will only send 1 message for each completed event, it does not yet have streaming.

		System.out.println("watching for responses");
		
		int expected = MSG_SIZE*(testSize);
		int count = 0;
		int lastCount = 0;
		do {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				break;
			}
			
			count = doneCount(cleaners);
			
			if (count!=lastCount) {				
				lastCount = count;
				
				
				///System.err.println("pct "+((100f*lastCount)/(float)expected));
				
			}

		} while (count<expected && System.currentTimeMillis()<timeout);

		long duration = System.currentTimeMillis()-start;
		//59 65 67 63
		
		//74 69 74
		System.out.println("shutdown");
		scheduler.shutdown();
		scheduler.awaitTermination(2, TimeUnit.SECONDS);
			
		System.out.println("duration: "+duration);
		System.out.println("ms per call: "+(duration/(float)testSize));
		
		assertEquals(expected, lastCount);

		
		
		
	}

	private int doneCount(PipeCleanerStage[] cleaners) {
		int count;
		{
		int k = cleaners.length;
		int c=0;
		while (--k>=0) {
			c+=cleaners[k].getTotalSlabCount();
			
		}
		
		count = c;
		}
		return count;
	}


	
	
}
