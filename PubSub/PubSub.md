# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a simple use of the ```addPubSubListener()``` method.

Demo code: 


```java
package com.ociweb.oe.foglight.api;

import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import java.util.ArrayList;
import java.util.Random;

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
```
The above code will generate seven random, lucky numbers. The first ```addPubSubListener()``` will generate a random number and add it to ArrayList ```luckyNums```. Once that has occured, it will publish a message uner the topic of "Gen", which the second PubSubListener is subscribed to, meaning that is is always listening for any publication under that topic. The second PubSubListener will simply print out the newest lucky number, then publish a message under the topic of "Print", which the first PubSubListener is subscribed to, restarting the process for a total of seven rounds.

