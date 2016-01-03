package simplehttp

import akka.actor.{Actor, ActorLogging, Props}

class SimpleHttpManager extends Actor with ActorLogging {

  val listenerCounter = Iterator from 0

  override def receive: Receive = {
    case bind: SimpleHttp.Bind =>
      log.info("Binding to SimpleHttp on " + bind.endpoint.toString)

      val commander = sender
      context.watch {
        context.actorOf(Props(classOf[SimpleHttpListener], commander, bind),
          name = "listener-" + listenerCounter.next())
      }
  }
}

