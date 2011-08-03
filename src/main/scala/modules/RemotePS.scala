package modules

import measurements.Profiling

object Rps {

  def main(args: Array[String]) {

    Profiling.timed(Profiling.printTime("End-to-End time: ")){
      val processData = Supervisor.startRps("collectInstance")
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
    }
  }
}