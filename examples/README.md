# PronghornIoT-Example-Projects
Examples for  IoT projects

+ Each of the folders are simple Maven projects which can be built by  mvn install
+ After build copy the file single jar (file is about 1.7Mb in size) from the target folder to your device  
+ Run with java -jar PronghornIoT-lightblink.jar

# usingClass

+ A single behavior class that implements the startup and pub/sub listeners
+ Simple to understand and all the business logic is in the behavior class

# usingLambdas

+ One startup lambda to kick off the system, One pub/sub lambda to loop the events
+ Using modern lambdas and does not require a second source file

# usingQueue

+ Using timer to keep a single channel full of on/off commands with interleaved delays
+ The blink is asynchronous so we can "pool up time" for other work as long as we pevent an empty command channel.

# usingTimer

+ Using timer to send on/off commands at the right rate
+ The blink is synchronous to our trigger except for the channel latency (nominally < 20ms).


If you would like to start your own projects...     
These projects were first built with     
mvn archetype:generate -DarchetypeGroupId=com.ociweb -DarchetypeArtifactId=FogLight-Archetype -DarchetypeVersion=1.0.0
