package com.ociweb.iot.trafficlight;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.LED;
import static com.ociweb.iot.maker.Port.D3;
import static com.ociweb.iot.maker.Port.D5;
import static com.ociweb.iot.maker.Port.D6;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.HTTPResponseService;
import com.ociweb.gl.api.PubSubService;
import com.ociweb.iot.grove.lcd_rgb.Grove_LCD_RGB;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.network.NetGraphBuilder;
public class IoTApp implements FogApp
{
	private static final Port LED3_PORT = D5;
	private static final Port LED1_PORT = D3;
	private static final Port LED2_PORT = D6;	

	public static int RED_MS = 10000;
	public static int GREEN_MS = 8000;
	public static int YELLOW_MS = 2000;

	private boolean isWebControlled = false;////set this to true;

	private int webRoute = -1;
	private long COLOR;

	private byte[] RED = "red".getBytes();
	private byte[] GREEN = "green".getBytes();
	private byte[] YELLOW = "yellow".getBytes();


	private enum State {
		REDLIGHT(RED_MS), 
		GREENLIGHT(GREEN_MS),
		YELLOWLIGHT(YELLOW_MS);
		private int deltaTime;
		State(int deltaTime){this.deltaTime=deltaTime;}
		public int getTime(){return deltaTime;}
	}

    public static void main(String[] args) {
    	FogRuntime.run(new IoTApp());
    }
	
	
    @Override
    public void declareConnections(Hardware c) {
		c.connect(LED, LED1_PORT);
		c.connect(LED, LED2_PORT);
		c.connect(LED, LED3_PORT);

		if (isWebControlled) {
			c.useHTTP1xServer(8088).setHost(NetGraphBuilder.bindHost(null));
						
		//	c.enableTelemetry(true);			
			webRoute = c.defineRoute().path("/trafficLight?color=${color}").routeId();
			COLOR = c.lookupFieldByName(webRoute, "color");
		}

	}


	@Override
	public void declareBehavior(FogRuntime runtime) {

		if (isWebControlled) {
			configureWebBasedColorChange(runtime); 
		} else {
			configureTimeBasedColorChange(runtime);
		}


	}


	private void configureWebBasedColorChange(FogRuntime runtime) {
		final FogCommandChannel channel = runtime.newCommandChannel(
									    FogRuntime.PIN_WRITER |
									    FogRuntime.I2C_WRITER );
		HTTPResponseService responseService = channel.newHTTPResponseService();

		runtime.addRestListener((reader)->{

									 if (reader.structured().isEqual(COLOR, RED)) {
										 return responseService.publishHTTPResponse(reader, turnOnRed(channel) ? 200 : 500);
										 
									 } else if (reader.structured().isEqual(COLOR, GREEN)) {
										 return responseService.publishHTTPResponse(reader, turnOnGreen(channel) ? 200 : 500);
										 
									 } else if (reader.structured().isEqual(COLOR, YELLOW)) {
										 return responseService.publishHTTPResponse(reader, turnOnYellow(channel) ? 200 : 500);
										 
									 } else {
										 
										 return responseService.publishHTTPResponse(reader, 404);
										 
									 }}).includeRoutes(webRoute);

	}


	protected void configureTimeBasedColorChange(FogRuntime runtime) {
		final FogCommandChannel channel0 = runtime.newCommandChannel(
				 									FogRuntime.PIN_WRITER |
				 									FogRuntime.I2C_WRITER);
		PubSubService pubService = channel0.newPubSubService();
		runtime.addPubSubListener((topic, payload)-> {

			turnOnRed(channel0);
			channel0.block(State.REDLIGHT.getTime());
			pubService.publishTopic("GREEN",w->{});
			return true;
		}).addSubscription("RED");

		final FogCommandChannel channel1 = runtime.newCommandChannel(
														FogRuntime.PIN_WRITER |
														FogRuntime.I2C_WRITER);
		PubSubService pubSubService = channel1.newPubSubService();
		
		
		runtime.addPubSubListener((topic, payload)-> {

			turnOnGreen(channel1);
			channel1.block(State.GREENLIGHT.getTime());
			
			pubSubService.publishTopic("YELLOW",w->{});
			return true;
		}).addSubscription("GREEN");

		final FogCommandChannel channel2 = runtime.newCommandChannel(
													 FogRuntime.PIN_WRITER |
													 FogRuntime.I2C_WRITER);
		PubSubService pubService2 = channel2.newPubSubService();
		
		runtime.addPubSubListener((topic, payload)-> {

			turnOnYellow(channel2);
			channel2.block(State.YELLOWLIGHT.getTime());
			
			pubService2.publishTopic("RED",w->{});
			return true;
    	}).addSubscription("YELLOW");
    	
       final FogCommandChannel channel4 = runtime.newCommandChannel(
    		                                       FogRuntime.PIN_WRITER |
    		                                       FogRuntime.I2C_WRITER |
    		                                       GreenCommandChannel.DYNAMIC_MESSAGING);
       PubSubService pubService4 = channel4.newPubSubService();
       
       runtime.addStartupListener(()->{pubService4.publishTopic("RED",w->{});});

	}


	private boolean turnOnGreen(final FogCommandChannel c) {
		return 
				c.setValue(LED1_PORT, 0) |
				c.setValue(LED2_PORT, 0) |
				c.setValue(LED3_PORT, 1) |
				Grove_LCD_RGB.commandForTextAndColor(c, "GREEN",0, 255, 0);
	}


	private boolean turnOnYellow(final FogCommandChannel c) {
		return
				c.setValue(LED1_PORT, 0) |
				c.setValue(LED2_PORT, 1) |
				c.setValue(LED3_PORT, 0) |
				Grove_LCD_RGB.commandForTextAndColor(c,"YELLOW", 255, 255, 0);
	}


	private boolean turnOnRed(final FogCommandChannel c) {
		return
				c.setValue(LED1_PORT, 1) |
				c.setValue(LED2_PORT, 0) |
				c.setValue(LED3_PORT, 0) |
				Grove_LCD_RGB.commandForTextAndColor(c, "RED", 255, 0, 0);
	}



}
