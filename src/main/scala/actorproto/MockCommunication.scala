package actorproto

import akka.actor.Actor

object Local {
//  def run = {
//    Actor.remote.start("10.25.38.50", 3000).register(LocalActor.serviceName, Actor.actorOf(new LocalActor))
//    println("Started LocalActor on port 3000" )
//  }
    def main(args: Array[String]) {
//    run
  }
}

//class LocalActor extends Actor {
//  println("LocalActor Called")
//  val name = "Local: "
//
//  def receive = {
//    case message @ _ =>
//      println("Received" + message +"!!")
//  }
//}
//
//object LocalActor {
//    val serviceName = "local"
//}

object Remote {
//  def run = {
//    println("Starting Remote.." )
//    val destination = Actor.remote.actorFor(LocalActor.serviceName, "10.25.38.50", 3000)
//    val msg = "Hello"
//    destination ! msg
//  }

    def main(args: Array[String]) {
//    run
  }
}
