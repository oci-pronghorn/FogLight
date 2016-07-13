package com.ociweb.iot.maker;

import com.ociweb.pronghorn.iot.schema.MessageSubscription;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.Pipe;

public class PayloadReader extends DataInputBlobReader<MessageSubscription>{

    public PayloadReader(Pipe<MessageSubscription> pipe) {
        super(pipe);
    }

}
