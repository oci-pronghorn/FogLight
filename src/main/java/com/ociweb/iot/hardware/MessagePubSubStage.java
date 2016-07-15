package com.ociweb.iot.hardware;

import java.util.Arrays;

import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.MessageSubscription;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.util.hash.IntHashTable;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.TrieParser;
import com.ociweb.pronghorn.util.TrieParserReader;

public class MessagePubSubStage extends AbstractOutputStage {

    private final Pipe<MessagePubSub>[] incomingSubsAndPubsPipe;
    private final Pipe<MessageSubscription>[] outgoingMessagePipes;
    
    private static final int estimatedTopicLength = 100;
    private static final int maxLists = 10; //TODO: make this grow later
    private final int subscriberListSize;
    private short[] subscriberLists;
    private int totalSubscriberLists;
    
    private TrieParser trie; 
    private TrieParserReader trieReader;
    
    private IntHashTable subscriptionPipeLookup;
    
    private int[] pendingPublish;
    private int pendingPublishCount;
    private int pendingReleaseCountIdx;
    
    public MessagePubSubStage(GraphManager gm, IntHashTable subscriptionPipeLookup, Hardware hardware, Pipe<MessagePubSub>[] incomingSubsAndPubsPipe,
                              Pipe<TrafficReleaseSchema>[] goPipe,
                              Pipe<TrafficAckSchema>[] ackPipe, 
                              Pipe<MessageSubscription>[] outgoingMessagePipes) {
       super(gm, hardware, incomingSubsAndPubsPipe, goPipe, ackPipe, outgoingMessagePipes);

       this.incomingSubsAndPubsPipe = incomingSubsAndPubsPipe;
       this.outgoingMessagePipes = outgoingMessagePipes;
       
       assert(goPipe.length == ackPipe.length) : "should be one ack pipe for every go pipe";
       
       this.subscriberListSize = outgoingMessagePipes.length;//can never hava more subscribers than ALL
       this.totalSubscriberLists = 0;
       this.subscriptionPipeLookup = subscriptionPipeLookup;

    }
    
    @Override
    public void startup() {
        super.startup();
        
        this.subscriberLists = new short[maxLists*subscriberListSize];       
        Arrays.fill(this.subscriberLists, (short)-1);
        this.trie = new TrieParser(maxLists * estimatedTopicLength );
        this.trieReader = new TrieParserReader();
        this.pendingPublish = new int[subscriberListSize];
    }
    

    @Override
    public void run() {
        if (pendingPublishCount>0) { //must do these first.
            
            int limit = pendingPublishCount;
            pendingPublishCount = 0;//set to zero to collect the new failed values
            
            Pipe<MessagePubSub> pipe = incomingSubsAndPubsPipe[pendingReleaseCountIdx];
            for(int i = 0; i<limit; i++) {
                copyToSubscriber(pipe, pendingPublish[i]);                
            }
            if (pendingPublishCount>0) {
                return;//try again later
            } else {
                //now done with this message so release it and send the ack
                PipeReader.releaseReadLock(pipe);
                decReleaseCount(pendingReleaseCountIdx);
            }
        }        
        super.run();
    }
    
    @Override
    protected void processMessagesForPipe(int a) {
        
        //TODO: still need to add support for +
        //TODO: still need to add support for #
        
        
        Pipe<MessagePubSub> pipe = incomingSubsAndPubsPipe[a];
        while (hasReleaseCountRemaining(a) &&
               PipeReader.tryReadFragment(pipe) 
              ) {
            
            int msgIdx = PipeReader.getMsgIdx(pipe);
            switch (msgIdx)  {
                case MessagePubSub.MSG_PUBLISH_103:
                    {
                        
                        //find which pipes have subscribed to this topic
                        final byte[] backing = PipeReader.readBytesBackingArray(pipe, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1);
                        final int pos = PipeReader.readBytesPosition(pipe, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1);
                        final int len = PipeReader.readBytesLength(pipe, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1);
                        final int mask = PipeReader.readBytesMask(pipe, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1);
                        
                        int listIdx = (int) TrieParserReader.query(trieReader, trie, backing, pos, len, mask);
                        if (listIdx>=0) {
                            for(int i = listIdx; i<(listIdx+subscriberListSize) && (-1 != subscriberLists[i]); i++) {
                                copyToSubscriber(pipe, subscriberLists[i]);                                
                            }
                            
                            if (pendingPublishCount>0) {
                                pendingReleaseCountIdx = a; //keep so this is only cleared after we have had successful transmit to all subscribers.
                                return;//must try again later
                            }                            
                            
                        }
                    }   
                    break;
                case MessagePubSub.MSG_SUBSCRIBE_100:
                    {
                        final short pipeIdx = (short)IntHashTable.getItem(subscriptionPipeLookup, PipeReader.readInt(pipe, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_SUBSCRIBERIDENTITYHASH_4));
                                
                        assert(pipeIdx>=0) : "Must have valid pipe index";
                        
                        final byte[] backing = PipeReader.readBytesBackingArray(pipe, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_TOPIC_1);
                        final int pos = PipeReader.readBytesPosition(pipe, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_TOPIC_1);
                        final int len = PipeReader.readBytesLength(pipe, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_TOPIC_1);
                        final int mask = PipeReader.readBytesMask(pipe, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_TOPIC_1);
                        
                       
                        int listIdx = (int) TrieParserReader.query(trieReader, trie, backing, pos, len, mask);
                        
                        if (listIdx<0) {
                            //create new subscription
                            listIdx = subscriberListSize*totalSubscriberLists++;
                            trie.setValue(backing, pos, len, mask, listIdx);
                        }
                        
                        //add index on first -1 or stop if value already found                    
                        for(int i = listIdx; i<(listIdx+subscriberListSize); i++) {
                            if (-1 == subscriberLists[i]) {
                                subscriberLists[i]=pipeIdx;
                                break;
                            } else if (pipeIdx == subscriberLists[i]){
                                break;//already in list.
                            }
                        }
                    }              
                    break;
                case MessagePubSub.MSG_UNSUBSCRIBE_101:
                    
                    
                    throw new UnsupportedOperationException("This feature will be added after full wildcard support is added. dependent feature.");
                    //break;
                
                
            }
            
            PipeReader.releaseReadLock(pipe);
            decReleaseCount(a);
            
        }
    }

    private void copyToSubscriber(Pipe<MessagePubSub> pipe, int pipeIdx) {
        Pipe<MessageSubscription> outPipe = outgoingMessagePipes[pipeIdx];
        if (PipeWriter.tryWriteFragment(outPipe, MessageSubscription.MSG_PUBLISH_103)) {
            
            PipeReader.copyBytes(pipe, outPipe, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1, MessageSubscription.MSG_PUBLISH_103_FIELD_TOPIC_1);
            PipeReader.copyBytes(pipe, outPipe, MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3, MessageSubscription.MSG_PUBLISH_103_FIELD_PAYLOAD_3);
            
            PipeWriter.publishWrites(outPipe);
        } else {
            pendingPublish[pendingPublishCount++] = pipeIdx;                                    
        }
    }

}
