package modules

import akka.actor.Actor
import akka.config.Config
import measurements.Profiling

object Rns {

  def main(args: Array[String]) {
    run
  }

  def run = {
    val start = System.nanoTime()
    val networkData = startRns("collectInstanceNS")
    println("config_address\tconfig_hwaddr\tconfig_type\tstat_speed\tstat_tx_bytes\tconfig_netmask\tconfig_address6\tconfig_broadcast\tconfig_destination\tconfig_name")
    for(i <- 0 until networkData.length){
      print((networkData.apply(i)).get("config_address").get + "\t")
    }

    for(i <- 0 until networkData.length){
      print((networkData.apply(i)).get("config_hwaddr").get + "\t")
    }

    for(i <- 0 until networkData.length){
      print((networkData.apply(i)).get("config_type").get + "\t")
    }

    for(i <- 0 until networkData.length){
      print((networkData.apply(i)).get("stat_speed").get + "\t")
    }

    for(i <- 0 until networkData.length){
      print((networkData.apply(i)).get("stat_tx_bytes").get + "\t")
    }

    for(i <- 0 until networkData.length){
      print((networkData.apply(i)).get("config_netmask").get + "\t")
    }

    for(i <- 0 until networkData.length){
      print((networkData.apply(i)).get("config_address6").get + "\t")
    }

    for(i <- 0 until networkData.length){
      print((networkData.apply(i)).get("config_broadcast").get + "\t")
    }

    for(i <- 0 until networkData.length){
      print((networkData.apply(i)).get("config_destination").get + "\t")
    }

    for(i <- 0 until networkData.length){
      print((networkData.apply(i)).get("config_name").get + "\t")
    }

    val end = System.nanoTime() - start
    val endToEnd = Profiling.formatTime(end)
    sendTimeToDirectory("endToEnd", endToEnd)
  }

  def sendTimeToDirectory(timeFor: String, time: String) = {
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryPort = Config.config.getInt("project-name.directoryPort").get

    val destination = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)
    destination ! perfStats(timeFor, time)
  }

  def startRns(request: String) = {
    Supervisor.run(request)
    while(!Supervisor.gotData){ Thread.sleep(100) }

    Supervisor.entireData.reverse
}
}