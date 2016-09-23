package com.ociweb.pronghorn.network;

import java.nio.ByteBuffer;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.schema.ClientNetRequestSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class SSLEngineWrapStage extends PronghornStage {

	private final ClientConnectionManager ccm;
	private final Pipe<ClientNetRequestSchema>[] encryptedContent; 
	private final Pipe<ClientNetRequestSchema>[] plainContent;
	private ByteBuffer[]                          buffers;
	
	
	protected SSLEngineWrapStage(GraphManager graphManager, ClientConnectionManager ccm, Pipe<ClientNetRequestSchema>[] plainContent, Pipe<ClientNetRequestSchema>[] encryptedContent) {
		super(graphManager, plainContent, encryptedContent);
		this.ccm = ccm;
		this.encryptedContent = encryptedContent;
		this.plainContent = plainContent;
		assert(encryptedContent.length==plainContent.length);
	}

	@Override
	public void startup() {
		
		//must allocate buffers for the out of order content 
		int c = plainContent.length;
		buffers = new ByteBuffer[c];
		while (--c>=0) {
			buffers[c] = ByteBuffer.allocate(plainContent[c].maxAvgVarLen*2);
		}				
		
	}
	
	@Override
	public void run() {
		
		int i = encryptedContent.length;
		while (--i >= 0) {
			ClientConnection.engineWrap(ccm, plainContent[i], encryptedContent[i], buffers[i]);
		}
	}


}
