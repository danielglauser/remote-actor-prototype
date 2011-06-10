import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import actorproto. {ConfigurationActor, ConfigSetting, MessageSetting, PayloadBinding, ActorRepositorySetting}

class ConfigurationSpec extends FlatSpec with ShouldMatchers {
//
//  "A Configuration Actor" should "return MessageSetting(\"numCollectionMessages\", 400) after the setting has been set" in {
//    val PORT = 8675
//    Actor.remote.start("localhost", PORT)
//    val configurationActor = Actor.remote.actorFor("actorproto.ConfigurationActor", "localhost", PORT)
//    var message = MessageSetting("numCollectionMessages", 400)
//    configurationActor ! message
//    (configurationActor !! MessageSetting("numCollectionMessages")).get should equal (message)
//  }

//  "A Configuration Actor" should "return MessageSetting(\"numRemediationMessages\", 400) after the setting has been set" in {
//    val PORT = 8675
//    Actor.remote.start("localhost", PORT)
//    val configurationActor = Actor.remote.actorFor("actorproto.ConfigurationActor", "localhost", PORT)
//    var message = MessageSetting("numRemediationMessages", 400)
//    configurationActor ! message
//    (configurationActor !! MessageSetting("numRemediationMessages")).get should equal (message)
//  }
//
  // "A Configuration Actor" should "return help test after a help message is sent" in {
  //   val PORT = 8675
  //   Actor.remote.start("localhost", PORT)
  //   val configurationActor = Actor.remote.actorFor("actorproto.ConfigurationActor", "localhost", PORT)
  //   (configurationActor !! "help").get should startWith ("The configuration-actor responds to the following messages:")
  // }
  
//  it should "throw NoSuchElementException if an empty stack is popped" in {        
//    evaluating { emptyStack.pop() } should produce [NoSuchElementException]
//  }
}