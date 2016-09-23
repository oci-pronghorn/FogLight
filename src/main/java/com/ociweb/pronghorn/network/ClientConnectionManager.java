package com.ociweb.pronghorn.network;

import java.io.IOException;
import java.nio.channels.Selector;

import com.ociweb.pronghorn.iot.HTTPClientRequestStage;
import com.ociweb.pronghorn.util.PoolIdx;
import com.ociweb.pronghorn.util.ServiceObjectHolder;
import com.ociweb.pronghorn.util.ServiceObjectValidator;
import com.ociweb.pronghorn.util.TrieParser;
import com.ociweb.pronghorn.util.TrieParserReader;

public class ClientConnectionManager implements ServiceObjectValidator<ClientConnection>{

	private final ServiceObjectHolder<ClientConnection> connections;
	private final TrieParser hostTrie;
	private final TrieParserReader hostTrieReader;
	private final byte[] guidWorkspace = new byte[6+512];
	private final PoolIdx responsePipeLinePool;
	private Selector selector;
		
	//TOOD: may keep internal pipe of "in flight" URLs to be returned with the results...
	
	public ClientConnectionManager(int connectionsInBits, int maxPartialResponses) { 
		connections = new ServiceObjectHolder<ClientConnection>(connectionsInBits, ClientConnection.class, this, false);
		hostTrie = new TrieParser(4096,4,false,false);
		hostTrieReader = new TrieParserReader();
		responsePipeLinePool = new PoolIdx(maxPartialResponses); //NOTE: maxPartialResponses should never be greater than response listener count		
	}
		
	public ClientConnection get(long hostId) {
		ClientConnection response = connections.getValid(hostId);
		if (null == response) {
			releaseResponsePipeLineIdx(hostId);
		}
		return response;
	}
	
	/**
	 * 
	 * This method is not thread safe. 
	 * 
	 * @param host
	 * @param port
	 * @param userId
	 * @return -1 if the host port and userId are not found
	 */
	public long lookup(CharSequence host, int port, int userId) {	
		//TODO: lookup by userID then by port then by host??? may be a better approach instead of guid 
		int len = ClientConnection.buildGUID(guidWorkspace, host, port, userId);		
		return TrieParserReader.query(hostTrieReader, hostTrie, guidWorkspace, 0, len, Integer.MAX_VALUE);
	}
	
	public long add(ClientConnection connection) {
		long id = connections.add(connection);		
		//store under host and port this hostId
		hostTrie.setValue(connection.GUID(), id);
		connection.setId(id);
		return id;
	}

	public int responsePipeLineIdx(long ccId) {
		return responsePipeLinePool.get(ccId);
	}
	
	public void releaseResponsePipeLineIdx(long ccId) {
		responsePipeLinePool.release(ccId);
	}
	
	public int resposePoolSize() {
		return responsePipeLinePool.length();
	}

	@Override
	public boolean isValid(ClientConnection connection) {
		return connection.isValid();
	}


	@Override
	public void dispose(ClientConnection connection) {
		connection.close();
	}

	public void shutdown() {
		ClientConnection.isShuttingDown = true;
		//provide a little time to let the connections finish before we pull the plug.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		connections.disposeAll();
		
	}

	public Selector selector() {
		if (null==selector) {
			try {
				selector = Selector.open();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return selector;
	}

	public static long openConnection(ClientConnectionManager ccm, CharSequence host, int port, int userId, int pipeIdx) {
		long connectionId = ccm.lookup(host, port, userId);				                
		if (-1 == connectionId || null == ccm.get(connectionId)) {
			
			try {
		    	//create new connection because one was not found or the old one was closed
				ClientConnection cc = new ClientConnection(host.toString(), port, userId, pipeIdx);				                	
		    	
		    	while (!cc.isFinishConnect() ) {  //TODO: revisit
		    		Thread.yield();
		    	}
		    	
		    	int p = Thread.currentThread().getPriority();
		    	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		    	cc.handshake(ccm.selector()); //TODO: revisit this is blocking
		    	Thread.currentThread().setPriority(p);
		    	
		    	if (cc.isValid()) {
		    		connectionId = ccm.add(cc);
		    		//log.debug("added new connection under id:"+connectionId);
		    	} else {
		    		HTTPClientRequestStage.log.warn("new connection was invalid {}:{}",host,port);
		    		connectionId = -1;
		    	}
			} catch (IOException ex) {
				HTTPClientRequestStage.log.warn("handshake problems with new connection {}:{}",host,port,ex);
				
				connectionId = -1;
			}				                	
		}
		return connectionId;
	}


	
	
}
