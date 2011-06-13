package actorproto

import scala.collection.immutable._
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._
import java.util.Scanner

object Server {

  def run = {
//    println("Starting the actor container...")
    Actor.remote.start("localhost", 2552)

//    println("Registering the Data Collection Actor...")
    val dataCollectionActor = Actor.actorOf( new DataCollectionActor )
    Actor.remote.register(DataCollectionActor.serviceName, dataCollectionActor)

//    println("Registering the Remendiation Actor...")
    val remediationActor = Actor.actorOf( new RemediationActor )
    Actor.remote.register(RemediationActor.serviceName, remediationActor)

//    println("Registering the Configuration Actor for callbacks...")
    val configurationActor = Actor.actorOf( new ConfigurationActor )
    Actor.remote.register(ConfigurationActor.serviceName, configurationActor)

//    println("Registering the Client Actor for callbacks...")
    Actor.remote.register(Proxy.serviceName, Actor.actorOf( new Proxy(
      Option.apply[ActorRef](dataCollectionActor),
      Option.apply[ActorRef](remediationActor),
      Option.apply[ActorRef](configurationActor))))


//    println("All actors registered and waiting for messages.")
      println("Server Listening..")
  }

  def main(args: Array[String]) = { /*println("Starting messaging server..."); */ run }
}

object Client {

  def userInputs = {
    println("Enter number of Messages to Send")
    val multiplier = (new Scanner(System.in)).next()

    println("Enter Port Number")
    val portNo = (new Scanner(System.in)).next()

    println("\nEnter first message for collectionMessages")
    val string1 = (new Scanner(System.in)).next()

    println("\nEnter second message for collectionMessages")
    val string2 = (new Scanner(System.in)).next()

    println("\nEnter first message for remediationMessages")
    val string3 = (new Scanner(System.in)).next()

    println("\nEnter second message for remediationMessages")
    val string4 = (new Scanner(System.in)).next()

    List.apply[String](multiplier, portNo, string1, string2, string3, string4)
  }


  def tabulateManyMessages(numMessages: Int, messageBodies: List[String]) = {
    List.tabulate(numMessages)(index => messageBodies.apply(index % messageBodies.length))
  }

  @throws(classOf[akka.dispatch.FutureTimeoutException])
  def sendManyMessages = {

    var messages = List("")
    var flag1= "noConnect"
    var flag2= "noConnect"

    val inputs = userInputs

    val client = Actor.remote.actorFor(Proxy.serviceName, "localhost", inputs.apply(1).toInt)

    var perfInfo = new HashMap[String, Long]
    perfInfo += "startTime" -> System.nanoTime


    val collectionMessages = tabulateManyMessages(inputs.apply(0).toInt, List.apply[String](inputs.apply(2), inputs.apply(3)))
    // Create a List of messages by repeating the original List
    timed(printTime("Sent " + messages.length + " " + collectionMessages + " messages in ")) {
    val result = (client !! collectionMessages.head).get
    if (result == "ACK")
      flag1 = "Connect"
    }

    if (flag1 == "noConnect")
      println("Couldnt Establish Connection")
    else {
      println("SUCCESS")
      collectionMessages.drop(0) foreach { message =>
        client ! message
      }

      perfInfo = perfInfo + ("endTimeCollections" -> System.nanoTime)
      perfInfo = perfInfo + ("numCollections" -> messages.length)
    }

    val remediationMessages = tabulateManyMessages(inputs.apply(0).toInt, List.apply[String](inputs.apply(4), inputs.apply(5)))
    // Create a List of messages by repeating the original List
    timed(printTime("Sent " + messages.length + " " + remediationMessages + " messages in ")) {
    val result = (client !! remediationMessages.head).get
    if (result == "ACK")
      flag2 = "Connect"
    }

    if (flag2 == "noConnect")
      println("Couldnt Establish Connection")
    else {
      println("SUCCESS")
      remediationMessages.drop(0) foreach { message =>
        client ! message
      }

      perfInfo = perfInfo + ("endTimeComplexRemediations" -> System.nanoTime)
      perfInfo = perfInfo + ("numComplexRemediations" -> messages.length)
     }

      if(flag1 == "Connect" && flag2 == "Connect")
        printPerformanceStats(perfInfo)
  }

  def printPerformanceStats(perfInfo : Map[String, Long]) = {
    println("------------------------------------------------------")
    println("                    Summary")
    println("------------------------------------------------------")
    println("")

    val collectionTime : Long = perfInfo.get("endTimeCollections").get - perfInfo.get("startTime").get
    val collectionRate : Double = perfInfo.get("numCollections").get * 1000000000 / collectionTime.toDouble

    val remediationTime : Long = perfInfo.get("endTimeComplexRemediations").get - perfInfo.get("endTimeCollections").get
    val remediationRate : Double = perfInfo.get("numComplexRemediations").get * 1000000000 / remediationTime.toDouble

    val numMessages : Long = perfInfo.get("numCollections").get + perfInfo.get("numComplexRemediations").get
    val totalTime   : Long = collectionTime + remediationTime
    val totalRate   : Double = numMessages * 1000000000 / totalTime

    printf("Sent %d messages at a rate of %.2f per second\n", numMessages, totalRate)

    println("Total time: " + formatTime(totalTime))

    println("\n")
    println("------------------------------------------------------")
  }

  def run = {
    sendManyMessages
  }

  def main(args: Array[String]) =  {
    run
  }
}