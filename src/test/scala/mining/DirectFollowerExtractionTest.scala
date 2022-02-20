package mining

import models.Event
import models.Trace
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

class DirectFollowerExtractionTest extends AnyFlatSpec {
  
  "directFollowerMatrix" should "return a matrix" in {
    val events = List(
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
        None),
      Event(
        "trace_1", 
        "Incident classification", 
        ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
        ZonedDateTime.of(2016, 1, 4, 12, 10, 44, 0, UTC), 
        Some("Citrix")),
      Event(
        "trace_1", 
        "Resolution and recovery", 
        ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), 
        ZonedDateTime.of(2016, 1, 4, 12, 17, 44, 0, UTC), 
        Some("Citrix")),
      Event(
        "trace_1", 
        "Incident logging",        
        ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
        ZonedDateTime.of(2016, 1, 4, 12, 9, 44, 0, UTC), 
        None))
    
    DirectFollowerExtraction.directFollowerMatrix(
      events, 
      ZonedDateTime.of(2015, 1, 1, 0, 0, 0, 0, UTC), 
      ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, UTC)) shouldBe Map(
      ("Incident logging", "Incident classification") -> 2,
      ("Incident classification", "Resolution and recovery") -> 2)
  }

  "render" should "render a table" in {
    val dfm = Map(
      ("Incident logging", "Incident classification") -> 2,
      ("Incident classification", "Resolution and recovery") -> 2)
    
    DirectFollowerExtraction.render(dfm) shouldBe 
      """                       |Incident classification|Resolution and recovery
        !Incident logging       |2                      |0                      
        !Incident classification|0                      |2                      """.stripMargin('!')
  }
}
