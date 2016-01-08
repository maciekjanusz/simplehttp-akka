package simplehttp

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ExtendedActorSystem, ExtensionKey, Props}
import akka.io.IO.Extension
import akka.io.Tcp

/**
  * Simple HTTP extension for akka.io
  *
  * @note Implementation based on spray-can module from http://spray.io library
  */
object SimpleHttp extends ExtensionKey[SimpleHttpExt] {
  /*
    Command and event messages for the system
   */
  type Command = Tcp.Command
  type Event = Tcp.Event

  type Bound = Tcp.Bound
  val Bound = Tcp.Bound

  type Connected = Tcp.Connected
  val Connected = Tcp.Connected

  case class Bind(listener: ActorRef, endpoint: InetSocketAddress) extends Command

  object Bind {
    def apply(listener: ActorRef, interface: String, port: Int = 80): Bind = {
      apply(listener, new InetSocketAddress(interface, port))
    }
  }

  case class Register(handler: ActorRef) extends Command

  case class Response(response: String) extends Command

  case class Request(request: String) extends Event

}

class SimpleHttpExt(system: ExtendedActorSystem) extends Extension {

  // a method returning the manager actor for this extension
  override def manager: ActorRef = system.actorOf(
    props = Props[SimpleHttpManager],
    name = "io-simplehttp")
}
