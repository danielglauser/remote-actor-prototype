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
    Actor.remote.start(workDistributorHost, workDistributorPort)
    Actor.remote.register(WorkDistributorActor.serviceName, Actor.actorOf(new WorkDistributorActor))
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

      (message, numberOfMsgs, secretKey, workerCount)
  }

  def callWorkers(message: String, numberOfMsgs: Int, secretKey: String, workerCount: Int) = {
    if(workerCount == 1){
      connectToWorker1(message, numberOfMsgs, secretKey)
    }

    if(workerCount == 2){
      connectToWorker1(message, numberOfMsgs/2, secretKey)
      connectToWorker2(message, numberOfMsgs/2, secretKey)
    }

    if(workerCount == 3){
      connectToWorker1(message, numberOfMsgs/3, secretKey)
      connectToWorker2(message, numberOfMsgs/3, secretKey)
      connectToWorker3(message, numberOfMsgs/3, secretKey)
    }
  }

  def connectToWorker1(message: String, numberOfMsgs: Int, secretKey: String) = {
    val Worker1Port = Config.config.getInt("project-name.worker1Port").get
    val Worker1Dest = Actor.remote.actorFor(WorkerActor.serviceName, "localhost", Worker1Port)
    println("workDistributor -> worker1")
    Worker1Dest ! entireMessage(1, message, numberOfMsgs, secretKey)
  }

  def connectToWorker2(message: String, numberOfMsgs: Int, secretKey: String) = {
    val Worker2Port = Config.config.getInt("project-name.worker2Port").get
    val Worker2Dest = Actor.remote.actorFor(WorkerActor.serviceName, "localhost", Worker2Port)
    println("workDistributor -> worker2")
    Worker2Dest ! entireMessage(2, message, numberOfMsgs, secretKey)
  }

  def connectToWorker3(message: String, numberOfMsgs: Int, secretKey: String) = {
    val Worker3Port = Config.config.getInt("project-name.worker3Port").get
    val Worker3Dest = Actor.remote.actorFor(WorkerActor.serviceName, "localhost", Worker3Port)
    println("workDistributor -> worker3")
    Worker3Dest ! entireMessage(3, message, numberOfMsgs, secretKey)
  }

  def run = {
    startWorkerDistributor
    val (message, numberOfMsgs, secretKey, workerCount) = msgDetails
    callWorkers(message, numberOfMsgs, secretKey, workerCount)
  }

  def main(args: Array[String]) {
    run
  }
}

object Worker1 {

  def startWorker1 = {
    val worker1Port = Config.config.getInt("project-name.worker1Port").get
    val worker1Host = Config.config.getString("project-name.worker1Host").get
    println("Starting the worker1 on " + worker1Port)
    Actor.remote.start(worker1Host, worker1Port)
    Actor.remote.register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
  }

  def run(messageString: String, numberOfMsgs: Int, secretKey: String) = {
    WorkerService.run(messageString, numberOfMsgs, secretKey)
  }

  def main(args: Array[String]) {
    startWorker1
  }
}

object Worker2 {

  def startWorker2 = {
    val worker2Port = Config.config.getInt("project-name.worker2Port").get
    val worker2Host = Config.config.getString("project-name.worker2Host").get
    println("Starting the worker2 on " + worker2Port)
    Actor.remote.start(worker2Host, worker2Port)
    Actor.remote.register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
  }

   def run(messageString: String, numberOfMsgs: Int, secretKey: String) = {
    WorkerService.run(messageString, numberOfMsgs, secretKey)
  }

  def main(args: Array[String]) {
    startWorker2
  }
}

object Worker3 {

  def startWorker3 = {
    val worker3Port = Config.config.getInt("project-name.worker3Port").get
    val worker3Host = Config.config.getString("project-name.worker3Host").get
    println("Starting the worker3 on " + worker3Port)
    Actor.remote.start(worker3Host, worker3Port)
    Actor.remote.register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
  }

  def run(messageString: String, numberOfMsgs: Int, secretKey: String) = {
    WorkerService.run(messageString, numberOfMsgs, secretKey)
  }

  def main(args: Array[String]) {
    startWorker3
  }
}

object WorkerService {

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

  def connectToAMQP(AMQPPort: Int, messageString: String, numberOfMsgs: Int, secretKey: String) = {

    val AMQPDest = Actor.remote.actorFor(AMQPActor.serviceName, "localhost", AMQPPort)
    AMQPDest ! "worker -> AMQPWrapper"
    AMQPDest ! allExceptWorkerNumber(messageString, numberOfMsgs, secretKey)
  }

  def run(message: String, numberOfMsgs: Int, secretKey: String) = {
    val configurationPort = connectToDirectory1(secretKey)
    connectToConfigurations(configurationPort)
    val AMQPPort = connectToDirectory2(secretKey)
    connectToAMQP(AMQPPort, message, numberOfMsgs, secretKey)
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
      println("Enter Key to Authenticate")
      val secretKey = new Scanner(System.in).next()
//      val secretKey = "secret"

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

  def connectToAMQP(messageString: String, numberOfMsgs: Int, secretKey: String) = {

//  **** Pattern 0 ****
    val connection = AMQP.newConnection()
    val exchangeParameters = ExchangeParameters("hello")
    val producer = AMQP.newProducer(connection, ProducerParameters(Some(exchangeParameters), producerId = Some("my_producer")))

    for (i <- 1 to numberOfMsgs)
	    producer ! Message(messageString.getBytes, secretKey)
  }

//  def msgManipulations(message: Any) = {
//      val messageString = message.toString
//      val index1 =  messageString.indexOf("#")
//      val index2 = messageString.indexOf("`")
//      val msgPart = messageString.slice(0, index1)
//      val numberOfMsg = messageString.slice(index1+1, index2)
//      val key = messageString.slice(index2+1, messageString.length())
//
////    Or any other processing required
//      val messageToSend = msgPart.reverse
//
//      (messageToSend, numberOfMsg.toInt, key)
//  }

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