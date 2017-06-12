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

# startingYourOwnProject

 In the command line or terminal of your local machine, please enter:
```
 git clone https://github.com/oci-pronghorn/FogLighter.git
 cd FogLighter
 mvn install
 ```
 
Now, please ```cd``` into a directory that you would like your own IoT project to be created in, and enter:
```
mvn archetype:generate -DarchetypeGroupId=com.ociweb -DarchetypeArtifactId=FogLight-Archetype -DarchetypeVersion=0.1.0-SNAPSHOT
```
The terminal now asks you for: 
```groupID```: type in  *com.ociweb* then press Enter

```ArtifactID```: type in name of your project then press Enter

```version: 1.0-SNAPSHOT ```: Ignore, Press Enter

```package: com.ociweb ```: Ignore, Press Enter

```Y:```  :  Type *Y*, press Enter


This will create a folder named after your project, which includes all the project files. Let’s call our project *ProjectXYZ*.  
If you’re working from Terminal, open up the file  “ProjectXYZ”/src/main/java/com/ociweb/IoTApp.java . You can start implementing the project code from here. 
If you’re using an IDE, open up the created Maven project - *ProjectXYZ* and start working from IoTApp.java

Once you’re done with the implementation, open your project folder in terminal and type 
```
mvn install
```
.. to build the project. This will create a .jar file named ProjectXYZ.jar in the **/target** folder (note that there are other .jar files  in **/target**, but we don’t have to worry about those). Transfer this .jar file to your device and use the command 
```
java -jar ProjectXYZ.jar 
```
.. to execute it.
 
