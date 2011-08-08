package modules

import akka.config.Config
import akka.actor.Actor
import java.util.{HashMap, Scanner}

object CollectRemoteData {


  def main(args: Array[String]){
    println("Select Appropriate Option:\n 1 to Collect Remote Process Details\n 2 to Collect Remote File System Details\n 3 to Collect Network Details")
    val option = new Scanner(System.in).next()

    if(option == "1") Rps.run
    if(option == "2") Rfs.run
    if(option == "3") Rns.run

    println("\nDo you want Performance details (y\\n)")
    val aboutPerf = new Scanner(System.in).next()
    if(aboutPerf == "y")
      getPerfDetailsFromDirectory

    println("\nDo you want IP details (y\\n)")
    val aboutIp = new Scanner(System.in).next()
    if(aboutIp == "y")
      getIpDetailsFromDirectory

    System.exit(0)
  }

  def getIpDetailsFromDirectory = {
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryPort = Config.config.getInt("project-name.directoryPort").get

    val destination = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)
    val ipDetails = (destination !! "sendIpMap").get

    val ipMap = ipDetails.asInstanceOf[HashMap[String, String]]

    println("Supervisor\t" + ipMap.get("supervisor"))
    println("Worker\t\t" + ipMap.get("worker"))


  }

  def getPerfDetailsFromDirectory = {
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryPort = Config.config.getInt("project-name.directoryPort").get

    val destination = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)
    val timeDetails = (destination !! "sendTimeMap").get

    val perfMap = timeDetails.asInstanceOf[HashMap[String, String]]

    println("Supervisor Bootstrap Time\t" + perfMap.get("supervisorBootstrapTime"))
    println("Worker Bootstrap Time\t\t" + perfMap.get("workerBootstrapTime"))
    println("CAF Time\t\t\t" + perfMap.get("cafTime"))
    println("CAF Processing Time\t\t" + perfMap.get("cafProcessing"))
    println("End to End Time\t\t\t" + perfMap.get("endToEnd"))
  }
}