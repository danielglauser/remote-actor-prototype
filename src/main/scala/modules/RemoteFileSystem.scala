package modules

import measurements.Profiling

object RFileSystem {

  def main(args: Array[String]) {

    Profiling.timed(Profiling.printTime("\n\n\nEnd-to-End time: ")){
      val fileSystemData = startRFileSystem("collectInstance")

      print("\ndir_name\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("dir_name").get + "\t")
      }

      print("\nusage_free_files\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("usage_free_files").get + "\t")
      }

      print("\nusage_avail\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("usage_avail").get + "\t")
      }

      print("\nusage_use_percent\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("usage_use_percent").get + "\t")
      }

      print("\nusage_total\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("usage_total").get + "\t")
      }

      print("\nsys_type_name\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("sys_type_name").get + "\t")
      }

      print("\ndev_name\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("dev_name").get + "\t")
      }

      print("\nusage_files\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("usage_files").get + "\t")
      }

      print("\nusage_free\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("usage_free").get + "\t")
      }

      print("\nflags\t\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("flags").get + "\t")
      }

      print("\ntype\t\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("type").get + "\t")
      }

      print("\noptions\t\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("options").get + "\t")
      }

      print("\nusage_used\t\t")
      for(i <- 0 until fileSystemData.length){
        print((fileSystemData.apply(i)).get("usage_used").get + "\t")
      }
    }
  }

  def startRFileSystem(request: String) = {
    if(request == "collectSchema") Supervisor.run(true, false)
    if(request == "collectInstance") Supervisor.run(false, true)

    while(!Supervisor.gotData){ Thread.sleep(100) }
    Supervisor.entireData.reverse
  }
}