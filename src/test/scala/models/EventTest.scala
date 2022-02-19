package models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

import java.io.File
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import scala.io.Source
import scala.util.Failure
import scala.util.Success

class EventTest extends AnyFlatSpec {
  
  "Event.fromCSV" should "return events" in {
    val testCsv = 
      """|Case ID,Activity,Start,Complete,Classification
         |trace_0,Incident logging,2016/01/04 12:09:44.000,2016/01/04 12:09:44.000,
         |trace_0,Incident classification,2016/01/04 12:10:44.000,2016/01/04 12:17:44.000,Citrix""".stripMargin

    Event
      .fromCSV(Source.fromString(testCsv))
      .get should contain theSameElementsAs List(
        Event("trace_0", "Incident logging",        ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), None),
        Event("trace_0", "Incident classification", ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), Some("Citrix")))
  }

  it should "fail if there is no Case ID value" in {
    val testCsv = 
      """|Case ID,Activity,Start,Complete,Classification
         |,Incident logging,2016/01/04 12:09:44.000,2016/01/04 12:09:44.000,""".stripMargin

    Event.fromCSV(Source.fromString(testCsv)) shouldBe a[Failure[_]]
  }

  it should "fail if there is no Activity value" in {
    val testCsv = 
      """|Case ID,Activity,Start,Complete,Classification
         |trace_0,,2016/01/04 12:09:44.000,2016/01/04 12:09:44.000,""".stripMargin

    Event.fromCSV(Source.fromString(testCsv)) shouldBe a[Failure[_]]
  }

  it should "fail if there is no Start value" in {
    val testCsv = 
      """|Case ID,Activity,Start,Complete,Classification
         |trace_0,Incident logging,,2016/01/04 12:09:44.000,""".stripMargin

    Event.fromCSV(Source.fromString(testCsv)) shouldBe a[Failure[_]]
  }

  it should "fail if there is no Complete value" in {
    val testCsv = 
      """|Case ID,Activity,Start,Complete,Classification
         |trace_0,Incident logging,2016/01/04 12:09:44.000,,""".stripMargin

    Event.fromCSV(Source.fromString(testCsv)) shouldBe a[Failure[_]]
  }

  it should "succeed if there is no Classification value" in {
    val testCsv = 
      """|Case ID,Activity,Start,Complete,Classification
         |trace_0,Incident logging,2016/01/04 12:09:44.000,2016/01/04 12:09:44.000,""".stripMargin

    Event.fromCSV(Source.fromString(testCsv)) shouldBe a[Success[_]]
  }
}
