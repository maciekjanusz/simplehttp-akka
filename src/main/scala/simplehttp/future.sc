import akka.dispatch.ExecutionContexts

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

implicit val context = ExecutionContexts.global()

val x = Future(1 + 2)

//val timeout = 1 seconds
//val result = Await.result(x, timeout)

x.foreach(println(_))