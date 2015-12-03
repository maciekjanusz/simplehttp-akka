package simplehttp

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

/**
  * Created by maciek-private on 28.11.2015.
  */
class SimpleHttpManager extends Actor with ActorLogging {

  val listenerCounter = Iterator from 0
  var listeners = Seq.empty[ActorRef] // simple http listeners

  override def receive: Receive = {
    case bind: SimpleHttp.Bind =>
      log.info("SimpleHttp: Bind")
      val commander = sender // jesli wysłano nie z contextu aktora, commander prowadzi do deadLetterMailbox
      listeners :+= context.watch {
        context.actorOf(Props(new SimpleHttpListener(commander, bind)), name = "listener-" + listenerCounter.next())
        // the SimpleHttpListener will bind to TCP automatically, so no need for sending any message to it
      }
  }
}
