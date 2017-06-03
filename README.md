# FogLight Embedded Toolkit #
FogLight is a lightweight runtime that enables makers of all ages and skill levels to create highly performant apps for embedded devices like Raspberry Pis.

## What It Is ##
FogLight is a Java 8 functional API for embedded systems that's built on top of [GreenLightning](https://github.com/oci-pronghorn/GreenLightning), a small footprint, garbage free compact 1 Java web server and message routing platform, 

FogLight is...
- Fast - Built on top of GreenLightning, PET is a garbage-free, lock-free and low latency way to talk directly to hardware.
- Simple - Taking advantage of the latest Java 8 APIs, PET has a clean and fluent set of APIs that make it easy to learn and apply with minimal training.
- Secure - By taking advantage of the compile-time graph validation system, all FogLight applications can be compiled and compressed to a point where injecting malicious code into the final production JAR would prove difficult, if not impossible.

## How It Works ##
Every FogLight application starts with an `IoTSetup` implementation which initializes the `DeviceRuntime` by defining various hardware connections and behaviors for handling state changes in those connections.  

A very simple example of a FogLight application is below (omitting boilerplate import statements and so on); this app makes an LED connected to a GrovePi board blink every 500 milliseconds:

    public class IoTApp implements IoTSetup {
        
        private static final String TOPIC = "light";
        private static final int PAUSE = 500;    
        public static final Port LED_PORT = D4;
               
        public static void main( String[] args) {
            DeviceRuntime.run(new IoTApp());
        }    
        
        @Override
        public void declareConnections(Hardware c) {
            c.connect(LED, D4);
        }
    
        @Override
        public void declareBehavior(DeviceRuntime runtime) {
            
            final CommandChannel blinkerChannel = runtime.newCommandChannel();        
            runtime.addPubSubListener((topic,payload)->{
                
                boolean value = payload.readBoolean();
                blinkerChannel.setValueAndBlock(LED_PORT, value?1:0, PAUSE);               
                PayloadWriter writer = blinkerChannel.openTopic(TOPIC);
                writer.writeBoolean(!value);
                writer.publish();
                
            }).addSubscription(TOPIC); 
                    
            final CommandChannel startupChannel = runtime.newCommandChannel(); 
            runtime.addStartupListener(
                    ()->{
                        PayloadWriter writer = startupChannel.openTopic(TOPIC);
                        writer.writeBoolean(true);
                        writer.publish();
                    });        
        } 
    }
    
Of course, this is just a simple example; for more interesting examples that demonstrate the various features currently available in FogLight, you should take a look at [our examples repository](https://github.com/oci-pronghorn/EmbeddedToolkit-Examples).

## Notes for Contributors ##
This project contains specially compiled version of the libjffi-1.2.so file for use with the Edison (i386-linux) and Pi (arm-Linux). Additionally, Libjffi is broken on Raspberry Pi's, requiring a manual recompile which is talked about [here](https://github.com/jruby/jruby/issues/1561#issuecomment-67953147).

### Building Native Dependencies ###
Whenever the native C code is modified, it must be rebuilt on all of the target Pronghorn platforms and have its artifacts pushed back up to this repository. To re-build individual artifacts for each platform, perform the following steps:

1. Clone the FogLight repository down to your device (e.g., a Raspberry Pi or Intel Edison).
2. Within the root of the cloned repository, execute the appropriate make target (pi for Pi's, edison for Edisons). The make commands will give you instructions if you do it wrong.
3. A variety of '.so' files should be generated under resources/jni.
4. Commit the changes (which should only include `.so` files).
5. Push the changes.

Once the artifacts are properly built, you should be able to simply run `mvn clean packge` as normal and use the FogLight jar.

## Sponsors ##
Interested in sponsoring the development of the FogLight Embedded Toolkit? Contact [tippyn@objectcomputing.com](mailto:tippyn@objectcomputing.com).