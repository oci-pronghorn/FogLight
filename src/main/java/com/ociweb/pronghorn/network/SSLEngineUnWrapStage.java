package com.ociweb.pronghorn.network;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.schema.ClientNetResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class SSLEngineUnWrapStage extends PronghornStage {

	private final ClientConnectionManager ccm;
	private final Pipe<ClientNetResponseSchema>[] encryptedContent; 
	private final Pipe<ClientNetResponseSchema>[] outgoingPipeLines;
	private ByteBuffer[]                          buffers;
	private Logger log = LoggerFactory.getLogger(SSLEngineUnWrapStage.class);
	
	protected SSLEngineUnWrapStage(GraphManager graphManager, ClientConnectionManager ccm, Pipe<ClientNetResponseSchema>[] encryptedContent, Pipe<ClientNetResponseSchema>[] outgoingPipeLines) {
		super(graphManager, encryptedContent, outgoingPipeLines);
		this.ccm = ccm;
		this.encryptedContent = encryptedContent;
		this.outgoingPipeLines = outgoingPipeLines;
		assert(encryptedContent.length == outgoingPipeLines.length);
	}

	@Override
	public void startup() {
		
		//must allocate buffers for the out of order content 
		int c = encryptedContent.length;
		buffers = new ByteBuffer[c];
		while (--c>=0) {
			buffers[c] = ByteBuffer.allocate(encryptedContent[c].maxAvgVarLen*2);
		}				
		
	}
	
	@Override
	public void run() {
		
		int i = encryptedContent.length;
		while (--i >= 0) {	
			ClientConnection.engineUnWrap(ccm, encryptedContent[i], outgoingPipeLines[i], buffers[i]);
		}
	}

	@Override
	public void shutdown() {
		int i = buffers.length;
		while (--i>=0) {
			
			if (buffers[i].position()>0) {
				log.warn("unwrap found unconsumed data in buffer");
			}
			
		}
	}

}
