package simplehttp

import java.net.InetSocketAddress

import akka.actor._
import akka.io.{IO, Tcp}

/**
  * Created by maciek-private on 28.11.2015.
  */
class SimpleHttpListener(bindCommander: ActorRef, // bind commander to user-level listener
                         bind: SimpleHttp.Bind) extends Actor with ActorLogging {

  // bind to tcp automatically upon creation
  import context.system
  println("Binding to TCP on " + bind.endpoint)
  val tcpMgr = IO(Tcp)
  tcpMgr ! Tcp.Bind(self, bind.endpoint)

  // behaviors
  def receive = binding

  def connecting: Receive = {
    case event: Tcp.Connected =>
      import event._
      val tcpManager = sender
      // create handler for this TCP connection -> the handler should wait for registration after creation
      newConnectionHandler(tcpManager, remoteAddress, localAddress)
  }

  def binding: Receive = {
    case bound: Tcp.Bound =>
      log.info("TCP Bound")
      context.become(connecting)
  }

  def newConnectionHandler(tcpManager: ActorRef, remoteAddress: InetSocketAddress,
                           localAddress: InetSocketAddress): ActorRef = {
    // this Props mechanism is for safer creation of actor with parameters. See AkkaScala doc. section 3.1. recommended practices
    import bind._
    context.actorOf(SimpleHttpConnectionHandler.props(tcpManager, userLevelListener, remoteAddress, localAddress),
      name = "handler-" + SimpleHttpConnectionHandler.handlerCounter.next())
  }
}
