package simplehttp

import java.net.InetSocketAddress

import akka.actor._
import akka.io.{IO, Tcp}

class SimpleHttpListener(bindCommander: ActorRef,
                         bind: SimpleHttp.Bind) extends Actor with ActorLogging {
  import context.system

  val handlerCounter = Iterator from 0

  val tcpMgr = IO(Tcp)
  tcpMgr ! Tcp.Bind(self, bind.endpoint) // binding to TCP

  def receive = binding

  def binding: Receive = {
    case bound: Tcp.Bound =>
      bind.listener ! bound
      context.become(connecting)
  }

  def connecting: Receive = {
    case event: Tcp.Connected =>
      import event._
      val tcpHandler = sender
      newConnectionHandler(tcpHandler, remoteAddress, localAddress)
  }

  def newConnectionHandler(tcpManager: ActorRef, remoteAddress: InetSocketAddress,
                           localAddress: InetSocketAddress): ActorRef = {
    import bind._
    context.actorOf(Props(classOf[SimpleHttpConnectionHandler],
      tcpManager, listener, remoteAddress, localAddress))
//      name = "handler-" + handlerCounter.next())
  }
}
