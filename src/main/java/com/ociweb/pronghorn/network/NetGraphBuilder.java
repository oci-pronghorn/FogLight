package com.ociweb.pronghorn.network;

import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.pronghorn.iot.schema.ClientNetRequestSchema;
import com.ociweb.pronghorn.iot.schema.ClientNetResponseSchema;
import com.ociweb.pronghorn.iot.schema.NetParseAckSchema;
import com.ociweb.pronghorn.iot.schema.NetRequestSchema;
import com.ociweb.pronghorn.iot.schema.NetResponseSchema;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.util.hash.IntHashTable;
import com.ociweb.pronghorn.stage.network.config.HTTPSpecification;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class NetGraphBuilder {

	
	public static void buildHTTPClientGraph(GraphManager gm, HardwareImpl hardware, int inputsCount, int outputsCount,
			int maxPartialResponses, int maxListeners, ClientConnectionManager ccm, IntHashTable listenerPipeLookup,
			PipeConfig<NetRequestSchema> netRequestConfig, 
			PipeConfig<TrafficReleaseSchema> trafficReleaseConfig,
			PipeConfig<TrafficAckSchema> trafficAckConfig, 
			PipeConfig<ClientNetRequestSchema> clientNetRequestConfig,
			PipeConfig<NetParseAckSchema> parseAckConfig, 
			PipeConfig<ClientNetResponseSchema> clientNetResponseConfig,
			PipeConfig<NetResponseSchema> netResponseConfig, 
			Pipe<NetRequestSchema>[] input,
			Pipe<TrafficReleaseSchema>[] goPipe, 
			Pipe<NetResponseSchema>[] toReactor, 
			Pipe<TrafficAckSchema>[] ackPipe) {

		//this is the fully formed request to be wrapped
		Pipe<ClientNetRequestSchema>[] clientRequests = new Pipe[outputsCount];
		//this is the encrypted (aka wrapped) fully formed requests
		Pipe<ClientNetRequestSchema>[] wrappedClientRequests = new Pipe[outputsCount];	

		Pipe<NetParseAckSchema> parseAck = new Pipe<NetParseAckSchema>(parseAckConfig);
		Pipe<ClientNetResponseSchema>[] socketResponse = new Pipe[maxPartialResponses];
		Pipe<ClientNetResponseSchema>[] clearResponse = new Pipe[maxPartialResponses];		
		
		
		int m = maxListeners;
		while (--m>=0) {
			toReactor[m] = new Pipe<NetResponseSchema>(netResponseConfig);
		}
				
		int k = maxPartialResponses;
		while (--k>=0) {
			socketResponse[k] = new Pipe<ClientNetResponseSchema>(clientNetResponseConfig);
			clearResponse[k] = new Pipe<ClientNetResponseSchema>(clientNetResponseConfig);
		}
		
		int j = outputsCount;
		while (--j>=0) {
			clientRequests[j] = new Pipe<ClientNetRequestSchema>(clientNetRequestConfig);									
			wrappedClientRequests[j] = new Pipe<ClientNetRequestSchema>(clientNetRequestConfig);
		}
		
		int i = inputsCount;
		while (--i>=0) {
			ackPipe[i] = new Pipe<TrafficAckSchema>(trafficAckConfig);
			goPipe[i] = new Pipe<TrafficReleaseSchema>(trafficReleaseConfig); 
            input[i] = new Pipe<NetRequestSchema>(netRequestConfig);	
		}
				
	
		
		///////////////////
		//add the stage under test
		////////////////////
		
		HTTPClientRequestStage requestStage = new HTTPClientRequestStage(gm, hardware, ccm, input, goPipe, ackPipe, clientRequests);
		SSLEngineWrapStage wrapStage = new  SSLEngineWrapStage(gm,ccm,clientRequests, wrappedClientRequests );
		ClientSocketWriterStage socketWriteStage = new ClientSocketWriterStage(gm, ccm, wrappedClientRequests);
		//the data was sent by this stage but the next stage is responsible for responding to the results.
		
		ClientSocketReaderStage socketReaderStage = new ClientSocketReaderStage(gm, ccm, parseAck, socketResponse);
        // 	GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 0, socketReaderStage); //may be required for 10Gb+ connections
				
		//the responding reading data is encrypted so there is not much to be tested
		//we will test after the unwrap
		SSLEngineUnWrapStage unwrapStage = new SSLEngineUnWrapStage(gm, ccm, socketResponse, clearResponse);	
		HTTPResponseParserStage parser = new HTTPResponseParserStage(gm, clearResponse, toReactor, parseAck, listenerPipeLookup, ccm, HTTPSpecification.defaultSpec());
	}
	
}
