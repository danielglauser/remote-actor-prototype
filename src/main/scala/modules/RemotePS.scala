package modules

import measurements.Profiling
import akka.config.Config
import akka.actor.Actor

object Rps {

  def main(args: Array[String]) {
    run
  }

  def run = {
    val start = System.nanoTime()
    val processData = startRps("collectInstancePS")
    println("pid\tcredName_group\tcpu_total\tstate_name\tcpu_percent\tstate_ppid\tstate_tty")
    for(i <- 0 until processData.length){
      print((processData.apply(i)).get("pid").get + "\t")
      print((processData.apply(i)).get("credName_group").get + "\t\t")
      print((processData.apply(i)).get("cpu_total").get + "\t\t")
      print((processData.apply(i)).get("cpu_percent").get + "\t\t")
      print((processData.apply(i)).get("state_ppid").get + "\t\t")
      print((processData.apply(i)).get("state_tty").get + "\t\t")
      print((processData.apply(i)).get("state_name").get + "\n")
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

  def startRps(request: String) = {
    Supervisor.run(request)
    while(!Supervisor.gotData){ Thread.sleep(100) }

    Supervisor.entireData.reverse
}
}