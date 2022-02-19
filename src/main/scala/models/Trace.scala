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

  def directFollowers: Iterator[(String, String)] = {
    events
      .groupMap(_.start)(_.activity)
      .toSeq
      .sortBy(_._1)
      .sliding(2)
      .collect { case Seq(e1, e2) => cartesianProduct(e1._2, e2._2) }
      .flatten
  }
}

object Trace {

  def fromEvents(events: Seq[Event]): Seq[Trace] = {
    events
      .groupBy(_.traceId)
      .map { case (traceId, events) => 
        Trace(
          traceId, 
          events.view.map(_.start).min, 
          events.view.map(_.complete).max, 
          events) 
      }
      .toSeq
  }

  private def cartesianProduct[A, B](xs: Seq[A], ys: Seq[B]): Seq[(A, B)] = {
    for {
      x <- xs
      y <- ys
    } yield (x, y)
  }
}
