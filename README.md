   Sample code for playing with [Akka](http://www.akka.io/) actors and exploring patterns across multiple nodes.
Built with [Gradle](http://gradle.org/).

Build:  
  `gradle jar`     # compiles the Scala code and jars up the classes  
  `gradle deps`    # puts all the project dependencies in the deps directory for the shell scripts  

Run:  
  `./bin/runServer.sh`  
  `./bin/runClient.sh`  
  
If you are on Windows I suggest you install [Cygwin](http://www.cygwin.com/).

Structure
----------

Currently all of the logic is in RemoteActorServer.scala.  
`Server`: Registers the `RemediationActor` and `DataCollectionActor` and waits for messages.  
`Client`: Uses the `ClientActor` to send remote messages to the `DataCollectionActor` and `RemediationActor`.  