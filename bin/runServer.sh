#!/bin/bash
LOCAL_CLASSPATH=./build/libs/remote-actor-prototype.jar
for jar in `ls ./deps/*.jar`; do
  LOCAL_CLASSPATH=${LOCAL_CLASSPATH}:${jar}
done

scala -cp $LOCAL_CLASSPATH actorproto.Server