package simplehttp

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ExtendedActorSystem, ExtensionKey, Props}
import akka.io.IO.Extension
import akka.io.Tcp

/**
  * Simple HTTP protocol extension for akka.io
  *
  * @note Implementation based on spray-can module from Spray library:
  *       http://spray.io
  */
object SimpleHttp extends ExtensionKey[SimpleHttpExt] {

  // bind command
  case class Bind(userLevelListener: ActorRef, endpoint: InetSocketAddress)
  // bind companion object for InetSocketAddres construction from ip & port
  object Bind {
    def apply(listener: ActorRef, interface: String, port: Int = 80): Bind = {
      apply(listener, new InetSocketAddress(interface, port))
    }
  }

  type Command = Tcp.Command

  type Bound = Tcp.Bound
  val Bound = Tcp.Bound

  type Connected = Tcp.Connected
  val Connected = Tcp.Connected

  case class Register(handler: ActorRef) extends Command

}

class SimpleHttpExt(system: ExtendedActorSystem) extends Extension {

  override def manager: ActorRef = system.actorOf(
    props = Props[SimpleHttpManager],
    name = "IO-SIMPLEHTTP")
}
