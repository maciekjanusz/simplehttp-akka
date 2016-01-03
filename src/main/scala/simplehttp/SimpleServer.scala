package simplehttp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.io.IO
import akka.util.ByteString

object SimpleServer {

  implicit val system = ActorSystem()

  val defaultSocketAddress = new InetSocketAddress("localhost", 8080)

  def main(args: Array[String]) {
    try {
      start(new InetSocketAddress(args(0), args(1).toInt))
    } catch {
      case e: Exception =>
        println("Invalid arguments, using " + defaultSocketAddress.toString)
        start(defaultSocketAddress)
    }
  }

  def start(inetSocketAddress: InetSocketAddress): Unit = {
    val handler = system.actorOf(Props[UserLevelHandler], name = "user-level-handler")
    IO(SimpleHttp) ! SimpleHttp.Bind(listener = handler, inetSocketAddress)
  }
}

class UserLevelHandler extends Actor with ActorLogging {

  val responseHandlerCounter = Iterator from 0

  override def receive: Receive = {
    case msg: SimpleHttp.Connected =>
      val responseHandler = context.actorOf(Props[HttpRequestHandler])
      //        name = "request-handler-" + responseHandlerCounter.next())
      sender() ! SimpleHttp.Register(responseHandler)

    case msg: SimpleHttp.Bound =>
      log.info("Bound to " + msg.localAddress)
  }
}

class HttpRequestHandler extends Actor with ActorLogging {

  def httpHeader(contentLength: Int) = "HTTP/1.1 200 OK\nServer: SimpleServer/0.1\nContent-Length: " +
    contentLength + "\nConnection: close\nContent-Type: text/plain\n\n"

  val document = "Hello!\n"

  override def receive: Receive = {
    case SimpleHttp.Request(requestString) =>
      val response = httpHeader(ByteString.fromString(document).size) + document
      sender ! SimpleHttp.Response(response)
      log.info("\n" + requestString)
//      log.info("\n" + response)
      context.stop(self)
  }
}
