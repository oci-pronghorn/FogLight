# Pronghorn Embedded Toolkit #
The Pronghorn Embedded Toolkit (PET) is a lightweight runtime that enables makers of all ages and skill levels to create highly performant apps for embedded devices like Raspberry Pis.

## What It Is ##
PET is a Java 8 functional API for embedded systems that's built on top of [Pronghorn](https://github.com/oci-pronghorn/Pronghorn), an actor-oriented framework for creating high-performance messaging systems composed of `Stages` and `Pipes` between stages.

PET is...
- Fast - Built on top of Pronghorn, PET is a garbage-free, lock-free and low latency way to talk directly to hardware.
- Simple - Taking advantage of the latest Java 8 APIs, PET has a clean and fluent set of APIs that make it easy to learn and apply with minimal training.
- Secure - By taking advantage of the compile-time Pronghorn graph validation system, all PET applications can be compiled and compressed to a point where injecting malicious code into the final production JAR would prove difficult, if not impossible.

## How It Works ##
Every PET application starts with an `IoTSetup` implementation which initializes the Pronghorn `DeviceRuntime` by defining various hardware connections and behaviors for handling state changes in those connections.  

A very simple example of a PET application is below (omitting boilerplate import statements and so on); this app makes an LED connected to a GrovePi board blink every 500 milliseconds:

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
    
Of course, this is just a simple example; for more interesting examples that demonstrate the various features currently available in PET, you should take a look at [our examples repository](https://github.com/oci-pronghorn/PronghornIoT-Examples).

## Notes for Contributors ##
This project contains specially compiled version of the libjffi-1.2.so file for use with the Edison (i386-linux) and Pi (arm-Linux). Additionally, Libjffi is broken on Raspberry Pi's, requiring a manual recompile which is talked about [here](https://github.com/jruby/jruby/issues/1561#issuecomment-67953147).

## Sponsors ##
Interested in sponsoring the development of the Pronghorn Embedded Toolkit? Contact [tippyn@ociweb.com](mailto:tippyn@ociweb.com).