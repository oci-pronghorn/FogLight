package com.ociweb.iot.examples;


import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogApp;

public class IoTApp implements FogApp
{
	private static String brokerURI;
	private static String clientId;
    private static String kafkaURI;

    public static void main( String[] args ) {
    	
    	//parse the optional command line arguments
    	brokerURI = FogRuntime.getOptArg("--brokerURI", "-br", args, "tcp://localhost:1883");;
    	clientId = FogRuntime.getOptArg("--clientId", "-id", args, "unknownGateway");
        kafkaURI = FogRuntime.getOptArg("--kafkaURI", "-ku", args, "localhost:9092");


        FogRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {       
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {

    	runtime.addStartupListener(new SubscribeDataMQTT(runtime, "#", "localPub", brokerURI, clientId ));
    	
    	runtime.addPubSubListener(new PublishKafka(runtime, kafkaURI, clientId)).addSubscription("localPub");
    
    }
        
  
}
