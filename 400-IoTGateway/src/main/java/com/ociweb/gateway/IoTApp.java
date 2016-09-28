package com.ociweb.gateway;


import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;

public class IoTApp implements IoTSetup
{
  
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {       
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {

    	runtime.addStartupListener(new SubscribeDataMQTT(runtime, "#", "localPub", "tcp://localhost:1883", "TestClient"));;
    	
    	runtime.addPubSubListener(new PublishKafka(runtime,"kafkaTopic")).addSubscription("localPub");
    
    }
        
  
}
