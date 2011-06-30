package actorproto

import akka.actor.Actor._
import akka.actor.Actor
import akka.config._
import akka.amqp.AMQP._
import akka.amqp._
import java.lang.{String}
import java.util.Scanner

object WorkDistributor {

  def startWorkerDistributor = {
    val workDistributorPort = Config.config.getInt("project-name.workDistributorPort").get
    val workDistributorHost = Config.config.getString("project-name.workDistributorHost").get
    println("Starting the workerDistributor on " + workDistributorPort)
    Actor.remote.start(workDistributorHost, workDistributorPort).register(WorkDistributorActor.serviceName, Actor.actorOf(new WorkDistributorActor))
  }

  def msgDetails = {
      println("Enter Message to Send")
      val message = new Scanner(System.in).next()

      println("Enter Number of Messages")
      val numberOfMsgs = new Scanner(System.in).next().toInt

      println("Enter number of workers to be used")
      val workerCount = new Scanner(System.in).next().toInt

      println("Enter Key to Authenticate")
      val secretKey = new Scanner(System.in).next()

      if(secretKey != "secret"){
        println("INVALID KEY")
        sys.exit(0)
      }

      (message, numberOfMsgs, secretKey, workerCount)
  }

  def callWorkers(message: String, numberOfMsgs: Int, secretKey: String, workerCount: Int) = {
    val workerPort = Config.config.getInt("project-name.workerPort").get
    val workerHost = Config.config.getString("project-name.workerHost").get

    Actor.remote.start(workerHost, workerPort)

    for (count <- 1 to workerCount){
      spinUpWorkers(message, numberOfMsgs/workerCount, secretKey, count, workerPort, workerHost)
    }
  }

  def spinUpWorkers(message: String, numberOfMsgs: Int, secretKey: String, count: Int, workerPort: Int, workerHost: String) = {

    if(count == 1){
      println("\nSpinning up worker" + count.toString + " on " + workerPort)
      Actor.remote.register(Worker1Actor.serviceName, Actor.actorOf(new Worker1Actor))

      val dest = Actor.remote.actorFor(Worker1Actor.serviceName, workerHost, workerPort)
      println("workDistributor -> worker" + count)
      dest ! entireMessage(count, message, numberOfMsgs, secretKey)
    }

    if(count == 2){
      println("\nSpinning up worker" + count.toString + " on " + workerPort)
      Actor.remote.register(Worker2Actor.serviceName, Actor.actorOf(new Worker2Actor))

      val dest = Actor.remote.actorFor(Worker2Actor.serviceName, workerHost, workerPort)
      println("workDistributor -> worker" + count)
      dest ! entireMessage(count, message, numberOfMsgs, secretKey)
    }

    if(count == 3){
      println("\nSpinning up worker" + count.toString + " on " + workerPort)
      Actor.remote.register(Worker3Actor.serviceName, Actor.actorOf(new Worker3Actor))

      val dest = Actor.remote.actorFor(Worker3Actor.serviceName, workerHost, workerPort)
      println("workDistributor -> worker" + count)
      dest ! entireMessage(count, message, numberOfMsgs, secretKey)
    }

    if(count == 4){
      println("\nSpinning up worker" + count.toString + " on " + workerPort)
      Actor.remote.register(Worker4Actor.serviceName, Actor.actorOf(new Worker4Actor))

      val dest = Actor.remote.actorFor(Worker4Actor.serviceName, workerHost, workerPort)
      println("workDistributor -> worker" + count)
      dest ! entireMessage(count, message, numberOfMsgs, secretKey)
    }

    if(count == 5){
      println("\nSpinning up worker" + count.toString + " on " + workerPort)
      Actor.remote.register(Worker5Actor.serviceName, Actor.actorOf(new Worker5Actor))

      val dest = Actor.remote.actorFor(Worker5Actor.serviceName, workerHost, workerPort)
      println("workDistributor -> worker" + count)
      dest ! entireMessage(count, message, numberOfMsgs, secretKey)
    }
  }

  def run = {
    val (message, numberOfMsgs, secretKey, workerCount) = msgDetails
    callWorkers(message, numberOfMsgs, secretKey, workerCount)

  }

  def main(args: Array[String]) {
    run
  }
}

object WorkerService {

//  def startWorkerN(count: Int) = {
//    val workerNPort = (Config.config.getString("project-name.workerNPort").get + count.toString).toInt
//    val workerNHost = Config.config.getString("project-name.workerNHost").get
//    println("Starting the worker" + count.toString + " on " + workerNPort)
//    Actor.remote.start(workerNHost, workerNPort).register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
//  }


  def connectToDirectory1(secretKey: String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)

    val configurationPort = (directoryDest !! "Where is Configurations? "+secretKey).get

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

  def connectToAMQP(AMQPPort: Int, messageString: String, numberOfMsgs: Int, secretKey: String) = {

    val AMQPDest = Actor.remote.actorFor(AMQPActor.serviceName, "localhost", AMQPPort)
    AMQPDest ! "worker -> AMQPWrapper"
    AMQPDest ! allExceptWorkerNumber(messageString, numberOfMsgs, secretKey)
  }

  def run(messageString: String, numberOfMsgs: Int, secretKey: String) = {
    val configurationPort = connectToDirectory1(secretKey)
    connectToConfigurations(configurationPort)
    val AMQPPort = connectToDirectory2(secretKey)
    connectToAMQP(AMQPPort, messageString, numberOfMsgs, secretKey)
  }
}

object Directory {

  def run = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    println("Starting the directory on " + directoryPort)
    Actor.remote.start("localhost", directoryPort).register(DirectoryActor.serviceName, Actor.actorOf(new DirectoryActor))
  }

  def main(args: Array[String]) {
    run
  }
}

object Configurations {

  def run = {
    println("Starting the Configurations on 2552")
    Actor.remote.start("localhost", 2552).register(ConfigurationActor.serviceName, Actor.actorOf(new ConfigurationActor))
  }

  def main(args: Array[String]) {
    run
  }
}

object AMQPWrapper {

  def getKey = {
      println("Enter Key to Authenticate")
      val secretKey = new Scanner(System.in).next()
//      val secretKey = "secret"

      secretKey
  }

  def start = {
    println("Starting the Configurations on 2700")
    Actor.remote.start("localhost", 2700).register(AMQPActor.serviceName, Actor.actorOf(new AMQPActor))
  }

  def connectToDirectory1(secretKey: String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "localhost", directoryPort)

    val configurationPort = (directoryDest !! "Where is Configurations? "+secretKey).get

    configurationPort.toString.toInt
  }

  def connectToConfigurations(configurationPort: Int) = {
    val configurationDest = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", configurationPort)
    configurationDest ! "AMQPWrapper -> Configuration"
  }

  def connectToAMQP(messageString: String, numberOfMsgs: Int, secretKey: String) = {

//  **** Pattern 0 ****
    val connection = AMQP.newConnection()
    val exchangeParameters = ExchangeParameters("hello")
    val producer = AMQP.newProducer(connection, ProducerParameters(Some(exchangeParameters), producerId = Some("my_producer")))

    for (i <- 1 to numberOfMsgs)
	    producer ! Message(messageString.getBytes, secretKey)
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