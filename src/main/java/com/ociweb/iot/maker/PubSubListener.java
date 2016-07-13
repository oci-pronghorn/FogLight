package com.ociweb.iot.maker;

public interface PubSubListener {

    public void message(CharSequence topic, PayloadReader payload);
    
    
}
