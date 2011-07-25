#!/bin/bash
LOCAL_CLASSPATH=./build/libs/elastic-work-breakdown.jar

export CAF_JAVA_APPCONFIG=/home/caf/caf/config/cafRunner.properties
export CAF_ROOTDIR=/home/caf/caf/
export MALLOC_CHECK_=0
export CAF_APPCONFIG=/home/caf/caf/config/comm-appconfig
export LD_LIBRARY_PATH=/home/caf/caf/lib 

for jar in `ls ./deps/*.jar`; do
  LOCAL_CLASSPATH=${LOCAL_CLASSPATH}:${jar}
done

PROTO_JAVA_OPS=$PROTO_JAVA_OPS' ''-Dakka.config=./src/main/resources/proto-akka.conf'
java -cp $LOCAL_CLASSPATH $PROTO_JAVA_OPS modules.startCaf