# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

Demo code:

```java
package com.ociweb.oe.foglight.api;


import com.ociweb.gl.api.HTTPSession;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class HTTPClient implements FogApp
{

    @Override
    public void declareConnections(Hardware c) {   
    	c.useNetClient();
    	//c.enableTelemetry();
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {       
    	HTTPSession session = new HTTPSession("www.objectcomputing.com",80,0);
    	
    	session = new HTTPSession("www.objectcomputing.com",80,1);
    	HTTPGetBehaviorSingle temp = new HTTPGetBehaviorSingle(runtime, session);
		runtime.addStartupListener(temp).includeHTTPSession(session);
			   	
    	
    	runtime.addResponseListener(new HTTPResponse()).includeHTTPSession(session);
    	runtime.addStartupListener(new HTTPGetBehaviorChained(runtime, session));
    	
    	
    }
          
}
```

Behavior class:

```java
package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.HTTPSession;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

public class HTTPGetBehaviorChained implements StartupListener {
	
	private FogCommandChannel cmd;
	
    private HTTPSession session;
	
	public HTTPGetBehaviorChained(FogRuntime runtime, HTTPSession session) {
		this.cmd = runtime.newCommandChannel(NET_REQUESTER);
		this.session = session;
	}

	@Override
	public void startup() {
		
		cmd.httpGet(session, "/");
		
	}

}
```


```java
package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.HTTPResponseListener;
import com.ociweb.gl.api.HTTPResponseReader;
import com.ociweb.gl.api.HTTPSession;
import com.ociweb.gl.api.Payloadable;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.network.config.HTTPContentType;
import com.ociweb.pronghorn.pipe.ChannelReader;

public class HTTPGetBehaviorSingle implements StartupListener, HTTPResponseListener {

	
	private final FogCommandChannel cmd;
	private HTTPSession session;
	 
	public HTTPGetBehaviorSingle(FogRuntime runtime, HTTPSession session) {
		cmd = runtime.newCommandChannel(NET_REQUESTER);
		this.session = session;
	}

	@Override
	public void startup() {
		cmd.httpGet(session, "/");
	}

	@Override
	public boolean responseHTTP(HTTPResponseReader reader) {
		
		System.out.println(" status:"+reader.statusCode());
		System.out.println("   type:"+reader.contentType());
		
		Payloadable payload = new Payloadable() {
			@Override
			public void read(ChannelReader reader) {
				System.out.println(reader.readUTFOfLength(reader.available()));
			}
		};
		
		reader.openPayloadData( payload );
		
		return true;
	}

}
```


```java
package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.HTTPResponseListener;
import com.ociweb.gl.api.HTTPResponseReader;
import com.ociweb.gl.api.Payloadable;
import com.ociweb.pronghorn.network.config.HTTPContentType;
import com.ociweb.pronghorn.pipe.ChannelReader;

public class HTTPResponse implements HTTPResponseListener {

	@Override
	public boolean responseHTTP(HTTPResponseReader reader) {
		
		System.out.println(" status:"+reader.statusCode());
		System.out.println("   type:"+reader.contentType());

		Payloadable payload = new Payloadable() {
			@Override
			public void read(ChannelReader reader) {
				System.out.println(reader.readUTFOfLength(reader.available()));
			}
		};
		boolean hadAbody = reader.openPayloadData(payload );

		
		return true;
	}

}
```


