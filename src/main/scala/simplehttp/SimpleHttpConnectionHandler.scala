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
  // report established connection
  userLevelListener ! SimpleHttp.Connected(remoteAddress, localAddress)

  override def receive = awaitingRegister

  def awaitingRegister: Receive = {
    case SimpleHttp.Register(handler) =>
      // register for this tcp connection
      tcpManager ! Tcp.Register(handler = self)
      // await received data
      context.become(receiving(handler))
  }

  def receiving(userLevelHandler: ActorRef): Receive = {
    case Tcp.Received(data) =>
      // receive an http message
      val httpMsg = data.decodeString("UTF-8")
      // send the message to user-level handler and await response
      userLevelHandler ! Request(httpMsg)
      context.become(awaitingResponse(sender))
  }

  def awaitingResponse(tcpReceiver: ActorRef): Receive = {
    case SimpleHttp.Response(responseString) =>
      // send the response through tcp and back to receiving
      tcpReceiver ! Write(ByteString(responseString))
      context.unbecome()
  }
}