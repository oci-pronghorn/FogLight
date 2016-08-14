# PronghornIoT-Archetype
Maven Archetype for starting new IoT projects

To load the archetype on your local machine

+ Checkout this project and run 
+ mvn install

To create a new IoT project

+ mvn archetype:generate -DarchetypeGroupId=com.ociweb.iot.archetype -DarchetypeArtifactId=PronghornIoT-Archetype -DarchetypeVersion=0.0.1

When the project is built with [mvn install] an artifact with the selected name will be built in the target folder.  This jar is executable and contains all its needed dependencies. It just needs to be copied over to the IntelEdison or RaspberryPi.