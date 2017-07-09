package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.PubSubStructuredWritable;
import com.ociweb.gl.api.PubSubStructuredWriter;
import com.ociweb.gl.impl.pubField.IntegerFieldProcessor;
import com.ociweb.gl.impl.pubField.MessageConsumer;
import com.ociweb.gl.impl.pubField.UTF8FieldProcessor;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

public class DecrementValueBehavior implements PubSubListener {

	private final FogCommandChannel channel;
    private final MessageConsumer consumer;
    private long lastValue;
    private final CharSequence publishTopic;
    private final FogRuntime runtime;
    private final long decrementBy;
		
    DecrementValueBehavior(FogRuntime runtime, CharSequence publishTopic, long decrementBy) {
    	this.channel = runtime.newCommandChannel(DYNAMIC_MESSAGING);

    	// Process each field in order. Return false to stop processing.
		this.consumer = new MessageConsumer()
				            .integerProcessor(PubSubStructured.COUNT_DOWN_FIELD, value -> {
								lastValue = (int) value;
								return true;
							});
		
		this.publishTopic = publishTopic;
		this.runtime = runtime;
		this.decrementBy = decrementBy;
	}
    
    private final PubSubStructuredWritable writable = new PubSubStructuredWritable() {
    	@Override
    	public void write(PubSubStructuredWriter writer) {
    		writer.writeLong(PubSubStructured.COUNT_DOWN_FIELD, lastValue-decrementBy);
    		writer.writeUTF8(PubSubStructured.SENDER_FIELD, "from thing one behavior");
    	}			
    };

	@Override
	public boolean message(CharSequence topic, MessageReader payload) {
		//
		////NOTE: this one line will copy messages from payload if consumer returns true
		////      when the message is copied its topic is changed to the first argument string
		//
		//cmd.copyStructuredTopic(publishTopic, payload, consumer);
		//
		// consumer.process returns the process chain return value
		if (consumer.process(payload)) {
			if (lastValue>0) {
				System.out.println(lastValue);
				return channel.publishStructuredTopic(publishTopic, writable);
			} else {
				runtime.shutdownRuntime();
				return true;
			} 
		} else {
			return false;
		}
		
	}

}
