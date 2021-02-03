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
case class Level0End(n: Int)
case class Level0InProgress(n: Int)

class Level0 extends Actor with ActorLogging {
  override val receive: Receive = {
    case msg@Level0Message(n) =>
      log.info("Received message {}", msg)
      val child = context.actorOf(Props(new Level1(n)), s"level-1-${n}")
      child ! Level1Start
    case Level0End(n) =>
      log.info("Task {} completed.", n)
    case Level0InProgress(n) =>
      log.info("Task {} is still in progress", n)
  }
}

case object Level1Start
case class Level1Success(text: String)

class Level1(n: Int) extends Actor with ActorLogging {
  import context.dispatcher

  override val receive: Receive = {
    case Level1Start =>
      log.info("Received {}", Level1Start)
      val child = context.actorOf(Props(new Level2), "leaf-actor")
      child ! Level2Message(context.self.path.hashCode())
      context.system.scheduler.scheduleOnce(1.second) {
        context.parent ! Level0InProgress(n)
      }
    case msg: Level1Success =>
      log.info("Received {}", msg)
      context.parent ! Level0End(n)
  }
}

case class Level2Message(n: Int)

class Level2 extends Actor {
  import context.dispatcher

  override val receive: Receive = {
    case Level2Message(n) =>
      context.system.scheduler.scheduleOnce(1.second) {
        context.parent ! Level1Success(s"success-${n}")
      }
  }
}