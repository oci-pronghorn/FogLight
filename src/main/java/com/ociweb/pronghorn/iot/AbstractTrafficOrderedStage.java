package com.ociweb.pronghorn.iot;

import static com.ociweb.pronghorn.pipe.PipeWriter.publishWrites;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Blocker;

public abstract class AbstractTrafficOrderedStage extends PronghornStage {

	private final int MAX_DEVICES = 256; //do not know of any hardware yet with more connections than this.

	private final Pipe<TrafficReleaseSchema>[] goPipe;
	private final Pipe<TrafficAckSchema>[] ackPipe;

	protected final Hardware hardware;
	protected Blocker connectionBlocker;
		
	protected int[] activeCounts;	

	private int hitPoints;
    private final GraphManager graphManager;
    protected long msNearWindow;
    private int startLoopAt = -1;
    
	private static final Logger logger = LoggerFactory.getLogger(AbstractTrafficOrderedStage.class);

	//////////////////////////////////////////
	/////////////////////////////////////////
	//Needs testing but this class is complete
	//////////////////////////////////////////
	///do not modify this logic without good reason
	//////////////////////////////////////////

  

	/**
	 * Using real hardware support this stage turns on and off digital pins and sets PWM for analog out.
	 * It supports time based blocks (in ms) specific to each connection.  This way no other commands are
	 * send to that connection until the time expires.  This is across all pipes.
	 * 
	 * 
	 * @param graphManager
	 * @param hardware
	 * @param goPipe
	 * @param ackPipe
	 * @param ccToAdOut
	 */
	public AbstractTrafficOrderedStage(GraphManager graphManager, 
			Hardware hardware,
			Pipe<?>[] output,
			Pipe<TrafficReleaseSchema>[] goPipe, Pipe<TrafficAckSchema>[] ackPipe, Pipe<?> ... otherResponse ) {

		super(graphManager, join(goPipe, output), join(ackPipe, otherResponse));

		this.hardware = hardware;
		this.ackPipe = ackPipe;
		this.goPipe = goPipe;
		this.hitPoints = goPipe.length;
		this.graphManager = graphManager;
		

	}

	@Override 
	public void startup() {
	    	       
        //processing can be very time critical so this thread needs to be on of the highest in priority.
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	    
		connectionBlocker = new Blocker(MAX_DEVICES);
		activeCounts = new int[goPipe.length];
		Arrays.fill(activeCounts, -1); //0 indicates, need to ack, -1 indicates done and ready for more
		
		Number nsPollWindow = (Number)GraphManager.getNota(graphManager, this,  GraphManager.SCHEDULE_RATE, 10_000_000);
		msNearWindow = (long)Math.ceil((nsPollWindow.longValue()*4f)/1_000_000f);
		logger.info("near window size for ordered stage: {} ",msNearWindow);
		startLoopAt = activeCounts.length;
	}

	@Override
	public void run() {
		processReleasedCommands(10_000);		
	}
	
	
	protected void blockChannelDuration(int activePipe, long timeMillis) {
	    hardware.blockChannelUntil(( goPipe[activePipe].id ), hardware.currentTimeMillis() + timeMillis );
	}

    protected boolean processReleasedCommands(long timeout) {
        boolean foundWork;
		int[] localActiveCounts = activeCounts;
		long timeLimit = hardware.currentTimeMillis()+timeout;

		do {
			foundWork = false;
			int a = startLoopAt;
			
				while (--a >= 0) {
				    long now = hardware.currentTimeMillis();
				    hardware.releaseChannelBlocks(now);
				    if (isChannelBlocked(a) ) {
				        return true;            
				    }   
				    if (now >= timeLimit) {
				        //stop here because we have run out of time, do save our location to start back here.
				        startLoopAt = a+1;
                        return false;
				    }
				    
				    //must clear these before calling processMessages, 
				    connectionBlocker.releaseBlocks(now);				    
				    
					//pull all known the values into the active counts array
					if (-1==localActiveCounts[a] && PipeReader.tryReadFragment(goPipe[a])) {                    
						readNextCount(a); 
						foundWork = true;
					}

					int startCount = localActiveCounts[a];
										
			           
					//This method must be called at all times to poll I2C
					processMessagesForPipe(a);
					
					logger.debug("ProcessMessagesForPipe called in output stages");
					if (localActiveCounts[a]>0) {
					    //unable to finish group, try again later, this is critical so that callers can
					    //interact and then block knowing nothing else can get between the commands.
					    startLoopAt = a+1;
					    return true; //a poll may happen here but no other commands will happen until this is completed.
					} else {
					    foundWork |= (localActiveCounts[a]!=startCount);//work was done if progress was made					    
					}					

					//send any acks that are outstanding
					if (startCount > 0 && 0==localActiveCounts[a]) {
					    logger.debug("send ack back to {}",a);					    
					    if (PipeWriter.tryWriteFragment(ackPipe[a], TrafficAckSchema.MSG_DONE_10)) {
							publishWrites(ackPipe[a]);
							localActiveCounts[a] = -1;
							foundWork = true;
						}//this will try again later since we did not clear it to -1
					}
				} 
				startLoopAt = activeCounts.length;
			//only stop after we have 1 cycle where no work was done, this ensure all pipes are as empty as possible before releasing the thread.
			//we also check for 'near' work but only when there is no found work since its more expensive
		} while (foundWork || connectionBlocker.willReleaseInWindow(hardware.currentTimeMillis(),msNearWindow));
		return true;
    }

    protected boolean isChannelBlocked(int a) {
        return hardware.isChannelBlocked( goPipe[a].id );
    }

	protected abstract void processMessagesForPipe(int a);

	private void readNextCount(final int a) {
		assert(PipeReader.isNewMessage(goPipe[a])) : "This test should only have one simple message made up of one fragment";
		int msgIdx = PipeReader.getMsgIdx(goPipe[a]);
		if(TrafficReleaseSchema.MSG_RELEASE_20 == msgIdx){
			assert(-1==activeCounts[a]);
			activeCounts[a] = PipeReader.readInt(goPipe[a], TrafficReleaseSchema.MSG_RELEASE_20_FIELD_COUNT_22);
		}else{
			assert(msgIdx == -1);
			if (--hitPoints == 0) {
				requestShutdown();
			}
		}
		PipeReader.releaseReadLock(goPipe[a]);

	}

	protected void decReleaseCount(int a) {
		activeCounts[a]--;
	}

	protected boolean hasReleaseCountRemaining(int a) {
		if(activeCounts.length>0){
			return activeCounts[a] > 0;
		}else{
			return false;
		}
	}

}