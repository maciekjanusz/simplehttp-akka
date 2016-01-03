package simplehttp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp
import akka.io.Tcp.Write
import akka.util.ByteString
import simplehttp.SimpleHttp.Request

class SimpleHttpConnectionHandler(tcpManager: ActorRef,
                                  userLevelListener: ActorRef,
                                  remoteAddress: InetSocketAddress,
                                  localAddress: InetSocketAddress)
  extends Actor with ActorLogging {

  userLevelListener ! SimpleHttp.Connected(remoteAddress, localAddress)

  override def receive = awaitingRegister

  def awaitingRegister: Receive = {
    case SimpleHttp.Register(handler) =>
      tcpManager ! Tcp.Register(handler = self)
      context.become(receiving(handler))
  }

  def receiving(userLevelHandler: ActorRef): Receive = {
    case Tcp.Received(data) =>
      val httpMsg = data.decodeString("UTF-8")
      userLevelHandler ! Request(httpMsg)
      context.become(awaitingResponse(sender))
  }

  def awaitingResponse(tcpReceiver: ActorRef): Receive = {
    case SimpleHttp.Response(responseString) =>
      tcpReceiver ! Write(ByteString(responseString))
      context.stop(self)
  }
}