package simplehttp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp
import akka.util.ByteString

/**
  * Created by maciek-private on 28.11.2015.
  */
class SimpleHttpConnectionHandler(tcpManager: ActorRef, // sam TcpConnectionListener nic nie wysyła do <self>
                                  userLevelListener: ActorRef, // do zwracania np. SimpleHttp.Connected i faktycznych wiadomosci http
                                  remoteAddress: InetSocketAddress,
                                  localAddress: InetSocketAddress) extends Actor with ActorLogging {

  userLevelListener ! SimpleHttp.Connected(remoteAddress, localAddress)

  def awaitingRegister: Receive = {
    case SimpleHttp.Register(handler) =>
      tcpManager ! Tcp.Register(handler = self)
      context.become(receiving(handler))
  }

  def receiving(userLevelHandler: ActorRef): Receive = {
    case Tcp.Received(data) =>
      val httpMsg = data.decodeString("UTF-8")
      userLevelHandler ! httpMsg

      sender() ! Tcp.Write(SimpleHttpConnectionHandler.byteStringResponse)
  }

  override def receive = awaitingRegister
}

object SimpleHttpConnectionHandler {

  val httpResponse = "HTTP/1.1 200 OK\nServer: SimpleServer/0.1\nContent-Length: 150\nConnection: close\nContent-Type: text/html\n\n\n<!doctype html>\n<html>\n<head>\n  <meta charset=\"utf-8\">\n  <title>Holy crap it works</title>\n</head>\n<body>\n  <h1>It fucking works!</h1>\n</body>\n</html>"
  println(httpResponse)
  val byteStringResponse = ByteString(httpResponse)

  val handlerCounter = Iterator from 0

  def props(tcpManager: ActorRef, listener: ActorRef, remoteAddress: InetSocketAddress,
            localAddress: InetSocketAddress) =
    Props(classOf[SimpleHttpConnectionHandler], tcpManager, listener, remoteAddress, localAddress)
}