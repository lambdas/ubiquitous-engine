package mining

import models._

import java.io.File
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import scala.io.Source
import scala.util.Failure
import scala.util.Success
import scala.util.Using

/**
 * Extracts the direct follower matrix from a log.
 * Parses each line of the log file as an `Event` and builds the `Trace`s from the events.
 * Then counts all direct follower relations and prints them to the command line.
 */
object DirectFollowerExtraction {

  def main(args: Array[String]): Unit = {
    // TODO: Just imagine those parameters are coming from the command line, that would be so cool..
    val fromDate = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, UTC)
    val toDate = ZonedDateTime.of(2050, 1, 1, 0, 0, 0, 0, UTC)

    Event.fromCSV(Source.fromResource("IncidentExample.csv")) match {
      case Success(events) => println(render(directFollowerMatrix(events, fromDate, toDate)))
      case Failure(e)      => println(s"Error processing the file: ${e.getMessage}")
    }
  }

  def directFollowerMatrix(events: Seq[Event], 
                           fromDate: ZonedDateTime, 
                           toDate: ZonedDateTime): DirectFollowerMatrix = {
    Trace.fromEvents(events)
      .filter(_.isInRange(fromDate, toDate))
   // .flatMap(_.directFollowersSimple) // Use this instead of the next line to run the simple algorithm.
      .flatMap(_.directFollowers) 
      .groupMapReduce(identity)(_ => 1)(_ + _)
  }

  def render(dfm: DirectFollowerMatrix): String = {
    val xLabels = dfm.keys.map(_._1).toList.distinct
    val yLabels = dfm.keys.map(_._2).toList.distinct
    val labelWidth = (xLabels ++ yLabels).map(_.length).max
    val header = renderRow("" :: yLabels, labelWidth)
    val rows = xLabels.map { xLabel => renderRow(xLabel :: yLabels.map { yLabel => dfm.getOrElse((xLabel, yLabel), 0) }, labelWidth) }

    (header :: rows).mkString("\n")
  }

  private def renderRow(cells: Seq[_], width: Int): String = {
    cells
      .map(_.toString.padTo(width, ' '))
      .mkString("|")
  }
}
