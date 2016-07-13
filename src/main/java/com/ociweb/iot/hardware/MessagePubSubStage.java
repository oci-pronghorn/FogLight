package com.ociweb.iot.hardware;

import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.MessageSubscription;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class MessagePubSubStage extends AbstractOutputStage {

    private final Pipe<TrafficReleaseSchema>[] goPipe;
    private final Pipe<TrafficAckSchema>[] ackPipe; 
    private final Pipe<MessagePubSub>[] incomingSubsAndPubsPipe;
    private final Pipe<MessageSubscription>[] outgoingMessagePipes;
    
    
    public MessagePubSubStage(GraphManager gm, Hardware hardware, Pipe<MessagePubSub>[] incomingSubsAndPubsPipe,
                              Pipe<TrafficReleaseSchema>[] goPipe,
                              Pipe<TrafficAckSchema>[] ackPipe, 
                              Pipe<MessageSubscription>[] outgoingMessagePipes) {
       super(gm, hardware, incomingSubsAndPubsPipe, goPipe, ackPipe, outgoingMessagePipes);
    
       this.goPipe = goPipe;
       this.ackPipe = ackPipe;
       this.incomingSubsAndPubsPipe = incomingSubsAndPubsPipe;
       this.outgoingMessagePipes = outgoingMessagePipes;
       
       assert(goPipe.length == ackPipe.length) : "should be one ack pipe for every go pipe";
       
       //TODO: we know the max subseribers so build the lists as needed.
       
       
    }

    @Override
    protected void processMessagesForPipe(int a) {
        Pipe<MessagePubSub> pipe = incomingSubsAndPubsPipe[a];
        while (hasReleaseCountRemaining(a) &&
               PipeReader.tryReadFragment(pipe) 
              ) {
            
            int msgIdx = PipeReader.getMsgIdx(pipe);
            switch (msgIdx)  {
                case MessagePubSub.MSG_PUBLISH_103:
                
                    //find which pipes have subscribed to this string
                    //First iteration we will sent all listeners all publications
              
                    int x = outgoingMessagePipes.length;
                    while (--x>=0) {
                    
                        //TODO: a lot more work needs to be done here.
                        
                        Pipe<MessageSubscription> outPipe = outgoingMessagePipes[x];
                        while(!PipeWriter.tryWriteFragment(outPipe, MessageSubscription.MSG_PUBLISH_103)) {};//very very bad
                        
                        PipeReader.copyBytes(pipe, outPipe, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1, MessageSubscription.MSG_PUBLISH_103_FIELD_TOPIC_1);
                        PipeReader.copyBytes(pipe, outPipe, MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3, MessageSubscription.MSG_PUBLISH_103_FIELD_PAYLOAD_3);
                        
                        PipeWriter.publishWrites(outPipe);
                        
                    }
                    
                    break;
                case MessagePubSub.MSG_SUBSCRIBE_100:
                    
                    //TODO: add this pipe as subscribed to this string.
                    
                    
                    break;
                case MessagePubSub.MSG_UNSUBSCRIBE_101:
                    throw new UnsupportedOperationException("This feature will be added after full wildcard support is added. dependent feature.");
                    //break;
                
                
            }
            
            PipeReader.releaseReadLock(pipe);
            
            
            
            decReleaseCount(a);
            
        }
    }

}
