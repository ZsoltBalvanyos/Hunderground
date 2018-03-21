package repositories

import java.time.{LocalDate, LocalTime}

import org.scalacheck.{Arbitrary, Gen, ScalacheckShapeless}
import org.scalatest.{FlatSpec, WordSpec}
import play.api.inject.guice.GuiceApplicationBuilder
import util.CommonSpec
import org.scalacheck.ScalacheckShapeless._

class EventRepositorySpec extends WordSpec with CommonSpec {

  case class RawMemo(date: LocalDate, memo: String)
  case class RawRehearsal(date: LocalDate, location: String, start: LocalTime, finish: LocalTime)
  case class RawHoliday(date: LocalDate, userId: Int)
  case class RawGig(date: LocalDate, location: String)
  
  val eventRepository = new GuiceApplicationBuilder().injector().instanceOf[EventRepository]

  "EventRepository" should {

    "find past dates" when {

      implicit def localDateArb: Arbitrary[LocalDate] = Arbitrary(LocalDate.now().minusDays(3))
      implicit def localTimeArb: Arbitrary[LocalTime] = Arbitrary(LocalTime.now())

      "save and store events" in forAll {

        (rawGig:RawGig, rawHoliday: RawHoliday, rawMemo: RawMemo, rawRehearsal: RawRehearsal) =>

          val gig = eventRepository.createGig(rawGig.date, rawGig.location).futureValue
          val memo = eventRepository.createMemo(rawMemo.date, rawMemo.memo).futureValue
          val holiday = eventRepository.createHoliday(rawHoliday.date, rawHoliday.userId).futureValue
          val rehearsal = eventRepository.createRehearsal(rawRehearsal.date, rawRehearsal.location, rawRehearsal.start, rawRehearsal.finish).futureValue

          val events = eventRepository.list().futureValue

          events should contain(gig)
          events should contain(memo)
          events should contain(holiday)
          events should contain(rehearsal)
      }
    }

    "find future dates" when {
      implicit def localDateArb: Arbitrary[LocalDate] = Arbitrary(LocalDate.now().plusDays(3))
      implicit def localTimeArb: Arbitrary[LocalTime] = Arbitrary(LocalTime.now())

      "save and store events" in forAll {
        (rawGig:RawGig, rawHoliday: RawHoliday, rawMemo: RawMemo, rawRehearsal: RawRehearsal) =>

          val now = LocalDate.now

          val gig = eventRepository.createGig(rawGig.date, rawGig.location).futureValue
          val memo = eventRepository.createMemo(rawMemo.date, rawMemo.memo).futureValue
          val holiday = eventRepository.createHoliday(rawHoliday.date, rawHoliday.userId).futureValue
          val rehearsal = eventRepository.createRehearsal(rawRehearsal.date, rawRehearsal.location, rawRehearsal.start, rawRehearsal.finish).futureValue

          val events = eventRepository.futureEvents(now.getYear, now.getMonthValue).futureValue

          events should contain(gig)
          events should contain(memo)
          events should contain(holiday)
          events should contain(rehearsal)
      }
    }
  }

}
