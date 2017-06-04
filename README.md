# FogLighter (A Maven Archetype for FogLight projects)
Maven Archetype for starting new FogLight IoT projects

To load the archetype on your local machine

+ git clone https://github.com/oci-pronghorn/FogLighter.git
+ mvn install

To create a new FogLight IoT project run the following. You will be prompted for the group (name space) and project name.

+ mvn archetype:generate -DarchetypeGroupId=com.ociweb -DarchetypeArtifactId=FogLight-Archetype -DarchetypeVersion=0.1.0-SNAPSHOT

When the project is built with [mvn install] an artifact with the selected name will be built in the target folder.  This jar is executable and contains all its needed dependencies. It just needs to be copied over to the IntelEdison, RaspberryPi or other hardware.
