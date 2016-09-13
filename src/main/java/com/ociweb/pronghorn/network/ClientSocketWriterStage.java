package com.ociweb.pronghorn.network;

import com.ociweb.pronghorn.iot.schema.ClientNetRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class ClientSocketWriterStage extends PronghornStage {
	
	private final ClientConnectionManager ccm;
	private final Pipe<ClientNetRequestSchema>[] input;
	private int shutCountDown;
	
	protected ClientSocketWriterStage(GraphManager graphManager, ClientConnectionManager ccm, Pipe<ClientNetRequestSchema>[] input) {
		super(graphManager, input, NONE);
		this.ccm = ccm;
		this.input = input;
		this.shutCountDown = input.length;
		
	}

	@Override
	public void run() {
		
		int i = input.length;
		while (--i>=0) {		
			while (PipeReader.tryReadFragment(input[i])) try {				
				if (ClientNetRequestSchema.MSG_ENCRYPTEDREQUEST_110 == PipeReader.getMsgIdx(input[i])) {
									
					ClientConnection cc = ccm.get(PipeReader.readLong(input[i], ClientNetRequestSchema.MSG_ENCRYPTEDREQUEST_110_FIELD_CONNECTIONID_101));
					
					if (null!=cc && cc.isValid()) {
						if (!cc.writeToSocketChannel(input[i])) { //TODO: this is a blocking write to be converted to non blocking soon.
							cc.close();//unable to write request
						}
					} else {
						//TODO: this in important case to test.
						//can not send this connection was lost
						continue;
					}
				} else {
					if (ClientNetRequestSchema.MSG_ENCRYPTEDREQUEST_110 == PipeReader.getMsgIdx(input[i])) {
						//TODO: neeed the non encrypted message to also be supported
						throw new UnsupportedOperationException();
					}
					
					assert(-1 == PipeReader.getMsgIdx(input[i])) : "Expected end of stream shutdown";
					if (--this.shutCountDown <= 0) {
						requestShutdown();
						return;
					}					
				}				
				
			} finally {
				PipeReader.releaseReadLock(input[i]);
			}
		
		}
		
	}

}
