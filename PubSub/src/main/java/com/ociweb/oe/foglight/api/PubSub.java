package com.ociweb.oe.foglight.api;

import java.util.ArrayList;
import java.util.Random;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class PubSub implements FogApp
{
	ArrayList<Integer> luckyNums = new ArrayList<>();
	Random rand = new Random();
	
	public static int count = 0;

	
    @Override
    public void declareConnections(Hardware c) {
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {

    	final FogCommandChannel channel0 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
    	
    	runtime.addStartupListener(()->{
    		System.out.println("Your lucky numbers are ...");
    		channel0.publishTopic("Starter", writable->{});
    	});
    	
    	
    	final FogCommandChannel channel1 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
    	
    	runtime.addPubSubListener((topic, payload)-> {
    		
    		int n = rand.nextInt(101);
    		luckyNums.add(n);
    		
    		channel1.publishTopic("Gen", writable->{});
    		channel1.block(500);

    		return true;
    	}).addSubscription("Print").addSubscription("Starter");
    	
    	
    	final FogCommandChannel channel2 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
    	
    	runtime.addPubSubListener((topic, payload) -> {
    		System.out.print(luckyNums.get(count) + " ");
    		count++;
    		if(count<7){
    			channel2.publishTopic("Print", writable->{});
    		}
    		
    		return true;	
    	}).addSubscription("Gen");
    }
          
}
