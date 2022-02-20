package models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

class TraceTest extends AnyFlatSpec {

  "fromEvents" should "return traces" in {
    val events = List(
      Event("trace_0", "Incident logging",        ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), None),
      Event("trace_0", "Incident classification", ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), Some("Citrix")),
      Event("trace_1", "Incident logging",        ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), None))
    
    Trace.fromEvents(events) should contain theSameElementsAs List(
      Trace(
        "trace_0",
        ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC),
        ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC),
        List(
          Event("trace_0", "Incident logging",        ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), None),
          Event("trace_0", "Incident classification", ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), Some("Citrix")))),
      Trace(
        "trace_1",
        ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC),
        ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC),
        List(
          Event("trace_1", "Incident logging",        ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), None))))
  }

  "isInRange" should "return true if the trace is in provided range" in {
    Trace("trace_0", ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), Nil)
      .isInRange(ZonedDateTime.of(2015, 1, 4, 12, 9, 44, 0, UTC), ZonedDateTime.of(2017, 1, 4, 12, 17, 44, 0, UTC)) shouldBe true
  }

  it should "return true if the trace starts when provided range starts" in {
    Trace("trace_0", ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), Nil)
      .isInRange(ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), ZonedDateTime.of(2017, 1, 4, 12, 17, 44, 0, UTC)) shouldBe true
  }

  it should "return true if the trace ends when provided range ends" in {
    Trace("trace_0", ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), Nil)
      .isInRange(ZonedDateTime.of(2015, 1, 4, 12, 9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC)) shouldBe true
  }

  it should "return false if the trace starts before provided range starts" in {
    Trace("trace_0", ZonedDateTime.of(2015, 1, 4, 12,  9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), Nil)
      .isInRange(ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC)) shouldBe false
  }

  it should "return false if the trace ends after provided range ends" in {
    Trace("trace_0", ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC), ZonedDateTime.of(2017, 1, 4, 12, 17, 44, 0, UTC), Nil)
      .isInRange(ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC)) shouldBe false
  }

  "directFollowers" should "return direct followers if events follow each other" in {
    Trace(
      "trace_0",
      ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC),
      ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC),
      List(
        Event(
          "trace_0", 
          "Incident classification", 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Resolution and recovery", 
          ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Incident logging",        
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          None)))
      .directFollowers.toSeq should contain theSameElementsAs List(
        ("Incident logging", "Incident classification"),
        ("Incident classification", "Resolution and recovery"))
  }

  it should "return direct followers if events form a DAG" in {
    Trace(
      "trace_0",
      ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC),
      ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC),
      List(
        Event(
          "trace_0", 
          "Incident logging",        
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          None),
        Event(
          "trace_0", 
          "Incident classification", 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Initial diagnosis", 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Resolution and recovery", 
          ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), 
          Some("Citrix"))))
      .directFollowers.toSeq should contain theSameElementsAs List(
        ("Incident logging", "Incident classification"),
        ("Incident logging", "Initial diagnosis"),
        ("Incident classification", "Resolution and recovery"),
        ("Initial diagnosis", "Resolution and recovery"))
  }

  it should "return nothing if events start at the same time" in {
    Trace(
      "trace_0",
      ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC),
      ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC),
      List(
        Event(
          "trace_0", 
          "Incident logging",        
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          None),
        Event(
          "trace_0", 
          "Incident classification", 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Initial diagnosis", 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Resolution and recovery", 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          Some("Citrix"))))
      .directFollowers.toSeq should contain theSameElementsAs Nil
  }

  "directFollowersSimple" should "return direct followers if events follow each other" in {
    Trace(
      "trace_0",
      ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC),
      ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC),
      List(
        Event(
          "trace_0", 
          "Incident classification", 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Resolution and recovery", 
          ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Incident logging",        
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          None)))
      .directFollowersSimple.toSeq should contain theSameElementsAs List(
        ("Incident logging", "Incident classification"),
        ("Incident classification", "Resolution and recovery"))
  }

  it should "return direct followers if events form a DAG" in {
    Trace(
      "trace_0",
      ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC),
      ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC),
      List(
        Event(
          "trace_0", 
          "Incident logging",        
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          None),
        Event(
          "trace_0", 
          "Incident classification", 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Initial diagnosis", 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Resolution and recovery", 
          ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), 
          Some("Citrix"))))
      .directFollowersSimple.toSeq should contain theSameElementsAs List(
        ("Incident logging", "Incident classification"),
        ("Incident classification", "Initial diagnosis"),
        ("Initial diagnosis", "Resolution and recovery"))
  }

  it should "return direct followers if events start at the same time" in {
    Trace(
      "trace_0",
      ZonedDateTime.of(2016, 1, 4, 12,  9, 44, 0, UTC),
      ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC),
      List(
        Event(
          "trace_0", 
          "Incident logging",        
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          None),
        Event(
          "trace_0", 
          "Incident classification", 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Initial diagnosis", 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          Some("Citrix")),
        Event(
          "trace_0", 
          "Resolution and recovery", 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
          Some("Citrix"))))
      .directFollowersSimple.toSeq should contain theSameElementsAs List(
        ("Incident logging", "Initial diagnosis"),
        ("Incident classification", "Incident logging"),
        ("Initial diagnosis", "Resolution and recovery"))
  }
}
