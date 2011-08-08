package modules

import akka.actor.Actor
import akka.config.Config
import java.util.HashMap

case class setIpAddressToMap(sender: String, ipAddress: String)
case class getIpAddressFromMap(sender: String)
case class perfStats(timeFor: String, time: String)

object Directory {
  val ipDetails = new HashMap[String, String]()
  val timeDetails = new HashMap[String, String]()

  def startActorRepository = {
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryPort = Config.config.getInt("project-name.directoryPort").get

    Actor.remote.start(directoryHost, directoryPort)
}

  def startDirectory = {
    println("Starting the directory")
    Actor.remote.register(DirectoryActor.serviceName, Actor.actorOf(new DirectoryActor))
  }

  def startConfigurations = {
    println("Starting the Configurations")
    Actor.remote.register(ConfigurationActor.serviceName, Actor.actorOf(new ConfigurationActor))
  }

  def setMap(sender: String, ipAddress: String) = {
    ipDetails.put(sender, ipAddress)

    println("MAP: " + ipDetails)
  }

  def getMap(sender: String) = {
    ipDetails.get(sender)
  }

  def addTimeDetails(timeFor: String, time: String) = {
    timeDetails.put(timeFor, time)

    println("Performance Stats:" + timeDetails)
  }

  def getIpMap = {
    ipDetails
  }

  def getPerfMap = {
    timeDetails
  }

  def main(args: Array[String]) {
    startActorRepository
    startDirectory
    startConfigurations
  }
}

class DirectoryActor extends Actor {
  val name = "Directory: "

  def receive = {
    case setIpAddressToMap(sender, ipAddress) =>
      Directory.setMap(sender, ipAddress)
    case getIpAddressFromMap(sender) =>
      val ipAddress = Directory.getMap(sender)
      self.reply_?(ipAddress)
    case perfStats(timeFor, time) =>
      Directory.addTimeDetails(timeFor, time)
    case "sendIpMap" =>
      val ipDetails = Directory.getIpMap
      self.reply_?(ipDetails)
    case "sendTimeMap" =>
      val timeDetails = Directory.getPerfMap
      self.reply_?(timeDetails)

  }
}
object DirectoryActor {
    val serviceName = "directory"
}

class ConfigurationActor extends Actor {
  val name = "Configurations: "

  def receive = {
    case message @ _ =>
      self.reply_?("true")
      println(message)
  }
}
object ConfigurationActor {
    val serviceName = "configuration"
}