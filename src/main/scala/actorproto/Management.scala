package actorproto

import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef, Actors, UntypedActor}
import akka.config._
import akka.amqp.AMQP._
import akka.amqp._
import java.lang.{String}

object Worker {

  def msgDetails = {
//      println("Enter Message to Send")
//      val message = new Scanner(System.in).next()
      val message = "OG EW EREH"

//      println("Enter Number of Messages")
//      val numberOfMsgs = new Scanner(System.in).next()
      val numberOfMsgs = "3"

//      println("Enter Key to Authenticate")
//      val secretKey = new Scanner(System.in).next()
      val secretKey = "secret"

      (message, numberOfMsgs, secretKey)

  }

  def startWorker = {

    val workerPort = Config.config.getInt("project-name.workerPort").get
    val workerHost = Config.config.getString("project-name.workerHost").get
    println("Starting the worker on " + workerPort)
    Actor.remote.start(workerHost, workerPort)
    Actor.remote.register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
  }

  def connectToDirectory1(secretKey: String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)

    val configurationPort = (directoryDest !! "Where is Configurations? "+secretKey).get

    if(configurationPort.toString == "0000"){
      println("INVALID KEY")
      sys.exit(0)
    }

    configurationPort.toString.toInt
  }

  def connectToConfigurations(configurationPort: Int) = {
    val configurationDest = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", configurationPort)
    configurationDest ! "worker -> Configuration"
  }

  def connectToDirectory2(secretKey:String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "localhost", directoryPort)

    val AMQPPort = (directoryDest !! "Where is AMQPWrapper? "+secretKey).get

    AMQPPort.toString.toInt
  }

  def connectToAMQP(AMQPPort: Int, message: String, numberOfMsgs: String, secretKey: String) = {

    val AMQPDest = Actor.remote.actorFor(AMQPActor.serviceName, "localhost", AMQPPort)
    AMQPDest ! "worker -> AMQPWrapper"
    AMQPDest ! (message + "#" + numberOfMsgs + "`" + secretKey)
  }

  def run = {
    startWorker
    val (message, numberOfMsgs, secretKey) = msgDetails
    val configurationPort = connectToDirectory1(secretKey)
    connectToConfigurations(configurationPort)
    val AMQPPort = connectToDirectory2(secretKey)
    connectToAMQP(AMQPPort, message, numberOfMsgs, secretKey)
  }

  def main(args: Array[String]) {
    run
  }
}

object Directory {

  def run = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    println("Starting the directory on " + directoryPort)
    Actor.remote.start("localhost", directoryPort)
    Actor.remote.register(DirectoryActor.serviceName, Actor.actorOf(new DirectoryActor))
  }

  def main(args: Array[String]) {
    run
  }
}

object Configurations {

  def run = {
    println("Starting the Configurations on 2552")
    Actor.remote.start("localhost", 2552)
    Actor.remote.register(ConfigurationActor.serviceName, Actor.actorOf(new ConfigurationActor))
  }

  def main(args: Array[String]) {
    run
  }
}

object AMQPWrapper {

  def getKey = {
//      println("Enter Key to Authenticate")
//      val secretKey = new Scanner(System.in).next()
      val secretKey = "secret"

      secretKey
  }

  def start = {
    println("Starting the Configurations on 2700")
    Actor.remote.start("localhost", 2700)
    Actor.remote.register(AMQPActor.serviceName, Actor.actorOf(new AMQPActor))
  }

  def connectToDirectory1(secretKey: String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "localhost", directoryPort)

    val configurationPort = (directoryDest !! "Where is Configurations? "+secretKey).get

    if(configurationPort.toString == "0000"){
      println("INVALID KEY")
      sys.exit(0)
    }
    configurationPort.toString.toInt
  }

  def connectToConfigurations(configurationPort: Int) = {
    val configurationDest = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", configurationPort)
    configurationDest ! "AMQPWrapper -> Configuration"
  }

  def connectToAMQP(message: Any) = {
    val (messageToSend, numberOfMsg, secretKey) = msgManipulations(message)

//  **** Pattern 0 ****
    val connection = AMQP.newConnection()
    val exchangeParameters = ExchangeParameters("hello")
    val producer = AMQP.newProducer(connection, ProducerParameters(Some(exchangeParameters), producerId = Some("my_producer")))

    for (i <- 1 to numberOfMsg)
	    producer ! Message(messageToSend.getBytes, secretKey)
  }

  def msgManipulations(message: Any) = {
      val messageString = message.toString
      val index1 =  messageString.indexOf("#")
      val index2 = messageString.indexOf("`")
      val msgPart = messageString.slice(0, index1)
      val numberOfMsg = messageString.slice(index1+1, index2)
      val key = messageString.slice(index2+1, messageString.length())

//    Or any other processing required
      val messageToSend = msgPart.reverse

      (messageToSend, numberOfMsg.toInt, key)
  }

  def run {
    start
    val secretKey = getKey
    val configurationPort = connectToDirectory1(secretKey)
    connectToConfigurations(configurationPort)
  }

  def main(args: Array[String]) {
    run
  }
}

object Consumer {

  def run = {

    val secretKey = Config.config.getString("project-name.secretKey").get

//  **** Pattern 0 ****
    val connection = AMQP.newConnection()
    val exchangeParameters = ExchangeParameters("hello")

    val myConsumer = AMQP.newConsumer(connection, ConsumerParameters(secretKey, actorOf(new ConsumerActor), None, Some(exchangeParameters)))
  }

  def main(args: Array[String]) {
    run
  }
}