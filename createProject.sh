#!/usr/bin/bash
appName="$1"
if [ "$#" -eq 0 ]; then
    echo "You must pass at least one argument indicating the name of the project to create (ex: sh createProject.sh MyNewProject)"
else
    mvn archetype:generate -DarchetypeGroupId=com.ociweb -DarchetypeArtifactId=FogLight-Archetype -DarchetypeVersion=0.1.0 -DgroupId=com.dexterindustries.grovepi.project -DartifactId=$1 -Dversion=1.0-SNAPSHOT -Dpackage=com.dexterindustries.grovepi.project.$1 -DinteractiveMode=false
fi
