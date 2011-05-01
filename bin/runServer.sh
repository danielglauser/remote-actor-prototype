#!/bin/bash
LOCAL_CLASSPATH=./build/libs/remote-actor-prototype.jar
for jar in `ls ./deps/*.jar`; do
  LOCAL_CLASSPATH=${LOCAL_CLASSPATH}:${jar}
done

#java -cp $LOCAL_CLASSPATH -Dakka.config="/Users/dglauser/Projects/scala/akka/config/akka.conf" actorproto.Server
java -cp $LOCAL_CLASSPATH -Dakka.config="./src/main/resources/proto-akka.conf" actorproto.Server