#!/bin/bash
LOCAL_CLASSPATH=./build/libs/elastic-work-breakdown.jar
for jar in `ls ./deps/*.jar`; do
  LOCAL_CLASSPATH=${LOCAL_CLASSPATH}:${jar}
done

PROTO_JAVA_OPS=$PROTO_JAVA_OPS' ''-Dakka.config=./src/main/resources/proto-akka.conf'
java -cp $LOCAL_CLASSPATH $PROTO_JAVA_OPS actorproto.WorkDistributor