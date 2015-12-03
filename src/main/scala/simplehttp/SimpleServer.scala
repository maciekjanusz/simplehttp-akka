package simplehttp

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.io.IO

/**
  * Created by maciek-private on 28.11.2015.
  */
object SimpleServer extends App {

  implicit val system = ActorSystem()
  val handler = system.actorOf(Props[Handler], name = "user-level-handler")
  val interface = "192.168.1.184" //"localhost"
  val port = 8888
  println("Binding to SimpleHttp on " + interface + ":" + port)
  IO(SimpleHttp) ! SimpleHttp.Bind(listener = handler, interface = interface, port = port)
}

class Handler extends Actor with ActorLogging {

  val responseHandlerCounter = Iterator from 0

  override def receive: Receive = {
    case msg: SimpleHttp.Connected =>
      log.info("Received SimpleHttp.Connected. Sending back SimpleHttp.Register")

      val responseHandler = context.actorOf(Props[HttpResponseHandler],
        name = "response-handler-" + responseHandlerCounter.next())
      sender() ! SimpleHttp.Register(responseHandler)
  }


}

class HttpResponseHandler extends Actor with ActorLogging {

  override def receive = registered
  def registered: Receive = {
    case msg: String =>
      log.info(msg)
  }

}
