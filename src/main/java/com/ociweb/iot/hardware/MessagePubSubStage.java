package com.ociweb.iot.hardware;

import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class MessagePubSubStage extends PronghornStage {

    public MessagePubSubStage(GraphManager gm, Pipe<MessagePubSub>[] messagePubSub, Pipe<TrafficReleaseSchema>[] masterMsggoOut,
            Pipe<TrafficAckSchema>[] masterMsgackIn) {
       super(gm, join(messagePubSub, masterMsggoOut), masterMsgackIn);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }

}
