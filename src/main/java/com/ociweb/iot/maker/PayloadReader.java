package com.ociweb.iot.maker;

import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.Pipe;

@SuppressWarnings("rawtypes")
public class PayloadReader extends DataInputBlobReader{

    public PayloadReader(Pipe pipe) {
        super(pipe);
    }
    
	//                    if (! payloadReader.markSupported() ) {
	//                        logger.warn("we need mark to be suppported for payloads in pubsub and http."); //TODO: need to implement mark, urgent.                      
	//                    }

}
