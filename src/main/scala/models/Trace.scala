package models

import models.Trace._

import java.time.ZonedDateTime

case class Trace(traceId: String, 
                 start: ZonedDateTime,
                 complete: ZonedDateTime,
                 events: Seq[Event]) {

  def isInRange(from: ZonedDateTime, to: ZonedDateTime): Boolean = {
    !from.isAfter(start) && !to.isBefore(complete)
  }

  def directFollowers: Seq[(Activity, Activity)] = {
    events
      .groupMap(_.start)(_.activity)
      .toSeq
      .sortBy(_._1)
      .sliding(2)
      .collect { case Seq(e1, e2) => cartesianProduct(e1._2, e2._2) }
      .flatten
      .toSeq
  }

  def directFollowersSimple: Seq[(Activity, Activity)] = {
    events
      .sortBy(_.start)
      .sliding(2)
      .collect { case Seq(e1, e2) => (e1.activity, e2.activity) }
      .toSeq
  }
}

object Trace {

  def fromEvents(events: Seq[Event]): IndexedSeq[Trace] = {
    events
      .groupBy(_.traceId)
      .map { case (traceId, events) => 
        Trace(
          traceId, 
          events.view.map(_.start).min, 
          events.view.map(_.complete).max, 
          events) 
      }
      .toVector
  }

  private def cartesianProduct[A, B](xs: Seq[A], ys: Seq[B]): Seq[(A, B)] = {
    for {
      x <- xs
      y <- ys
    } yield (x, y)
  }
}
