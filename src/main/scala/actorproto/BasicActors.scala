package actorproto

import akka.actor. {Actor, ActorRef}
import akka.amqp._
import akka.config.Config


class WorkerActor extends Actor {
  val name = "Worker: "

  def receive = {
    case message @ _ =>
      println("In workerActor receive")
  }
}

object WorkerActor {
    val serviceName = "worker"
}

class DirectoryActor extends Actor {
  val name = "Directory: "

  def receive = {
    case message @ "Where is Configurations? secret" =>
      println("Received: Where is Configurations?")
      println("Reply: 2552")
      self.reply_?("2552")
    case message @ "Where is AMQPWrapper? secret" =>
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
    case message @ _ =>
      AMQPWrapper.connectToAMQP(message)
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
        println("Received from Worker: " + new String(data))
    }
}

object ConsumerActor{
    val serviceName = "consumer"
}


