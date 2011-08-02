package modules

import akka.actor.Actor
import akka.config.Config

class DirectoryActor extends Actor {
  val name = "Directory: "

  def receive = {

    case message @ "Where is Worker?" =>
      val remoteHost = Config.config.getString("project-name.remoteHost").get
      val remotePort = Config.config.getInt("project-name.remotePort").get
      val replyString: String = remoteHost + "&" + remotePort.toString
      self.reply_?(replyString)

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
      self.reply_?("true")
      println(message)
  }
}
object ConfigurationActor {
    val serviceName = "configuration"
}