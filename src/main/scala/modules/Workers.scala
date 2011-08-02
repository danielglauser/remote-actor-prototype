package modules

import akka.actor.Actor
import akka.config.Config

object Worker {

  def startWorker = {
    println("Starting Worker")
    val remoteHost = Config.config.getString("project-name.remoteHost").get
    val remotePort = Config.config.getInt("project-name.remotePort").get

    Actor.remote.start(remoteHost, remotePort)
    Actor.remote.register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
  }

  def main(args: Array[String]) {
    startWorker
  }
}

class WorkerActor extends Actor {
  val name = "Worker: "

  def receive = {
    case "collectSchema" =>
      modules.startCaf.runSchema
    case "collectInstance" =>
      modules.startCaf.runInstance
  }
}
object WorkerActor {
    val serviceName = "worker"
}
