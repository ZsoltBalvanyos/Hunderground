package repositories

import java.time.{LocalDate, LocalTime}

import org.scalacheck.{Arbitrary, Gen, ScalacheckShapeless}
import org.scalatest.{FlatSpec, WordSpec}
import play.api.inject.guice.GuiceApplicationBuilder
import util.CommonSpec
import org.scalacheck.ScalacheckShapeless._

class EventRepositorySpec extends WordSpec with CommonSpec {
  
  val eventRepository = new GuiceApplicationBuilder().injector().instanceOf[EventRepository]

  "EventRepository" should {

    "find past dates" when {

      implicit def localDateArb: Arbitrary[LocalDate] = Arbitrary(LocalDate.now().minusDays(3))
      implicit def localTimeArb: Arbitrary[LocalTime] = Arbitrary(LocalTime.now())

      "save and store events" in forAll {

        (rawGig:RawGig, rawHoliday: RawHoliday, rawMemo: RawMemo, rawRehearsal: RawRehearsal) =>

          val gig = eventRepository.createGig(rawGig.date, rawGig.location).futureValue
          val memo = eventRepository.createMemo(rawMemo.date, rawMemo.memo).futureValue
          val holiday = eventRepository.createHoliday(rawHoliday.date, rawHoliday.userId, rawHoliday.start, rawHoliday.finish).futureValue
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
          val holiday = eventRepository.createHoliday(rawHoliday.date, rawHoliday.userId, rawHoliday.start, rawHoliday.finish).futureValue
          val rehearsal = eventRepository.createRehearsal(rawRehearsal.date, rawRehearsal.location, rawRehearsal.start, rawRehearsal.finish).futureValue

          val events = eventRepository.futureEvents(now.getYear, now.getMonthValue).futureValue

          events should contain(gig)
          events should contain(memo)
          events should contain(holiday)
          events should contain(rehearsal)
      }
    }

    "update event" when {
      implicit def localDateArb: Arbitrary[LocalDate] = Arbitrary(LocalDate.now().minusDays(3))
      implicit def localTimeArb: Arbitrary[LocalTime] = Arbitrary(LocalTime.now())

      "event is a gigs" in forAll { (rawGig: RawGig, location: String) =>
        val rehearsal = eventRepository.createGig(rawGig.date, rawGig.location).futureValue

        val events = eventRepository.list().futureValue

        events should contain(rehearsal)

        val editedGig = rehearsal.copy(location = location)

        eventRepository.updateGig(editedGig).futureValue

        eventRepository
          .listGigs()
          .futureValue
          .filter(_.eventId ===rehearsal.eventId)
          .head
          .location shouldBe location
      }

      "event is a memo" in forAll { (rawMemo: RawMemo, text: String) =>
        val memo = eventRepository.createMemo(rawMemo.date, rawMemo.memo).futureValue

        val events = eventRepository.list().futureValue

        events should contain(memo)

        val editedMemo = memo.copy(memo = text)

        eventRepository.updateMemo(editedMemo).futureValue

        eventRepository
          .listMemos()
          .futureValue
          .filter(_.eventId ===memo.eventId)
          .head
          .memo shouldBe text
      }

      "event is a holiday" in forAll { (rawHoliday: RawHoliday, userId: Int) =>
        val holiday = eventRepository.createHoliday(rawHoliday.date, rawHoliday.userId, rawHoliday.start, rawHoliday.finish).futureValue

        val events = eventRepository.list().futureValue

        events should contain(holiday)

        val editedHoliday = holiday.copy(userId = userId)

        eventRepository.updateHoliday(editedHoliday).futureValue

        eventRepository
          .listHolidays()
          .futureValue
          .filter(_.eventId ===holiday.eventId)
          .head
          .userId shouldBe userId
      }

      "event is a rehearsal" in forAll { (rawRehearsal: RawRehearsal, location: String) =>
        val rehearsal = eventRepository.createRehearsal(rawRehearsal.date, rawRehearsal.location, rawRehearsal.start, rawRehearsal.finish).futureValue

        val events = eventRepository.list().futureValue

        events should contain(rehearsal)

        val editedRehearsal = rehearsal.copy(location = location)

        eventRepository.updateRehearsal(editedRehearsal).futureValue

        eventRepository
          .listRehearsals()
          .futureValue
          .filter(_.eventId ===rehearsal.eventId)
          .head
          .location shouldBe location
      }
    }
  }

}
