package com.ociweb.pronghorn.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.schema.ClientNetRequestSchema;
import com.ociweb.pronghorn.schema.ClientNetResponseSchema;

public class ClientConnection {

	private static final Logger log = LoggerFactory.getLogger(ClientConnection.class);

	static boolean isShuttingDown =  false;
	
	private final SSLEngine engine;
	private final SocketChannel socketChannel;
	private SelectionKey key; //only registered after handshake is complete.
	private boolean isValid = true;
	
	
	private final byte[] connectionGUID;
	
	private final int pipeIdx;
	private long id=-1;
	
	private long requestsSent;
	private long responsesReceived;
	
	private final int userId;
	private final String host;
	private final int port;
	
	private static InetAddress testAddr;
	
	private long closeTimeLimit = Long.MAX_VALUE;
	private long TIME_TILL_CLOSE = 10_000;
	
	
	static {
		
		try {
			testAddr = InetAddress.getByName("www.google.com");
		} catch (UnknownHostException e) {
			log.error("no network connection.");
			System.exit(-1);
		}
	
	}
	
	private boolean hasNetworkConnectivity() {
		try {
			return testAddr.isReachable(10_000);
		} catch (IOException e) {
			return false;
		}
	}
	
	public ClientConnection(String host, int port, int userId, int pipeIdx) throws IOException {
		assert(port<=65535);
		
		// RFC 1035 the length of a FQDN is limited to 255 characters
		this.connectionGUID = new byte[(2*host.length())+6];
		buildGUID(connectionGUID, host, port, userId);
		this.pipeIdx = pipeIdx;
		this.userId = userId;
		this.host = host;
		this.port = port;
				
		this.engine = SSLEngineFactory.createSSLEngine(host, port);
		this.engine.setUseClientMode(true);
		
		this.socketChannel = SocketChannel.open();
		this.socketChannel.configureBlocking(false);  
		this.socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		this.socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true); 
		this.socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1<<18); //.25 MB
						
		try {
			InetSocketAddress remote = new InetSocketAddress(host, port);
			this.socketChannel.connect(remote);
		} catch (UnresolvedAddressException uae) {
			
			if (hasNetworkConnectivity()) {
				log.error("unable to find {}:{}",host,port);
				throw uae;
			} else {
				log.error("No network connection.");
				System.exit(-1);						
			}
		}
		this.socketChannel.finishConnect(); //call again later to confirm its done.	
		
	}
	
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public void incRequestsSent() {
		closeTimeLimit = Long.MAX_VALUE;
		requestsSent++;		
	}
	
	public void waitForMatch() {
		while (responsesReceived<requestsSent) {
			Thread.yield();
		}
	}
	
	public boolean incResponsesReceived() {
		assert(1+responsesReceived<=requestsSent) : "received more responses than requests were sent";
		boolean result = (++responsesReceived)==requestsSent;
		if (result) {
			
			if (isShuttingDown) {
				close();
			} else {
				closeTimeLimit = System.currentTimeMillis()+TIME_TILL_CLOSE;
			}
			
		}
		return result;
	}
	
	public SelectionKey getSelectionKey() {
		return key;
	}
	
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		assert(this.id == -1);
		this.id = id;	
	}

	public int requestPipeLineIdx() {
		return pipeIdx;
	}

	public static int buildGUID(byte[] target, CharSequence host, int port, int userId) {
		//TODO: if we find a better hash for host port user we can avoid this trie lookup. TODO: performance improvement.
		//new Exception("build guid").printStackTrace();
		
		int pos = 0;
    	for(int i = 0; i<host.length(); i++) {
    		short c = (short)host.charAt(i);
    		target[pos++] = (byte)(c>>8);
    		target[pos++] = (byte)(c);
    	}
    	target[pos++] = (byte)(port>>8);
    	target[pos++] = (byte)(port);
    	
    	target[pos++] = (byte)(userId>>24);
    	target[pos++] = (byte)(userId>>16);
    	target[pos++] = (byte)(userId>>8);
    	target[pos++] = (byte)(userId);
    	return pos;
	}
	
	public byte[] GUID() {
		return connectionGUID;
	}
	
	/**
	 * After construction this must be called until it returns true before using this connection. 
	 * @throws IOException
	 */
	public boolean isFinishConnect() throws IOException {
		return socketChannel.finishConnect();
	}
	
		
	public void handshake(Selector selector) throws IOException {

		assert(socketChannel.finishConnect());
		
		long limit = System.currentTimeMillis()+ 2000; //2 second time out to re-try the handshake
		do {
			engine.beginHandshake();	
    		isValid = doHandshake(socketChannel, engine);
    		//log.debug("is valid handshake connection {} ",isValid);
    		
		} while (!isValid && System.currentTimeMillis()<limit);
    	
    	if (isValid) {
    		//only register selector for listening to responses after the handshake.
    		//TODO: should we do this per url request instead, then ungesiter when done ??  Yes..
    		this.key = socketChannel.register(selector, SelectionKey.OP_READ, this); 
    		
    	}
    	
	}
	

	public boolean isValid() {

		if (!socketChannel.isConnected()) {
			return false;
		}
		
		if (responsesReceived==requestsSent && System.currentTimeMillis() > closeTimeLimit) {
			log.info("stale connection closed after non use {}",this);
			close();
			return false;
		}
		return isValid;
	}
	

	public void close() {
		if (isValid) {
			isValid = false;
			try {
				 engine.closeOutbound();
			  //   doHandshake(socketChannel, engine);
			     
			} catch (Throwable e) {
				isValid = false;
				log.warn("Error closing connection ",e);
			} finally {
				try {
					socketChannel.close();
				} catch (IOException e) {					
				}
			}
		}
		
	}
		
	
	public boolean writeToSocketChannel(Pipe<ClientNetRequestSchema> source) {
		ByteBuffer[] writeHolder = PipeReader.wrappedUnstructuredLayoutBuffer(source, ClientNetRequestSchema.MSG_SIMPLEREQUEST_100_FIELD_PAYLOAD_103);
		try {
			do {
				socketChannel.write(writeHolder);
			} while (writeHolder[1].hasRemaining()); //TODO: this  is a bad spin loop to be refactored up and out.
		} catch (IOException e) {
			log.debug("unable to write to socket {}",this,e);
			close();
			return false;
		}
		return true;
		
	}
	
	public long readfromSocketChannel(ByteBuffer[] targetByteBuffers) {
		
		try {
			return socketChannel.read(targetByteBuffers);			
		} catch (IOException ex) {			
			log.warn("unable read from socket {}",this,ex);
			close();
		}
		return -1;
	}
	
	
	/**
	 * Encrypt as much as possible based on the data available from the two pipes
	 * @param source
	 * @param target
	 * @param buffer 
	 */
	
	public static void engineWrap(ClientConnectionManager ccm, Pipe<ClientNetRequestSchema> source, Pipe<ClientNetRequestSchema> target, ByteBuffer buffer) {
		
		while (PipeWriter.hasRoomForWrite(target) && PipeReader.tryReadFragment(source) ) {
			
			final ClientConnection cc = ccm.get(PipeReader.readLong(source, ClientNetRequestSchema.MSG_SIMPLEREQUEST_100_FIELD_CONNECTIONID_101));
			
			if (null==cc || !cc.isValid) {
				//do not process this message because the connection has dropped
				continue;
			}
			
			ByteBuffer[] bbHolder = PipeReader.wrappedUnstructuredLayoutBuffer(source, ClientNetRequestSchema.MSG_SIMPLEREQUEST_100_FIELD_PAYLOAD_103);
						
			
			
			ByteBuffer[] targetBuffers = PipeWriter.wrappedUnstructuredLayoutBufferOpen(target, ClientNetRequestSchema.MSG_SIMPLEREQUEST_100_FIELD_PAYLOAD_103);

			SSLEngineResult result = null;
			try {
				result = cc.engine.wrap(bbHolder, targetBuffers[0]);
			} catch (SSLException sslex) {
				manageException(sslex, cc);			
				continue;
			}
			Status status = result.getStatus();
			if (status==Status.OK) {
				PipeWriter.tryWriteFragment(target, ClientNetRequestSchema.MSG_ENCRYPTEDREQUEST_110);
				PipeWriter.wrappedUnstructuredLayoutBufferClose(target, ClientNetRequestSchema.MSG_ENCRYPTEDREQUEST_110_FIELD_ENCRYPTED_104, result.bytesProduced());
				
				PipeReader.copyLong(source, target, ClientNetRequestSchema.MSG_SIMPLEREQUEST_100_FIELD_CONNECTIONID_101, ClientNetRequestSchema.MSG_ENCRYPTEDREQUEST_110_FIELD_CONNECTIONID_101);			
				PipeWriter.publishWrites(target);
				PipeReader.releaseReadLock(source);		
				
			} else if (status==Status.CLOSED){
								
				PipeWriter.wrappedUnstructuredLayoutBufferCancel(target);							
				PipeReader.releaseReadLock(source);		
				
				try {
					 cc.engine.closeOutbound();
					 cc.doHandshake(cc.socketChannel, cc.engine);
					 cc.socketChannel.close();
				} catch (IOException e) {
					cc.isValid = false;
					log.warn("Error closing connection ",e);
				}				
			} else if (status==Status.BUFFER_OVERFLOW) {
				///////////
				//This is only needed becauae engine.wrap does not take multiple target ByteBuffers as it should have.
				///////////
				try {
					buffer.clear();
					result = cc.engine.wrap(bbHolder, buffer);
				} catch (SSLException sslex) {
					manageException(sslex, cc);			
					continue;
				}
				
				if (result.getStatus()==Status.OK) {

					//write buffer to openA and openB
					buffer.flip();
					
					int finalLimit = buffer.limit();
					int room = targetBuffers[0].remaining();
					if (room<finalLimit) {
						buffer.limit(room);
					}										
					targetBuffers[0].put(buffer);
					buffer.limit(finalLimit);
					if (buffer.hasRemaining()) {
						targetBuffers[1].put(buffer);
					}
					if (buffer.hasRemaining()) {
						throw new UnsupportedOperationException("ERR1");
					}
					PipeWriter.tryWriteFragment(target, ClientNetRequestSchema.MSG_ENCRYPTEDREQUEST_110);
					PipeWriter.wrappedUnstructuredLayoutBufferClose(target, ClientNetRequestSchema.MSG_ENCRYPTEDREQUEST_110_FIELD_ENCRYPTED_104, result.bytesProduced());
					
					PipeReader.copyLong(source, target, ClientNetRequestSchema.MSG_SIMPLEREQUEST_100_FIELD_CONNECTIONID_101, ClientNetRequestSchema.MSG_ENCRYPTEDREQUEST_110_FIELD_CONNECTIONID_101);			
					PipeWriter.publishWrites(target);
					PipeReader.releaseReadLock(source);	
					
					
				} else {
					throw new UnsupportedOperationException("ERR2");
					//ERROR?
					
				}
				
				
//				request duration 120
//				request duration 121
//				request duration 160
//				[SSLEngineWrapStage id:23] ERROR com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler - Unexpected error in SSLEngineWrapStage #23 with 2 input pipes.
//				java.lang.RuntimeException: output pipe is too small for the content to be written RingId<ClientNetRequestSchema>:22 slabTailPos 5274 slabWrkTailPos 5274 slabHeadPos 5274 slabWrkHeadPos 5274  0/32  blobTailPos 114270 blobWrkTailPos 114270 blobHeadPos 114270 blobWrkHeadPos 114270
//					at com.ociweb.pronghorn.network.ClientConnection.engineWrap(ClientConnection.java:330)
//					at com.ociweb.pronghorn.network.SSLEngineWrapStage.run(SSLEngineWrapStage.java:28)
//					at com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler.runPeriodicLoop(ThreadPerStageScheduler.java:430)
//					at com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler.access$6(ThreadPerStageScheduler.java:405)
//					at com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler$3.run(ThreadPerStageScheduler.java:334)
//					at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
//					at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
//					at java.lang.Thread.run(Thread.java:745)
				
				
//				        return connection id 1
//						return connection id 1
//						[SSLEngineWrapStage id:1] ERROR com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler - Unexpected error in SSLEngineWrapStage #1 with 2 input pipes.
//						java.lang.RuntimeException: output pipe is too small for the content to be written RingId<ClientNetRequestSchema>:10 slabTailPos 5232 slabWrkTailPos 5226 slabHeadPos 5232 slabWrkHeadPos 5232  0/32  blobTailPos 114232 blobWrkTailPos 114232 blobHeadPos 114232 blobWrkHeadPos 114232
//							at com.ociweb.pronghorn.network.ClientConnection.engineWrap(ClientConnection.java:336)
//							at com.ociweb.pronghorn.network.SSLEngineWrapStage.run(SSLEngineWrapStage.java:28)
//							at com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler.runPeriodicLoop(ThreadPerStageScheduler.java:430)
//							at com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler.access$6(ThreadPerStageScheduler.java:405)
//							at com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler$3.run(ThreadPerStageScheduler.java:334)
//							at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
//							at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
//							at java.lang.Thread.run(Thread.java:745)
//						[SSLEngineWrapStage id:1] ERROR com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler - SSLEngineWrapStage #1 input pipe in state:RingId<ClientNetRequestSchema>:9 slabTailPos 5232 slabWrkTailPos 5232 slabHeadPos 5256 slabWrkHeadPos 5256  24/32  blobTailPos 88944 blobWrkTailPos 88944 blobHeadPos 89352 blobWrkHeadPos 89352
//						[SSLEngineWrapStage id:1] ERROR com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler - SSLEngineWrapStage #1 input pipe in state:RingId<ClientNetRequestSchema>:11 slabTailPos 0 slabWrkTailPos 0 slabHeadPos 0 slabWrkHeadPos 0  0/32  blobTailPos 0 blobWrkTailPos 0 blobHeadPos 0 blobWrkHeadPos 0
//
//				
			//	throw new RuntimeException("output pipe is too small for the content to be written "+target);
			} else {
				throw new RuntimeException("case should not happen, we have too little data to be wrapped and sent");
			}
		}
	
	}


	public static void engineUnWrap(ClientConnectionManager ccm, Pipe<ClientNetResponseSchema> source, Pipe<ClientNetResponseSchema> target, ByteBuffer rolling) {

		while (PipeReader.hasContentToRead(source) ) {
			
			while (!PipeWriter.hasRoomForWrite(target)) {				
				//System.err.println("no room to unwrap will try again later   "+target);				
				return;//try again later when there is room in the output
			}			
			
			if (!PipeReader.tryReadFragment(source)) {
				throw new UnsupportedOperationException("Internal error");
			}
	
			
			ClientConnection cc = ccm.get(PipeReader.readLong(source, ClientNetResponseSchema.MSG_RESPONSE_200_FIELD_CONNECTIONID_201));
			if ((null == cc) || (!cc.isValid)) {
				System.err.println("no unwrap, connection closed");
				
				//do not process this message because the connection has dropped
				PipeReader.releaseReadLock(source);
				continue;
			}			
	
			ByteBuffer inputA = PipeReader.wrappedUnstructuredLayoutBufferA(source, ClientNetResponseSchema.MSG_RESPONSE_200_FIELD_PAYLOAD_203);
			ByteBuffer inputB = PipeReader.wrappedUnstructuredLayoutBufferB(source, ClientNetResponseSchema.MSG_RESPONSE_200_FIELD_PAYLOAD_203);


			//Not helping? move back out to member?
			final ByteBuffer[] writeHolderUnWrap = PipeWriter.wrappedUnstructuredLayoutBufferOpen(target, ClientNetResponseSchema.MSG_SIMPLERESPONSE_210_FIELD_PAYLOAD_204);
								
			int thisCase = 0;
			
			//log.debug("UnWrap is taking from input pipe A bytes {}, B bytes {}",inputA.remaining(), inputB.remaining());
			
			SSLEngineResult result;
			int bytesProduced = 0;
			if (inputB.remaining()==0) {
				//if we have some rolling data from previously
				if (rolling.position()==0) {				
					try {
						thisCase=1;
						result = cc.engine.unwrap(inputA, writeHolderUnWrap);
						bytesProduced += result.bytesProduced();
					} catch (SSLException sslex) {
						manageException(sslex, cc);
						System.err.println("no unwrap, exception");
						
						continue;
					}
					rolling.put(inputA);//keep anything left for next time.
					assert(0==inputA.remaining());
					
					while (result.getStatus() == Status.OK && rolling.position()>0) {					
						rolling.flip();
						try {
							//TODO: this may only fill writeHolderUnWrap and may have space left over.
							
							thisCase=2;
							result = cc.engine.unwrap(rolling, writeHolderUnWrap);
							bytesProduced += result.bytesProduced();
						} catch (SSLException sslex) {
							manageException(sslex, cc);
							System.err.println("no unwrap, exception 2");
							
							continue;
						}
						rolling.compact();
					}
					
					
				} else {
					//add this new content onto the end before use.
					rolling.put(inputA);
					assert(0==inputA.remaining());
					
					//flip so unwrap can see it
					rolling.flip();
					try {
						//Takes single buffer in. NOTE: NONE OF THIS LOGIC WOULD HAVE BEEN REQURIED IF SSLEngine.unwrap took ByteBuffer[] instead of a single !!
						thisCase=3;
						result = cc.engine.unwrap(rolling, writeHolderUnWrap);
						bytesProduced += result.bytesProduced();
					} catch (SSLException sslex) {
						manageException(sslex, cc);
						System.err.println("no unwrap, exception 3");
						
						continue;
					}
					rolling.compact();	
					
					while (result.getStatus() == Status.OK && rolling.position()>0) {
						rolling.flip();
						try {
							thisCase=4;
							result = cc.engine.unwrap(rolling, writeHolderUnWrap);
							bytesProduced += result.bytesProduced();
						} catch (SSLException sslex) {
							manageException(sslex, cc);
							System.err.println("no unwrap, exception 4");
							
							continue;
						}
						
						rolling.compact();
					}
					
				}
			} else {
				//working this is confirmed as used.
				//System.out.println("***************  A and B ");
				assert(inputA.hasRemaining());
				assert(inputB.hasRemaining());
				
				rolling.put(inputA);  //slow copy
				rolling.put(inputB);  //slow copy
				rolling.flip();
				try {
					thisCase=5;
					result = cc.engine.unwrap(rolling, writeHolderUnWrap);
					bytesProduced += result.bytesProduced();
				} catch (SSLException sslex) {
					manageException(sslex, cc);
					System.err.println("no unwrap, exception 5");
					
					continue;
				}
				rolling.compact();
				
				while (result.getStatus() == Status.OK && rolling.position()>0) {
					rolling.flip();
					try {
						thisCase=6;
						result = cc.engine.unwrap(rolling, writeHolderUnWrap);
						bytesProduced += result.bytesProduced();
					} catch (SSLException sslex) {
						manageException(sslex, cc);
						System.err.println("no unwrap, exception 6");
						
						continue;
					}
					rolling.compact();
				}
			}

			PipeReader.releaseReadLock(source);	
			
			Status status = result.getStatus();
			if(bytesProduced>0) {
				if (!PipeWriter.tryWriteFragment(target, ClientNetResponseSchema.MSG_SIMPLERESPONSE_210)) {
					throw new RuntimeException("already checked for space should not happen.");
				}
				PipeWriter.wrappedUnstructuredLayoutBufferClose(target, ClientNetResponseSchema.MSG_SIMPLERESPONSE_210_FIELD_PAYLOAD_204, bytesProduced);
				bytesProduced = -1;
				
				//assert(longs match the one for cc)
				PipeWriter.writeLong(target, ClientNetResponseSchema.MSG_SIMPLERESPONSE_210_FIELD_CONNECTIONID_201, cc.id);
				//    PipeReader.copyLong(source, target, ClientNetResponseSchema.MSG_RESPONSE_200_FIELD_CONNECTIONID_201, ClientNetResponseSchema.MSG_SIMPLERESPONSE_210_FIELD_CONNECTIONID_201);
				
				PipeWriter.publishWrites(target);
				
			}
			
			
			if (status==Status.OK) {						

			} else if (status==Status.CLOSED){
				if (bytesProduced==0) {
					PipeWriter.wrappedUnstructuredLayoutBufferCancel(target);							
				} else {
					System.err.println("AAA Xxxxxxxxxxxxxxxxxx found some data to send ERROR: must publish this");
					
					
				}
				
				try {
					 cc.engine.closeOutbound();
					 cc.doHandshake(cc.socketChannel, cc.engine);
				     cc.socketChannel.close();
				} catch (IOException e) {
					cc.isValid = false;
					log.warn("Error closing connection ",e);
				}				
			} else if (status==Status.BUFFER_UNDERFLOW) {
				
				//roller already contains previous so no work but to cancel the outgoing write
				if (bytesProduced==0) {
					PipeWriter.wrappedUnstructuredLayoutBufferCancel(target);							
				} else {
					if (bytesProduced>0) {
						if (!PipeWriter.tryWriteFragment(target, ClientNetResponseSchema.MSG_SIMPLERESPONSE_210)) {
							throw new RuntimeException("already checked for space should not happen.");
						}
						PipeWriter.wrappedUnstructuredLayoutBufferClose(target, ClientNetResponseSchema.MSG_SIMPLERESPONSE_210_FIELD_PAYLOAD_204, bytesProduced);
						bytesProduced = -1;
						
						//assert(longs match the one for cc)
						PipeWriter.writeLong(target, ClientNetResponseSchema.MSG_SIMPLERESPONSE_210_FIELD_CONNECTIONID_201, cc.id);
						//    PipeReader.copyLong(source, target, ClientNetResponseSchema.MSG_RESPONSE_200_FIELD_CONNECTIONID_201, ClientNetResponseSchema.MSG_SIMPLERESPONSE_210_FIELD_CONNECTIONID_201);
						
						PipeWriter.publishWrites(target);
					}
				}
			} else {				
				assert(status == Status.BUFFER_OVERFLOW);
				
				throw new RuntimeException("server is untrustworthy? Output pipe is too small for the content to be written inside "+
			                               target.maxAvgVarLen+" reading from "+rolling.position());
				

			}	
		}
	}

    
	
	private static void manageException(SSLException sslex, ClientConnection cc) {
		try {
			cc.close();
		} catch (Throwable t) {
			//ignore we are closing this connection
		}
		log.error("Unable to encrypt closed conection",sslex);
	}

	//TODO: needs more review to convert this into a garbage free version
    private boolean doHandshake(SocketChannel socketChannel, SSLEngine engine) throws IOException {

        SSLEngineResult result;
        HandshakeStatus handshakeStatus;

        // NioSslPeer's fields myAppData and peerAppData are supposed to be large enough to hold all message data the peer
        // will send and expects to receive from the other peer respectively. Since the messages to be exchanged will usually be less
        // than 16KB long the capacity of these fields should also be smaller. Here we initialize these two local buffers
        // to be used for the handshake, while keeping client's buffers at the same size.
        int appBufferSize = engine.getSession().getApplicationBufferSize()*2;
        ByteBuffer handshakeMyAppData = ByteBuffer.allocate(appBufferSize);        
        ByteBuffer handshakePeerAppData = ByteBuffer.allocate(appBufferSize);
        
        SSLSession session = engine.getSession();
        ByteBuffer myNetData = ByteBuffer.allocate(Math.max(1<<14, session.getPacketBufferSize()));
        
        //These are the check sizes we read from the server.
        ByteBuffer peerNetData = ByteBuffer.allocate(Math.max(1<<15, session.getPacketBufferSize()));
        
        myNetData.clear();
        peerNetData.clear();
        
        //long limit = System.currentTimeMillis()+2000; //timeout
        handshakeStatus = engine.getHandshakeStatus();
        while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
        	
            switch (handshakeStatus) {
	            case NEED_UNWRAP:
	            	
	            	int temp = 0;
	            	do {
	            		temp = socketChannel.read(peerNetData); //TODO: update to be non blocking.
	            	} while (temp>0);
	            	
	                if (temp < 0) {
	                    if (engine.isInboundDone() && engine.isOutboundDone()) {
	                        return false;
	                    }
	                    
	                    try {
	                        engine.closeInbound();
	                    } catch (SSLException e) {
	                        boolean debug = false;
	                        if (debug) {
	                        	if (!isShuttingDown) {	//if we are shutting down this is not an error.                    	
	                        		log.trace("This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.", e);
	                        	}
	                        }
	                    	
	                    }
	                    engine.closeOutbound();
	                    // After closeOutbound the engine will be set to WRAP state, in order to try to send a close message to the client.
	                    handshakeStatus = engine.getHandshakeStatus();
	                    break;
	                }
	                peerNetData.flip();
	                try {
	                    result = engine.unwrap(peerNetData, handshakePeerAppData);
	                    peerNetData.compact();
	                    handshakeStatus = result.getHandshakeStatus();
	                } catch (SSLException sslException) {
	                	if (!isShuttingDown) {
	                		log.error("A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...", sslException);
	                	}
	                    engine.closeOutbound();
	                    handshakeStatus = engine.getHandshakeStatus();
	                    break;
	                }
	                switch (result.getStatus()) {
		                case OK:
		                    break;
		                case BUFFER_OVERFLOW:
		                	throw new UnsupportedOperationException("Buffer overflow, the peerAppData must be larger or the server is sending responses too large");
		          //          break;
		                case BUFFER_UNDERFLOW:
		                	
		                	if (peerNetData.position() == peerNetData.limit()) {
		                		throw new UnsupportedOperationException("Should not happen but ByteBuffer was too small upon construction");
		                	} else {
		                		//do nothing since the server is not yet talking to us.
		                	}
		                	break;
		                case CLOSED:
		                    if (engine.isOutboundDone()) {
		                        return false;
		                    } else {
		                        engine.closeOutbound();
		                        handshakeStatus = engine.getHandshakeStatus();
		                        break;
		                    }
		                default:
		                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
	                }
	                break;
	            case NEED_WRAP:
	                myNetData.clear();
	                try {
	                    result = engine.wrap(handshakeMyAppData, myNetData);
	                    handshakeStatus = result.getHandshakeStatus();
	                } catch (SSLException sslException) {
	                    log.error("A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...", sslException);
	                    engine.closeOutbound();
	                    handshakeStatus = engine.getHandshakeStatus();
	                    break;
	                }
	                switch (result.getStatus()) {
		                case OK :
		                    myNetData.flip();
		                    while (myNetData.hasRemaining()) {
		                        socketChannel.write(myNetData); //TODO: update to be non blocking.
		                    }
		                    break;
		                case BUFFER_OVERFLOW:
		                    // Will occur if there is not enough space in myNetData buffer to write all the data that would be generated by the method wrap.
		                    // Since myNetData is set to session's packet size we should not get to this point because SSLEngine is supposed
		                    // to produce messages smaller or equal to that, but a general handling would be the following:
		                    //myNetData = enlargeBuffer(myNetData, engine.getSession().getPacketBufferSize());
		                    throw new UnsupportedOperationException("Buffer overflow, the pipe must be larger or the server is sending responses too large");
		             //       break;
		                case BUFFER_UNDERFLOW:
		                    throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
		                case CLOSED:
		                    try {
		                        myNetData.flip();
		                        while (myNetData.hasRemaining()) {
		                            socketChannel.write(myNetData); //TODO: update to be non blocking.
		                            
		                        }
		                        // At this point the handshake status will probably be NEED_UNWRAP so we make sure that peerNetData is clear to read.
		                        peerNetData.clear();
		                    } catch (Exception e) {
		                        log.error("Failed to send server's CLOSE message due to socket channel's failure.");
		                        handshakeStatus = engine.getHandshakeStatus();
		                    }
		                    break;
		                default:
		                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
	                }
	                break;
	            case NEED_TASK:
	                Runnable task;
	                while ((task = engine.getDelegatedTask()) != null) {
	                	task.run(); //NOTE: could be run in parallel but we only have 1 thread now
	                }
	                handshakeStatus = engine.getHandshakeStatus();
	                break;
	            case FINISHED:
	                break;
	            case NOT_HANDSHAKING:
	                break;
	            default:
	                throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
	        }
        }

        return true;

    }



	
}
