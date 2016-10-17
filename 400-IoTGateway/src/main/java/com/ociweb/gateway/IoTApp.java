package com.ociweb.gateway;


import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;

public class IoTApp implements IoTSetup
{
	private static String brokerURI;
	private static String clientId;
    private static String kafkaURI;

    public static void main( String[] args ) {
    	
    	//parse the optional command line arguments
    	brokerURI = DeviceRuntime.getOptArg("--brokerURI", "-br", args, "tcp://localhost:1883");;
    	clientId = DeviceRuntime.getOptArg("--clientId", "-id", args, "unknownGateway");
        kafkaURI = DeviceRuntime.getOptArg("--kafkaURI", "-ku", args, "localhost:9092");


        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {       
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {

    	runtime.addStartupListener(new SubscribeDataMQTT(runtime, "#", "localPub", brokerURI, clientId ));
    	
    	runtime.addPubSubListener(new PublishKafka(runtime, kafkaURI, clientId)).addSubscription("localPub");
    
    }
        
  
}
