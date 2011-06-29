package actorproto

import akka.actor.Actor
import akka.amqp._
import akka.config.Config
import java.lang.String

case class entireMessage(workerNumber: Int, messageString: String, numberOfMsgs: Int, secretKey: String)
case class allExceptWorkerNumber(messageString: String, numberOfMsgs: Int, secretKey: String)

class WorkDistributorActor extends Actor {
  val name = "WorkDistributor: "

  def receive = {
    case message @ _ =>
      println("In WorkDistributorActor receive")
  }
}

object WorkDistributorActor {
    val serviceName = "workDistributor"
}

class WorkerActor extends Actor {
  val name = "WorkerService: "

  def receive = {
    case entireMessage(1,messageString,numberOfMsgs,secretKey) =>
      println("Worker1 called by workDistributor")
      Worker1.run(messageString, numberOfMsgs, secretKey)
    case entireMessage(2,messageString,numberOfMsgs,secretKey) =>
      println("Worker2 called by workDistributor")
      Worker2.run(messageString, numberOfMsgs, secretKey)
    case entireMessage(3,messageString,numberOfMsgs,secretKey)  =>
      println("Worker3 called by workDistributor")
      Worker3.run(messageString, numberOfMsgs, secretKey)
  }
}

object WorkerActor {
    val serviceName = "worker"
}

class DirectoryActor extends Actor {
  val name = "Directory: "
  val secretKey = Config.config.getString("project-name.secretKey").get
  val CONFMessage = "Where is Configurations? " + secretKey
  val AMQPMessage = "Where is AMQPWrapper? " + secretKey

  def receive = {

    case message @ CONFMessage =>
      println("Received: Where is Configurations?")
      println("Reply: 2552")
      self.reply_?("2552")
    case message @ AMQPMessage =>
      println("Received: Where is AMQPWrapper?")
      println("Reply: 2700")
      self.reply_?("2700")
    case message @ _ =>
      self.reply_?("0000")
  }
}

object DirectoryActor {
    val serviceName = "directory"
}

class ConfigurationActor extends Actor {
  val name = "Configurations: "

  def receive = {
    case message @ _ =>
      println(message)
  }
}

object ConfigurationActor {
    val serviceName = "configuration"
}

class AMQPActor extends Actor {
  val name = "AMQPWrapper: "

  def receive = {
    case message @  "worker -> AMQPWrapper" =>
      println(message)
    case allExceptWorkerNumber(messageString, numberOfMsgs, secretKey)=>
      AMQPWrapper.connectToAMQP(messageString, numberOfMsgs, secretKey)
  }
}

object AMQPActor{
    val serviceName = "amqp"
}

class ConsumerActor extends Actor {
  val name = "Consumer: "
  val secretKey = Config.config.getString("project-name.secretKey").get

  def receive = {
      case Delivery(data, secretKey, _, _, _, _) =>
        println("Received from WorkerService: " + new String(data))
    }
}

object ConsumerActor{
    val serviceName = "consumer"
}


