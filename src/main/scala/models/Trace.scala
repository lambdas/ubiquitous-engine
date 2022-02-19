package models

import java.time.ZonedDateTime

case class Trace(traceId: String, 
                 start: ZonedDateTime,
                 complete: ZonedDateTime,
                 events: Seq[Event]) {

  def isInRange(from: ZonedDateTime, to: ZonedDateTime): Boolean = {
    !from.isAfter(start) && !to.isBefore(complete)
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
}
