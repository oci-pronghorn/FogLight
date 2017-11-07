package com.ociweb.pronghorn.iot.i2c;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.impl.schema.TrafficAckSchema;
import com.ociweb.gl.impl.schema.TrafficReleaseSchema;
import com.ociweb.gl.impl.stage.AbstractTrafficOrderedStage;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.network.mqtt.MQTTClientGraphBuilder;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;
import com.ociweb.pronghorn.util.math.ScriptedSchedule;

public class I2CJFFIStage extends AbstractTrafficOrderedStage {
    
    public static boolean debugCommands = true;
    
    private final Pipe<I2CCommandSchema>[] fromCommandChannels;
    private final Pipe<I2CResponseSchema> i2cResponsePipe;
    
    private static final Logger logger = LoggerFactory.getLogger(I2CJFFIStage.class);
    private ScriptedSchedule schedule;
    
    private I2CConnection[] inputs = null;
    
    private byte[] workingBuffer;
    
    private int inProgressIdx = 0;
    private int scheduleIdx = 0;
    
    private long blockStartTime = 0;
    
    private boolean awaitingResponse = false;
    
    private static final int MAX_ADDR = 127;
    
    private final boolean processInputs;
    private Number rate;
    private long timeOut = 0;
    private final int writeTime = 10; //it often takes 1 full ms just to contact the linux driver so this value must be a minimum of 3ms.
    
    //NOTE: on the pi without any RATE value this stage is run every .057 ms, this is how long 1 run takes to complete for the clock., 2 analog sensors.
    
    public static final AtomicBoolean instanceCreated = new AtomicBoolean(false);
    
    public I2CJFFIStage(GraphManager graphManager, Pipe<TrafficReleaseSchema>[] goPipe,
            Pipe<I2CCommandSchema>[] i2cPayloadPipes,
            Pipe<TrafficAckSchema>[] ackPipe,
            Pipe<I2CResponseSchema> i2cResponsePipe,
            HardwareImpl hardware) {
        super(graphManager, hardware, i2cPayloadPipes, goPipe, ackPipe, i2cResponsePipe);
        
        assert(!instanceCreated.getAndSet(true)) : "Only one i2c manager can be running at a time";
        
        this.fromCommandChannels = i2cPayloadPipes;
        this.i2cResponsePipe = i2cResponsePipe;
        
        //force all commands to happen upon publish and release
        this.supportsBatchedPublish = false;
        this.supportsBatchedRelease = false;
        
        this.inputs = hardware.getI2CInputs();
        
        if (((HardwareImpl)this.hardware).hasI2CInputs()) {
            this.schedule = ((HardwareImpl)this.hardware).buildI2CPollSchedule();
            
            logger.info("I2C Schedule: {} ", this.schedule);
            
        } else {
            logger.debug("skipped buildI2CPollSchedule has no i2c inputs" );
        }
        
        if (null!=this.schedule) {
            //The fastest message that can ever be sent on I2C 100K is once every 1.6MS
        
			long computedRate = (this.schedule.commonClock);
			do {
				if (computedRate%10 == 0) {
					computedRate = computedRate/10;
				} else {
					if (computedRate%5 == 0) {
						computedRate = computedRate/5;
					} else {
						if (computedRate%2 == 0) {
							computedRate = computedRate/2;
						} else {
							break;
						}
					}					
				}
			} while (computedRate>2_000_000); //must not poll any slower than once every 2ms.

            GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, computedRate , this);
            logger.debug("setting JFFI to pol every: {} with schedule {}",computedRate,this.schedule);
        
        }else{
            logger.debug("Schedule is null");
        }
        
        rate = (Number)GraphManager.getNota(graphManager, this.stageId,  GraphManager.SCHEDULE_RATE, null);
        
        processInputs = hardware.hasI2CInputs() && hasListeners();
        
		GraphManager.addNota(graphManager, GraphManager.DOT_BACKGROUND, "darksalmon", this);

    }
    
    @Override
    public void startup(){
        super.startup();
        
        workingBuffer = new byte[2048]; //TODO: find a way to eliminate this temp storage.
        
        logger.debug("Polling "+this.inputs.length+" i2cInput(s)");
        
        for (int i = 0; i < inputs.length; i++) {
            setupSingleInput(i);
        }
        if (null!=schedule) {
            logger.debug("proposed schedule: {} ",schedule);
        }
        
        blockStartTime = hardware.nanoTime();//critical Pronghorn contract ensure this start is called by the same thread as run
        
        if (!hasListeners()) {
            logger.debug("No listeners are attached to I2C");
        }
    }
    
    private void setupSingleInput(int i) {
        if (null != inputs[i].setup) {
            assert(hardware!=null);
            I2CBacking i2cBacking = ((HardwareImpl)hardware).getI2CBacking();
            I2CConnection connection = inputs[i];
            assert(i2cBacking!=null);
            timeOut = hardware.nanoTime() + (writeTime*35_000_000);
            while(!i2cBacking.write(connection.address,
                    connection.setup,
                    connection.setup.length) && hardware.nanoTime()<timeOut){
            	
            		};
                    if (hardware.nanoTime()>timeOut) {
                        logger.warn("on setup failed to get I2C bus master, waited 35ms");
                        //timeout trying to get the i2c bus
                        return;
                    }
                    
                    if(connection.readBytesAtStartUp > 0){ // doing i2c read at start up
                        
                        long delayAfterRequestNS = connection.delayAfterRequestNS;
                        long delayUntil = hardware.nanoTime()+delayAfterRequestNS;
                        
                        if (delayAfterRequestNS>0) {
                            try {
                            	//some slow platforms will not sleep long enough so we spin yield below
                                Thread.sleep(delayAfterRequestNS/1_000_000,(int) (delayAfterRequestNS%1_000_000));
                                long dif;
                                while ((dif = (delayUntil-hardware.nanoTime()))>0) {
                                	if (dif>100) {
                                		Thread.yield();
                                	}
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                requestShutdown();
                                return;
                            }
            				
                        }
                        
                        PipeWriter.presumeWriteFragment(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10);
                        PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11, connection.address);
                        PipeWriter.writeLong(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13, hardware.currentTimeMillis());
                        PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14, connection.setup[0]);
                        
                        workingBuffer[0] = -2;//this is the non-read case read did not populate the array.
                        byte[] temp = i2cBacking.read(connection.address, workingBuffer, connection.readBytesAtStartUp);                        
                        PipeWriter.writeBytes(i2cResponsePipe, 
                        		  I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12, 
                        		  temp, 0, connection.readBytesAtStartUp, Integer.MAX_VALUE);
                        
                        
                        PipeWriter.publishWrites(i2cResponsePipe);
                        
                    }
        }
        logger.debug("I2C setup {} complete",inputs[i].address);
    }
    
    
    @Override
    public void run() {
        
        long prcRelease = hardware.nanoTime();
        
        
        //never run poll if we have nothing to poll, in that case the array will have a single -1
        if (processInputs) {
            do {
                long waitTime = blockStartTime - hardware.nanoTime();
                
                //logger.info("wait time before continue {},",waitTime);
                
                if(waitTime>0){
                    if (null==rate || (waitTime > rate.longValue())) {
                        if (hardware.nanoTime()>prcRelease) {
                            processReleasedCommands(waitTime);
                        }
                        return; //Enough time has not elapsed to start next block on schedule
                    } else {
                    	int padding = 200; //ns
                        long block = ((hardware.nanoTime()-blockStartTime)) - padding;
                        if (block > 0) {
                            try {
                                Thread.sleep(block/1_000_000,(int)(block%1_000_000));
                                long dif;
                                while ((dif = (blockStartTime-hardware.nanoTime()))>0) {
                                	if (dif>100) {
                                		Thread.yield();
                                	}
                                }
                            } catch (InterruptedException e) {
                                requestShutdown();
                                return;
                            }//some slow platforms will NOT sleep long enough above so we spin below.
                        }
                    }
                }
                
                I2CBacking i2cBacking = ((HardwareImpl)hardware).getI2CBacking();
                
                do{
                    inProgressIdx = schedule.script[scheduleIdx];
                    
                    if(inProgressIdx != -1) {
                        
                        if (!PipeWriter.hasRoomForWrite(i2cResponsePipe)) {
                            if (hardware.nanoTime()>prcRelease) {
                                //we are going to miss the schedule due to backup in the pipes, this is common when the unit tests run or the user has put in a break point.
                                processReleasedCommands(rate.longValue());//if this backup runs long term we never release the commands so we must do it now.
                            }
                            logger.warn("outgoing pipe is backed up, unable to read new data  {}"+i2cResponsePipe);
                            return;//oops the pipe is full so we can not read, postpone this work until the pipe is cleared.
                        }
                        
                        I2CConnection connection = this.inputs[inProgressIdx];
                        timeOut = hardware.nanoTime() + (writeTime*35_000_000);///I2C allows for clients to abandon master after 35 ms
                        
                        //          logger.info("i2c request read from address: {} register: {} ",connection.address, connection.readCmd[0]);//+Arrays.toString(Arrays.copyOfRange(connection.readCmd, 0, connection.readCmd.length)));
                        
                        //Write the request to read
                        
                        while(!i2cBacking.write((byte)connection.address, connection.readCmd, connection.readCmd.length) && hardware.nanoTime()<timeOut){}
                        
                        if (hardware.nanoTime()>timeOut) {
                            logger.warn("on write failed to get I2C bus master, waited 35ms");
                            //timeout trying to get the i2c bus
                            return;
                        }
                        
                        long delayAfterRequestNS = this.inputs[inProgressIdx].delayAfterRequestNS;
                        long delayUntil = hardware.nanoTime()+delayAfterRequestNS;
                        
                        if (delayAfterRequestNS>0) {
                            try {
                            	//some slow platforms will not sleep long enough so we spin yield below
                                Thread.sleep(delayAfterRequestNS/1_000_000,(int) (delayAfterRequestNS%1_000_000));
                                long dif;
                                while ((dif = (delayUntil-hardware.nanoTime()))>0) {
                                	if (dif>100) {
                                		Thread.yield();
                                	}
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                requestShutdown();
                                return;
                            }
                            
                            
                        }

                        //logger.info("i2c reading result {} delay before read {} ",Arrays.toString(Arrays.copyOfRange(temp, 0, this.inputs[inProgressIdx].readBytes )),this.inputs[inProgressIdx].delayAfterRequestNS);
                        
                        PipeWriter.presumeWriteFragment(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10);
                        PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11, this.inputs[inProgressIdx].address);
                        PipeWriter.writeLong(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13, hardware.currentTimeMillis());
                        PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14, this.inputs[inProgressIdx].register);
                        
                        workingBuffer[0] = -2;
                        byte[] temp = i2cBacking.read(this.inputs[inProgressIdx].address, workingBuffer, this.inputs[inProgressIdx].readBytes);                       
                        PipeWriter.writeBytes(i2cResponsePipe, 
                        		   I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12, 
                        		   temp, 0, this.inputs[inProgressIdx].readBytes, Integer.MAX_VALUE);
                        
                        PipeWriter.publishWrites(i2cResponsePipe);
                        
                        
                    } else {
                        if (rate.longValue()>500_000) {
                            if (hardware.nanoTime()>prcRelease) {
                                processReleasedCommands(rate.longValue());
                                prcRelease+=rate.longValue();
                            }
                        }
                    }
                    //since we exit early if the pipe is full we must not move this forward until now at the bottom of the loop.
                    scheduleIdx = (scheduleIdx+1) % schedule.script.length;
                }while(inProgressIdx != -1);
                blockStartTime += schedule.commonClock;
                
            } while (true);
        } else {
            
            //System.err.println("nothing to poll, should choose a simpler design");
            if (hardware.nanoTime()>prcRelease) {
                processReleasedCommands(rate.longValue());
                prcRelease+=rate.longValue();
            }
        }
    }
    
    private boolean hasListeners() {
        return i2cResponsePipe != null;
    }
    
    protected void processMessagesForPipe(int a) {
        sendOutgoingCommands(a);
        
    }
    
    private void sendOutgoingCommands(int activePipe) {
        
        if(activePipe == -1){
            return; //No active pipe selected yet
        }
        
        Pipe<I2CCommandSchema> pipe = fromCommandChannels[activePipe];
        
//		logger.info("i2c while: {} {} {} {} {} {}",
//				activePipe,
//				hasReleaseCountRemaining(activePipe),
//				isChannelUnBlocked(activePipe),
//				isConnectionUnBlocked(PipeReader.peekInt(pipe, 1)),
//				PipeReader.hasContentToRead(pipe),
//				pipe
//				);
I2CBacking i2cBacking = ((HardwareImpl)hardware).getI2CBacking();

while ( hasReleaseCountRemaining(activePipe)
        && isChannelUnBlocked(activePipe)
        && PipeReader.hasContentToRead(pipe)
        && isConnectionUnBlocked(PipeReader.peekInt(pipe, 1)) //peek next connection and check that it is not blocking for some time
        && PipeReader.tryReadFragment(pipe)){
    
    int msgIdx = PipeReader.getMsgIdx(pipe);
    
    switch(msgIdx){
        case I2CCommandSchema.MSG_COMMAND_7:
        {
            int connection = PipeReader.readInt(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11);
            assert isConnectionUnBlocked(connection): "expected command to not be blocked";
            
            int addr = PipeReader.readInt(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12);
            
            byte[] backing = PipeReader.readBytesBackingArray(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            int len  = PipeReader.readBytesLength(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            int pos = PipeReader.readBytesPosition(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            int mask = PipeReader.readBytesMask(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            
            //must grow if calls are needing more room.
            if (workingBuffer.length < len) {
            	workingBuffer = new byte[len*2];
            }

            Pipe.copyBytesFromToRing(backing, pos, mask, 
            		                 workingBuffer, 0, Integer.MAX_VALUE, 
            		                 len);
            
            if (debugCommands) {
                logger.info("{} pipe {} send addr {} command {} {}",Appendables.appendEpochTime(new StringBuilder(), System.currentTimeMillis())
                		              ,activePipe, addr, Appendables.appendArray(new StringBuilder(), '[', backing, pos, mask, ']', len), pipe);
            }
            
            //    logger.info("i2c request write to address: {} register: {}  ",addr, workingBuffer[0]);//+Arrays.toString(Arrays.copyOfRange(connection.readCmd, 0, connection.readCmd.length)));
            
            
            timeOut = hardware.currentTimeMillis() + writeTime;
            
            while(!i2cBacking.write((byte) addr, workingBuffer, len) && hardware.currentTimeMillis()<timeOut){}
            
        }
        break;
        
        case I2CCommandSchema.MSG_BLOCKCHANNEL_22:
        {
            hardware.blockChannelDuration(PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCHANNEL_22_FIELD_DURATIONNANOS_13), goPipeId(activePipe));
            if (debugCommands) {
                logger.info("CommandChannel blocked for {} millis ",PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCHANNEL_22_FIELD_DURATIONNANOS_13));
            }
        }
        break;
        
        case I2CCommandSchema.MSG_BLOCKCONNECTION_20:
        {
            int connection = PipeReader.readInt(pipe, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11);
            assert isConnectionUnBlocked(connection): "expected command to not be blocked";
            
            int addr = PipeReader.readInt(pipe, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12);
            long duration = PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13);
            
            blockConnectionDuration(connection, duration);
            if (debugCommands) {
                logger.info("I2C addr {} {} blocked for {} nanos  {}", addr, connection, duration, pipe);
            }
        }
        break;
        
        case I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21:
        {
            int connection = PipeReader.readInt(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_CONNECTOR_11);
            int addr = PipeReader.readInt(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_ADDRESS_12);
            long time = PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_TIMEMS_14);
            blockConnectionUntil(connection, time);
            if (debugCommands) {
                logger.info("I2C addr {} {} blocked until {} millis {}", addr, connection, time, pipe);
            }
        }
        
        break;
        case -1 :
            requestShutdown();
            
    }
    PipeReader.releaseReadLock(pipe);
    
    //only do now after we know its not blocked and was completed
    decReleaseCount(activePipe);
    
}

    }
    
    
    
}