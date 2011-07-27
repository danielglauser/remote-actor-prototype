package actorproto

import akka.actor.Actor._
import akka.actor.Actor
import akka.config._
import akka.amqp.AMQP._
import akka.amqp._
import java.util.Scanner
import java.io.{InputStreamReader, BufferedReader}
import scala.swing._
import event._
import java.lang.{Boolean, String, Runtime}
import collection.mutable.HashMap

object userInterface extends SimpleSwingApplication{
    def top = new MainFrame {
      title = "Work Distributor"

      val workLabel = new Label() {
        text = "Select Work:"
      }

      val collectOS = new CheckBox("collect OS Details")
      val collectProcess = new CheckBox("collect Process Details")
      val collectRegistry = new CheckBox("collect Registry Details")
      val collectFileSystem = new CheckBox("collect FileSystem Details")

      val factorLabel = new Label() {
        text = "Enter factor:"
      }

      val factorField = new TextField() {
        columns = 10
      }

      val strategyLabel = new Label(){
        text = "Select Worker Strategy"
      }

      val strategy = new ButtonGroup
      val serial = new RadioButton("Serial")
      val parallel = new RadioButton("Parallel")
      val other = new RadioButton("Other")
      val resources = new RadioButton("According to Resources")
      val strategyRadios = List(serial, parallel, other, resources)
      strategy.buttons ++= strategyRadios
      strategy.select(serial)

      val otherStrategyLabel = new Label(){
        text = "Enter Number of Workers"
      }

      val otherStrategyField = new TextField() {
        columns = 10
      }

      val secretKeyLabel = new Label(){
        text = "Enter key to Authenticate"
      }

      val secretKeyField = new TextField() {
        columns = 10
      }

      val submitButton = new Button() {
      text = "Submit"
      }

      contents =  new BoxPanel(Orientation.Vertical){
        contents += workLabel
        contents += collectOS
        contents += collectProcess
        contents += collectRegistry
        contents += collectFileSystem

        contents += factorLabel
        contents += factorField

        contents += strategyLabel
        contents ++= strategyRadios

        contents += otherStrategyLabel
        contents += otherStrategyField

        contents += secretKeyLabel
        contents += secretKeyField

        contents += submitButton
      }

      listenTo(submitButton)
      var nClicks = 0
      reactions += {
        case ButtonClicked(b) =>

          var strategySelected = ""
          strategy.selected.get match {
            case `serial` =>  strategySelected = "SERIAL"
            case `parallel` =>  strategySelected = "PARALLEL"
            case `other` => strategySelected = "OTHER"
          }

          nClicks += 1
          submitButton.text = "Sent: " + nClicks

          WorkDistributor.run(collectOS.selected, collectProcess.selected, collectRegistry.selected,
            collectFileSystem.selected, factorField.text.toInt, strategySelected, otherStrategyField.text.toInt, secretKeyField.text)

          collectOS.selected = false
      }
    }
  }

object WorkDistributor {

  def startWorkerDistributor = {
    val workDistributorPort = Config.config.getInt("project-name.workDistributorPort").get
    val workDistributorHost = Config.config.getString("project-name.workDistributorHost").get
    println("Starting the workerDistributor on " + workDistributorPort)
    Actor.remote.start(workDistributorHost, workDistributorPort).register(WorkDistributorActor.serviceName, Actor.actorOf(new WorkDistributorActor))
  }

  def msgDetails(collectOS: Boolean, collectProcess: Boolean, collectRegistry: Boolean, collectFileSystem: Boolean,
           factor: Int, strategy: String, otherStrategy: Int, key: String) = {

    var rawMessageList = List[String]()

    if(collectOS)  rawMessageList = rawMessageList ::: List("collectOS")
    if(collectProcess) rawMessageList = rawMessageList ::: List("collectProcess")
    if(collectRegistry)  rawMessageList = rawMessageList ::: List("collectRegistry")
    if(collectFileSystem)  rawMessageList = rawMessageList ::: List("collectFileSystem")

    var finalMessageList = List[String]()
    for (i <- 0 until rawMessageList.length by factor){
      val tempData = rawMessageList.apply(i)
      finalMessageList = finalMessageList ::: List(tempData)
    }

    var workerCount = 0
    if(strategy == "SERIAL") workerCount = 1
    if(strategy == "PARALLEL") workerCount = 5
    if(strategy == "OTHER") workerCount = otherStrategy

    val secretKey = key

    (finalMessageList, secretKey, workerCount)

  }

  def callWorkers(finalMessageList: List[String], secretKey: String, workerCount: Int) = {
    val workerPort = Config.config.getInt("project-name.workerPort").get
    val workerHost = Config.config.getString("project-name.workerHost").get

    Actor.remote.start(workerHost, workerPort)

    var message = ""
    for(count <- 0 until workerCount){
      for(i <- count until (finalMessageList.length)+count by workerCount){
        message = finalMessageList.apply(i)
        spinUpWorkers(message, secretKey, count+1, workerPort, workerHost)
      }
    }

  }

  def spinUpWorkers(message: String, secretKey: String, count: Int, workerPort: Int, workerHost: String) = {

    if(count == 1){
      println("\nSpinning up worker" + count.toString + " on " + workerPort)
      Actor.remote.register(Worker1Actor.serviceName, Actor.actorOf(new Worker1Actor))

      val dest = Actor.remote.actorFor(Worker1Actor.serviceName, workerHost, workerPort)
      println("workDistributor -> worker" + count)
      dest ! entireMessage(count, message, secretKey)
    }

    if(count == 2){
      println("\nSpinning up worker" + count.toString + " on " + workerPort)
      Actor.remote.register(Worker2Actor.serviceName, Actor.actorOf(new Worker2Actor))

      val dest = Actor.remote.actorFor(Worker2Actor.serviceName, workerHost, workerPort)
      println("workDistributor -> worker" + count)
      dest ! entireMessage(count, message, secretKey)
    }

    if(count == 3){
      println("\nSpinning up worker" + count.toString + " on " + workerPort)
      Actor.remote.register(Worker3Actor.serviceName, Actor.actorOf(new Worker3Actor))

      val dest = Actor.remote.actorFor(Worker3Actor.serviceName, workerHost, workerPort)
      println("workDistributor -> worker" + count)
      dest ! entireMessage(count, message, secretKey)
    }

    if(count == 4){
      println("\nSpinning up worker" + count.toString + " on " + workerPort)
      Actor.remote.register(Worker4Actor.serviceName, Actor.actorOf(new Worker4Actor))

      val dest = Actor.remote.actorFor(Worker4Actor.serviceName, workerHost, workerPort)
      println("workDistributor -> worker" + count)
      dest ! entireMessage(count, message, secretKey)
    }

    if(count == 5){
      println("\nSpinning up worker" + count.toString + " on " + workerPort)
      Actor.remote.register(Worker5Actor.serviceName, Actor.actorOf(new Worker5Actor))

      val dest = Actor.remote.actorFor(Worker5Actor.serviceName, workerHost, workerPort)
      println("workDistributor -> worker" + count)
      dest ! entireMessage(count, message, secretKey)
    }
  }

  def run(collectOS: Boolean, collectProcess: Boolean, collectRegistry: Boolean, collectFileSystem: Boolean,
           factor: Int, strategy: String, otherStrategy: Int, key: String) = {

      val (finalMessageList, secretKey, workerCount) = msgDetails(collectOS, collectProcess, collectRegistry,
        collectFileSystem, factor, strategy, otherStrategy, key)
      callWorkers(finalMessageList, secretKey, workerCount)
  }

//  def main(args: Array[String]) {
//    run
//  }
}

object WorkerService {

  def connectToDirectory1(secretKey: String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)

    val configurationPort = (directoryDest !! "Where is Configurations? "+secretKey).get

    configurationPort.toString.toInt
  }

  def connectToConfigurations(configurationPort: Int) = {
    val configurationDest = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", configurationPort)
    configurationDest ! "worker -> Configuration"
  }

  def connectToDirectory2(secretKey:String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "localhost", directoryPort)

    val AMQPPort = (directoryDest !! "Where is AMQPWrapper? "+secretKey).get

    AMQPPort.toString.toInt
  }

  def connectToAMQP(AMQPPort: Int, messageString: String, secretKey: String) = {

    val AMQPDest = Actor.remote.actorFor(AMQPActor.serviceName, "localhost", AMQPPort)
    AMQPDest ! "worker -> AMQPWrapper"
    AMQPDest ! allExceptWorkerNumber(messageString, secretKey)
  }

  def run(messageString: String, secretKey: String) = {
    val configurationPort = connectToDirectory1(secretKey)
    connectToConfigurations(configurationPort)
    val AMQPPort = connectToDirectory2(secretKey)
    connectToAMQP(AMQPPort, messageString, secretKey)
  }
}

object Directory {

  def run = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    println("Starting the directory on " + directoryPort)
    Actor.remote.start("localhost", directoryPort).register(DirectoryActor.serviceName, Actor.actorOf(new DirectoryActor))
  }

  def main(args: Array[String]) {
    run
  }
}

object Configurations {

  def run = {
    println("Starting the Configurations on 2552")
    Actor.remote.start("localhost", 2552).register(ConfigurationActor.serviceName, Actor.actorOf(new ConfigurationActor))
  }

  def main(args: Array[String]) {
    run
  }
}

object AMQPWrapper {

  def getKey = {
      println("Enter Key to Authenticate")
      val secretKey = new Scanner(System.in).next()
//      val secretKey = "secret"

      secretKey
  }

  def start = {
    println("Starting the Configurations on 2700")
    Actor.remote.start("localhost", 2700).register(AMQPActor.serviceName, Actor.actorOf(new AMQPActor))
  }

  def connectToDirectory1(secretKey: String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "localhost", directoryPort)

    val configurationPort = (directoryDest !! "Where is Configurations? "+secretKey).get

    configurationPort.toString.toInt
  }

  def connectToConfigurations(configurationPort: Int) = {
    val configurationDest = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", configurationPort)
    configurationDest ! "AMQPWrapper -> Configuration"
  }

  def connectToAMQP(messageString: String, secretKey: String) = {

//  **** Pattern 0 ****
    val connection = AMQP.newConnection()
    val exchangeParameters = ExchangeParameters("request")
    val producer = AMQP.newProducer(connection, ProducerParameters(Some(exchangeParameters), producerId = Some("my_producer")))

    producer ! Message(messageString.getBytes, secretKey)
  }

  def runReplyConsumer = {
    val secretKey = Config.config.getString("project-name.secretKey").get

//  **** Pattern 0 ****
    val connection = AMQP.newConnection()
    val exchangeParameters = ExchangeParameters("response")

    val myConsumer = AMQP.newConsumer(connection, ConsumerParameters(secretKey, actorOf(new ConsumerActor), None, Some(exchangeParameters)))
  }

  def run {
    start
    val secretKey = getKey
    val configurationPort = connectToDirectory1(secretKey)
    connectToConfigurations(configurationPort)
  }

  def main(args: Array[String]) {
    run
  }
}

object Consumer {

  def ConnectToRemote = {
    Actor.remote.start("10.25.38.50", 3000).register(CafCommunicationActor.serviceName, Actor.actorOf(new CafCommunicationActor))
    println("Started LocalActor on port 3000" )
  }

  def runConsumer = {

    val secretKey = Config.config.getString("project-name.secretKey").get

//  **** Pattern 0 ****
    val connection = AMQP.newConnection()
    val exchangeParameters = ExchangeParameters("request")

    val myConsumer = AMQP.newConsumer(connection, ConsumerParameters(secretKey, actorOf(new ConsumerActor), None, Some(exchangeParameters)))
  }

  def runReply(dataInList: List[HashMap[String, String]]) = {

//  **** Pattern 0 ****
    val connection = AMQP.newConnection()
    val exchangeParameters = ExchangeParameters("response")
    val producer = AMQP.newProducer(connection, ProducerParameters(Some(exchangeParameters), producerId = Some("my_producer")))

    val messageString = "CHANGE INPUT"
    val secretKey = "secret"

    producer ! Message(messageString.getBytes, secretKey)
  }

  def main(args: Array[String]) {
    ConnectToRemote
    runConsumer
  }
}

object abcd {

  def runXML = {

    val ps = Runtime.getRuntime.exec("pc.bat")
    var br = new BufferedReader(new InputStreamReader(ps.getInputStream))
    var s = ""
    for (i <- 1 to 5) s = br.readLine()
    val index = s.indexOf(",")

    println("Cores: " +  Runtime.getRuntime.availableProcessors())
    println("Free memory available to the JVM: " + Runtime.getRuntime.freeMemory() + " bytes")
    println("Memory currently in use by the JVM: " + Runtime.getRuntime.totalMemory() + " bytes")
    println("CPU Usage: " + s.substring(index+2, s.length()-1) + " percent")

  }




  def main(args: Array[String]) {
    runXML
  }
}