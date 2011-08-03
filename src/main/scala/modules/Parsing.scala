package modules

import collection.mutable.HashMap

object DataParser {
  def processData(uri: String) = {
    val procInstance = scala.xml.XML.loadFile(uri)

    var totalList = List[HashMap[String, String]]()
    var procMap = new HashMap[String, String]

    for(process <- procInstance \\ "Proc") {
      for(mem_minor <- process \\ "mem_minor_faults"){
        procMap += "mem_minor" -> mem_minor.text
      }
      for(cpu_total <- process \\ "cpu_total"){
        procMap += "cpu_total" -> cpu_total.text
      }
      for(state_name <- process \\ "state_name"){
        procMap += "state_name" -> state_name.text
      }
      for(credName_group <- process \\ "credName_group"){
        procMap += "credName_group" -> credName_group.text
      }
      for(pid <- process \\ "pid"){
        procMap += "pid" -> pid.text
      }
      for(mem_page_faults <- process \\ "mem_page_faults"){
        procMap += "mem_page_faults" -> mem_page_faults.text
      }
      for(mem_resident <- process \\ "mem_resident"){
        procMap += "mem_resident" -> mem_resident.text
      }
      for(cred_gid <- process \\ "cred_gid"){
        procMap += "cred_gid" -> cred_gid.text
      }
      for(cpu_percent <- process \\ "cpu_percent"){
        procMap += "cpu_percent" -> cpu_percent.text
      }
      for(mem_size <- process \\ "mem_size"){
        procMap += "mem_size" -> mem_size.text
      }
      for(state_ppid <- process \\ "state_ppid"){
        procMap += "state_ppid" -> state_ppid.text
      }
      for(state_tty <- process \\ "state_tty"){
        procMap += "state_tty" -> state_tty.text
      }
      for(cpu_user <- process \\ "cpu_user"){
        procMap += "cpu_user" -> cpu_user.text
      }
      for(cred_euid <- process \\ "cred_euid"){
        procMap += "cred_euid" -> cred_euid.text
      }
      for(state_threads <- process \\ "state_threads"){
        procMap += "state_threads" -> state_threads.text
      }
      for(state_priority <- process \\ "state_priority"){
        procMap += "state_priority" -> state_priority.text
      }
      for(mem_share <- process \\ "mem_share"){
        procMap += "mem_share" -> mem_share.text
      }
      for(credName_user <- process \\ "credName_user"){
        procMap += "credName_user" -> credName_user.text
      }
      for(mem_share <- process \\ "mem_share"){
        procMap += "mem_share" -> mem_share.text
      }
      for(state_state <- process \\ "state_state"){
        procMap += "state_state" -> state_state.text
      }
      for(state_nice <- process \\ "state_nice"){
        procMap += "state_nice" -> state_nice.text
      }
      for(state_processor <- process \\ "state_processor"){
        procMap += "state_processor" -> state_processor.text
      }
      for(cpu_sys <- process \\ "cpu_sys"){
        procMap += "cpu_sys" -> cpu_sys.text
      }
      for(time_sys <- process \\ "time_sys"){
        procMap += "time_sys" -> time_sys.text
      }
      for(mem_major_faults <- process \\ "mem_major_faults"){
        procMap += "mem_major_faults" -> mem_major_faults.text
      }
      for(cred_egid <- process \\ "cred_egid"){
        procMap += "cred_egid" -> cred_egid.text
      }
      for(time_user <- process \\ "time_user"){
        procMap += "time_user" -> time_user.text
      }
      for(time_total <- process \\ "time_total"){
        procMap += "time_total" -> time_total.text
      }

      totalList = totalList.::(procMap)
      procMap = new HashMap[String, String]
    }

    for(i <- 0 until totalList.length){
      println(">>" + (totalList.apply(i)).get("cpu_total").get)
    }

    totalList
  }
  
  def fileSystemData(uri: String) = {
    val fileSystemInstance = scala.xml.XML.loadFile(uri)
    var totalList = List[HashMap[String, String]]()
    var fileSystemMap = new HashMap[String, String]

    for(fileSystem <- fileSystemInstance \\ "Filesystem") {
      for(dir_name <- fileSystem \\ "dir_name"){
        fileSystemMap += "dir_name" -> dir_name.text
      }
      for(usage_free_files <- fileSystem \\ "usage_free_files"){
        fileSystemMap += "usage_free_files" -> usage_free_files.text
      }
      for(usage_avail <- fileSystem \\ "usage_avail"){
        fileSystemMap += "usage_avail" -> usage_avail.text
      }
      for(usage_use_percent <- fileSystem \\ "usage_use_percent"){
        fileSystemMap += "usage_use_percent" -> usage_use_percent.text
      }
      for(usage_total <- fileSystem \\ "usage_total"){
        fileSystemMap += "usage_total" -> usage_total.text
      }
      for(sys_type_name <- fileSystem \\ "sys_type_name"){
        fileSystemMap += "sys_type_name" -> sys_type_name.text
      }
      for(dev_name <- fileSystem \\ "dev_name"){
        fileSystemMap += "dev_name" -> dev_name.text
      }
      for(usage_files <- fileSystem \\ "usage_files"){
        fileSystemMap += "usage_files" -> usage_files.text
      }
      for(usage_free <- fileSystem \\ "usage_free"){
        fileSystemMap += "usage_free" -> usage_free.text
      }
      for(flags <- fileSystem \\ "flags"){
        fileSystemMap += "flags" -> flags.text
      }
      for(type1 <- fileSystem \\ "type"){
        fileSystemMap += "type" -> type1.text
      }
      for(options <- fileSystem \\ "options"){
        fileSystemMap += "options" -> options.text
      }
      for(usage_used <- fileSystem \\ "usage_used"){
        fileSystemMap += "usage_used" -> usage_used.text
      }
    
      totalList = totalList.::(fileSystemMap)
      fileSystemMap = new HashMap[String, String]
    }

    for(i <- 0 until totalList.length){
      println(">>" + (totalList.apply(i)).get("dir_name").get)
    }

    totalList
  }
}