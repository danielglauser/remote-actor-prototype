package modules

import akka.actor.Actor
import akka.config.Config
import collection.mutable.HashMap

object Worker {

  def startWorker = {
    println("Starting Worker")
    val remoteHost = Config.config.getString("project-name.remoteHost").get
    val remotePort = Config.config.getInt("project-name.remotePort").get

    Actor.remote.start(remoteHost, remotePort)
    Actor.remote.register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
  }

  def processResponse(response: String) = {
    val uriMap = getURI(response)

    for (i <- 0 until uriMap.size)
      println("uriMap: " + uriMap.get(i).get)

    val dataInList = getListFromUri(uriMap.get(0).get)

    connectToSupervisor(dataInList)
  }

  def getURI(response: String) = {
    var uriCount = 0
    var uriMap = new HashMap[Int, String]()
    val manifest = scala.xml.XML.loadString(response)

    for(uri <- manifest \\ "@uri"){
      val temp = uri.text.substring(7)
      if(temp.contains(".provider-data")){
        uriMap += uriCount -> temp
        uriCount = uriCount + 1
      }
    }
    println("uriCount: " + uriCount)
    uriMap
  }

  def getListFromUri(uri: String) = {
    DataParser.processData(uri)
  }

  def connectToSupervisor(dataInList: List[HashMap[String, String]]) = {
    val localHost = Config.config.getString("project-name.localHost").get
    val localPort = Config.config.getInt("project-name.localPort").get
    println("Connecting to Supervisor.." )
    val destination = Actor.remote.actorFor(SupervisorActor.serviceName, localHost, localPort)
    destination ! cafData(dataInList)
  }

  def startCollectInstanceActor = {
    Actor.remote.register(CollectInstanceActor.serviceName, Actor.actorOf(new CollectInstanceActor))
  }

  def sendToCollectInstanceActor(request: String) = {
    val remoteHost = Config.config.getString("project-name.remoteHost").get
    val remotePort = Config.config.getInt("project-name.remotePort").get
    println("Connecting to collectInstanceActor.." )
    val destination = Actor.remote.actorFor(CollectInstanceActor.serviceName, remoteHost, remotePort)
    destination ! request
  }

  def main(args: Array[String]) {
    startWorker
    startCollectInstanceActor
  }
}

class WorkerActor extends Actor {
  val name = "Worker: "

  def receive = {
//    case "collectSchema" =>
//      InitializeCaf.runSchema
    case message @ "collectInstance" =>
      Worker.sendToCollectInstanceActor(message.toString)
    case manifest(response) =>
      println("Im here")
      Worker.processResponse(response)

  }
}
object WorkerActor {
    val serviceName = "worker"
}
