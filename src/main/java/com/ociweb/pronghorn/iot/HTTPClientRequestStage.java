package com.ociweb.pronghorn.iot;

import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.network.ClientConnection;
import com.ociweb.pronghorn.network.ClientCoordinator;
import com.ociweb.pronghorn.network.schema.NetPayloadSchema;
import com.ociweb.pronghorn.network.schema.NetRequestSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;

public class HTTPClientRequestStage extends AbstractTrafficOrderedStage {

	public static final Logger log = LoggerFactory.getLogger(HTTPClientRequestStage.class);
	
	private final Pipe<NetRequestSchema>[] input;
	private final Pipe<NetPayloadSchema>[] output;
	private final ClientCoordinator ccm;
    
	private boolean isTLS = true; //TODO: should not be fixed.
    
	private int activeOutIdx = 0;
			
	private static final String implementationVersion = PronghornStage.class.getPackage().getImplementationVersion()==null?"unknown":PronghornStage.class.getPackage().getImplementationVersion();
		
	/**
	 * Parse HTTP data on feed and sends back an ack to the  SSLEngine as each message is decrypted.
	 * 
	 * @param graphManager
	 * @param hardware
	 * @param input
	 * @param goPipe
	 * @param ackPipe
	 * @param output
	 */
	
	public HTTPClientRequestStage(GraphManager graphManager, 
			HardwareImpl hardware,
			ClientCoordinator ccm,
            Pipe<NetRequestSchema>[] input,
            Pipe<TrafficReleaseSchema>[] goPipe,
            Pipe<TrafficAckSchema>[] ackPipe,
            Pipe<NetPayloadSchema>[] output
            ) {
		super(graphManager, hardware, input, goPipe, ackPipe, output);
		this.input = input;
		this.output = output;
		this.ccm = ccm;
		
	}
	
	
	@Override
	public void startup() {
		super.startup();		
	}
	

	
	@Override
	protected void processMessagesForPipe(int activePipe) {
		
		    Pipe<NetRequestSchema> requestPipe = input[activePipe];

	        while (PipeReader.hasContentToRead(requestPipe) && hasReleaseCountRemaining(activePipe) 
	                && isChannelUnBlocked(activePipe)	                
	                && hasRoomForWrite(requestPipe, output, ccm)
	                && PipeReader.tryReadFragment(requestPipe) ){
	  	    
	        	
	        	//Need peek to know if this will block.
	        	
	            int msgIdx = PipeReader.getMsgIdx(requestPipe);
	            
				switch (msgIdx) {
	            			case NetRequestSchema.MSG_HTTPGET_100:
	            				
				                {
				            		final byte[] hostBack = Pipe.blob(requestPipe);
				            		final int hostPos = PipeReader.readBytesPosition(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_HOST_2);
				            		final int hostLen = PipeReader.readBytesLength(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_HOST_2);
				            		final int hostMask = Pipe.blobMask(requestPipe);
				                	
					                int port = PipeReader.readInt(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_PORT_1);
					                int userId = PipeReader.readInt(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_LISTENER_10);
					                
					                long connectionId = ccm.lookup(hostBack, hostPos, hostLen, hostMask, port, userId);	
					                
					                if (-1 != connectionId) {
						                
					                	ClientConnection clientConnection = (ClientConnection)ccm.get(connectionId, 0);
					                	int outIdx = clientConnection.requestPipeLineIdx();
					                	
					                	clientConnection.incRequestsSent();//count of messages can only be done here.
										Pipe<NetPayloadSchema> outputPipe = output[outIdx];
						                				                	
						                if (PipeWriter.tryWriteFragment(outputPipe, NetPayloadSchema.MSG_PLAIN_210) ) {
						                    	
						                	PipeWriter.writeLong(outputPipe, NetPayloadSchema.MSG_PLAIN_210_FIELD_CONNECTIONID_201, connectionId);
						                	PipeWriter.writeLong(outputPipe, NetPayloadSchema.MSG_PLAIN_210_FIELD_POSITION_206, 0);
						                	
						                	DataOutputBlobWriter<NetPayloadSchema> activeWriter = PipeWriter.outputStream(outputPipe);
						                	DataOutputBlobWriter.openField(activeWriter);
											
						                	DataOutputBlobWriter.encodeAsUTF8(activeWriter,"GET");
						                	
						                	int len = PipeReader.readDataLength(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_PATH_3);					                	
						                	int  first = PipeReader.readBytesPosition(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_PATH_3);					                	
						                	boolean prePendSlash = (0==len) || ('/' != PipeReader.readBytesBackingArray(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_PATH_3)[first&Pipe.blobMask(requestPipe)]);  
						                	
											if (prePendSlash) { //NOTE: these can be pre-coverted to bytes so we need not convert on each write. may want to improve.
												DataOutputBlobWriter.encodeAsUTF8(activeWriter," /");
											} else {
												DataOutputBlobWriter.encodeAsUTF8(activeWriter," ");
											}
											
											//Reading from UTF8 field and writing to UTF8 encoded field so we are doing a direct copy here.
											PipeReader.readBytes(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_PATH_3, activeWriter);
											
											finishWritingHeader(hostBack, hostPos, hostLen, hostMask, activeWriter, implementationVersion, 0);
						                	DataOutputBlobWriter.closeHighLevelField(activeWriter, NetPayloadSchema.MSG_PLAIN_210_FIELD_PAYLOAD_204);
						                					                	
						                	PipeWriter.publishWrites(outputPipe);
						                					                	
						                } else {
						                	System.err.println("unable to write");
						                	throw new RuntimeException("Unable to send request, outputPipe is full");
						                }
					                }
			                	}
	            		break;
	            			case NetRequestSchema.MSG_HTTPPOST_101:
	            				
				                {
				                	
				            		final byte[] hostBack = Pipe.blob(requestPipe);
				            		final int hostPos = PipeReader.readBytesPosition(requestPipe, NetRequestSchema.MSG_HTTPPOST_101_FIELD_HOST_2);
				            		final int hostLen = PipeReader.readBytesLength(requestPipe, NetRequestSchema.MSG_HTTPPOST_101_FIELD_HOST_2);
				            		final int hostMask = Pipe.blobMask(requestPipe);
				                	
					                int port = PipeReader.readInt(requestPipe, NetRequestSchema.MSG_HTTPPOST_101_FIELD_PORT_1);
					                int userId = PipeReader.readInt(requestPipe, NetRequestSchema.MSG_HTTPPOST_101_FIELD_LISTENER_10);
					                
					                long connectionId = ccm.lookup(hostBack, hostPos, hostLen, hostMask, port, userId);	
					                //openConnection(activeHost, port, userId, outIdx);
					                
					                if (-1 != connectionId) {
						                
					                	ClientConnection clientConnection = (ClientConnection)ccm.get(connectionId, 0);
					                	int outIdx = clientConnection.requestPipeLineIdx();
					                					                  	
					                	clientConnection.incRequestsSent();//count of messages can only be done here.
										Pipe<NetPayloadSchema> outputPipe = output[outIdx];
					                
						                if (PipeWriter.tryWriteFragment(outputPipe, NetPayloadSchema.MSG_PLAIN_210) ) {
					                    	
						                	PipeWriter.writeLong(outputPipe, NetPayloadSchema.MSG_PLAIN_210_FIELD_CONNECTIONID_201, connectionId);
						                	
						                	DataOutputBlobWriter<NetPayloadSchema> activeWriter = PipeWriter.outputStream(outputPipe);
						                	DataOutputBlobWriter.openField(activeWriter);
						                			                
						                	DataOutputBlobWriter.encodeAsUTF8(activeWriter,"POST");
						                	
						                	int len = PipeReader.readDataLength(requestPipe, NetRequestSchema.MSG_HTTPPOST_101_FIELD_PATH_3);					                	
						                	int  first = PipeReader.readBytesPosition(requestPipe, NetRequestSchema.MSG_HTTPPOST_101_FIELD_PATH_3);					                	
						                	boolean prePendSlash = (0==len) || ('/' != PipeReader.readBytesBackingArray(requestPipe, NetRequestSchema.MSG_HTTPPOST_101_FIELD_PATH_3)[first&Pipe.blobMask(requestPipe)]);  
						                	
											if (prePendSlash) { //NOTE: these can be pre-coverted to bytes so we need not convert on each write. may want to improve.
												DataOutputBlobWriter.encodeAsUTF8(activeWriter," /");
											} else {
												DataOutputBlobWriter.encodeAsUTF8(activeWriter," ");
											}
											
											//Reading from UTF8 field and writing to UTF8 encoded field so we are doing a direct copy here.
											PipeReader.readBytes(requestPipe, NetRequestSchema.MSG_HTTPPOST_101_FIELD_PATH_3, activeWriter);
											
											//TODO: what is the lenght?
											finishWritingHeader(hostBack, hostPos, hostLen, hostMask, activeWriter, implementationVersion, 0);
											
											//TODO: write the payload.
											
											
											
											
											
											//TODO: must write lenghth in header before we write the payload.
											//un-tested  post payload here, TODO: need to add support for chunking and length??
											PipeReader.readBytes(requestPipe, NetRequestSchema.MSG_HTTPPOST_101_FIELD_PAYLOAD_5, activeWriter);
											
						                	DataOutputBlobWriter.closeHighLevelField(activeWriter, NetPayloadSchema.MSG_PLAIN_210_FIELD_PAYLOAD_204);
						                					                	
						                	PipeWriter.publishWrites(outputPipe);
						                					                	
						                } else {
						                	System.err.println("unable to write");
						                	throw new RuntimeException("Unable to send request, outputPipe is full");
						                }
										
										
										
										
					                }
		            		
				                }
	    	        	break;
	    	            
	            	
	            
	            }
			
				PipeReader.releaseReadLock(requestPipe);
				
				//only do now after we know its not blocked and was completed
				decReleaseCount(activePipe);
	        }

	            
		
	}


	//TODO: make static.
	public boolean hasRoomForWrite(Pipe<NetRequestSchema> requestPipe, Pipe<NetPayloadSchema>[] output, ClientCoordinator ccm) {
		int result = -1;
		//if we go around once and find nothing then stop looking
		int i = output.length;
		while (--i>=0) {
			//next idx		
			if (++activeOutIdx == output.length) {
				activeOutIdx = 0;
			}
			//does this one have room
			if (PipeWriter.hasRoomForWrite(output[activeOutIdx])) {
				result = activeOutIdx;
				break;
			}
		}
		int outIdx = result;
		if (-1 == outIdx) {
			return false;
		}
		
		return hasOpenConnection(requestPipe, output, ccm, outIdx);

	}
	
	
	//has side effect fo storing the active connectino as a member so it neeed not be looked up again later.
	public boolean hasOpenConnection(Pipe<NetRequestSchema> requestPipe, 
											Pipe<NetPayloadSchema>[] output, ClientCoordinator ccm, int outIdx) {
		
		if (PipeReader.peekMsg(requestPipe, -1)) {
			return com.ociweb.pronghorn.network.HTTPClientRequestStage.hasRoomForEOF(output);
		}
		
		int hostPos =  PipeReader.peekDataPosition(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_HOST_2);
		int hostLen =  PipeReader.peekDataLength(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_HOST_2);

		byte[] hostBack = Pipe.blob(requestPipe);
		int hostMask = Pipe.blobMask(requestPipe);
		
		
		int port = PipeReader.peekInt(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_PORT_1);
		int userId = PipeReader.peekInt(requestPipe, NetRequestSchema.MSG_HTTPGET_100_FIELD_LISTENER_10);		
						
		ClientConnection activeConnection = ClientCoordinator.openConnection(ccm, hostBack, hostPos, hostLen, hostMask, port, userId, outIdx, output);
				
		
		if (null != activeConnection) {
			
			if (ccm.isTLS) {
				
				//If this connection needs to complete a hanshake first then do that and do not send the request content yet.
				HandshakeStatus handshakeStatus = activeConnection.getEngine().getHandshakeStatus();
				if (HandshakeStatus.FINISHED!=handshakeStatus && HandshakeStatus.NOT_HANDSHAKING!=handshakeStatus) {
					activeConnection = null;
					return false;
				}
	
			}
			
		} else {
			//this happens often when the profiler is running due to contention for sockets.
			
			//"Has no room" for the new connection so we request that the oldest connection is closed.
			
			//instead of doing this (which does not work) we will just wait by returning false.
//			ClientConnection connectionToKill = (ClientConnection)ccm.get( -connectionId, 0);
//			if (null!=connectionToKill) {
//				Pipe<NetPayloadSchema> pipe = output[connectionToKill.requestPipeLineIdx()];
//				if (PipeWriter.hasRoomForWrite(pipe)) {
//					//close the least used connection
//					cleanCloseConnection(connectionToKill, pipe);				
//				}
//			}
		
			return false;
		}
		
		
		outIdx = activeConnection.requestPipeLineIdx(); //this should be done AFTER any handshake logic
		Pipe<NetPayloadSchema> pipe = output[outIdx];
		if (!PipeWriter.hasRoomForWrite(pipe)) {
			return false;
		}
		return true;
	}

	public static void finishWritingHeader(byte[] hostBack, int hostPos, int hostLen, int hostMask,
			                              DataOutputBlobWriter<NetPayloadSchema> writer, CharSequence implementationVersion, long length) {
		DataOutputBlobWriter.encodeAsUTF8(writer," HTTP/1.1\r\nHost: ");
		DataOutputBlobWriter.write(writer,hostBack,hostPos,hostLen,hostMask);
		DataOutputBlobWriter.encodeAsUTF8(writer,"\r\nUser-Agent: Pronghorn/");
		DataOutputBlobWriter.encodeAsUTF8(writer,implementationVersion);
		if (length>0) {
			DataOutputBlobWriter.encodeAsUTF8(writer,"\r\nContent-Length: "+Long.toString(length)); //TODO: rewrite as garbage free.
		} else if (length<0) {
			DataOutputBlobWriter.encodeAsUTF8(writer,"\r\nTransfer-Encoding: chunked");//TODO: write the payload must be chunked.
		}
		DataOutputBlobWriter.encodeAsUTF8(writer,"\r\nConnection: keep-alive\r\n\r\n"); //double \r\b marks the end of the header
	}
	
	
	

}
