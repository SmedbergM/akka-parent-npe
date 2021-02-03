package com.github.smedbergm

import scala.concurrent.Promise
import scala.concurrent.duration._

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object MWE extends App {
  val actorSystem = ActorSystem("mwe-system")
  import actorSystem.dispatcher
  val jobIds = List(1,2,1)

  val level0 = actorSystem.actorOf(Props(new Level0), "userspace-root-actor")
  jobIds.zipWithIndex.foreach { case (jobId, idx) =>
    actorSystem.scheduler.scheduleOnce(idx * 100.milliseconds) {
      level0 ! Level0Message(jobId)
    }
  }
}

case class Level0Message(n: Int)

class Level0 extends Actor with ActorLogging {
  override val receive: Receive = {
    case msg@Level0Message(n) =>
      log.info("Received message {}", msg)
      val child = context.actorOf(Props(new Level1), s"level-1-${n}")
      child ! Level1Start
  }
}

case object Level1Start
sealed trait Level1End
case class Level1Success(text: String) extends Level1End
case class Level1Failure(text: String) extends Level1End

class Level1 extends Actor with ActorLogging {
  override val receive: Receive = {
    case Level1Start =>
      log.info("Received {}", Level1Start)
      val child = context.actorOf(Props(new Level2), "leaf-actor")
      child ! Level2Message(context.self.path.hashCode())
    case msg: Level1End =>
      log.info("Received {}", msg)
  }
}

case class Level2Message(n: Int)

class Level2 extends Actor {
  import context.dispatcher

  override val receive: Receive = {
    case Level2Message(n) =>
      val p: Promise[String] = Promise()
      context.system.scheduler.scheduleOnce(1.second) {
        val succeeded = p.trySuccess(s"success-${n}")
        if (succeeded) {
          context.parent ! Level1Success("success")
        } else {
          context.parent ! Level1Failure("failure")
        }
      }
  }
}