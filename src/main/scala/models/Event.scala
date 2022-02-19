package models

import com.github.tototoshi.csv.CSVReader

import java.io.File
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import scala.io.Source
import scala.util.Try
import scala.util.Using
import scala.util.chaining._

case class Event(traceId: String, 
                 activity: String, 
                 start: ZonedDateTime, 
                 complete: ZonedDateTime, 
                 classification: Option[String])

object Event {

  private val dateTimeFormatter =
    DateTimeFormatter
      .ofPattern("yyyy/MM/dd HH:mm:ss.SSS")
      .withZone(UTC)

  def fromCSV(source: Source): Try[Vector[Event]] = {
    Using(CSVReader.open(source)) { reader =>
      reader
        .iteratorWithHeaders
        .map { row =>
          Event(
            row("Case ID")       .tap  { s => assert(s.nonEmpty, "Case ID is required") }, // poor man's validation
            row("Activity")      .tap  { s => assert(s.nonEmpty, "Activity is required") },
            row("Start")         .pipe { s => ZonedDateTime.parse(s, dateTimeFormatter) },
            row("Complete")      .pipe { s => ZonedDateTime.parse(s, dateTimeFormatter) },
            row("Classification").pipe { s => Some(s).filter(_.trim.nonEmpty) })
        }
        .toVector
    }
  }
}