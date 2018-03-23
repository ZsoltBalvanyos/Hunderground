package services

import java.time.format.{DateTimeFormatter, TextStyle}
import java.time.{DayOfWeek, LocalDate, Month => MonthOfYear}
import java.util.{GregorianCalendar, Locale}

import com.google.inject.Inject
import models._
import repositories.{EventRepository, UserRepository}
import util.{AppError, InvalidDateOrderError}

import scala.annotation.tailrec
import scala.collection.immutable
import scala.concurrent.Await
import scala.concurrent.duration._

case class Day(dayId: String,
               formattedDate: String,
               events: List[CalendarEntry],
               style: String)

case class Month(name: String, days: List[Day])

class CalendarService @Inject() (eventRepository: EventRepository,
                                 userRepository: UserRepository) {

  private val viewFormat = DateTimeFormatter.ofPattern("dd")
  private val rehearsalFormat = DateTimeFormatter.ofPattern("HH")

  def getCalendar(displayedMonths: Int,
                  placeHolderInMonth: Int,
                  year: Int,
                  month: Int): List[Month] = {

    val listOfSavedEvents = Await.result(eventRepository.futureEvents(year, month), 10.seconds).groupBy(_.date)

    def getData(date: LocalDate): Day = {

      val twoCharDay = date
        .getDayOfWeek
        .getDisplayName(TextStyle.SHORT, Locale.UK)
        .take(2)

      val formattedDate = s"${date.format(viewFormat)} $twoCharDay"

      val weekend = date.getDayOfWeek match {
        case DayOfWeek.SATURDAY => "day we"
        case DayOfWeek.SUNDAY   => "day we"
        case _                  => "day"
      }

      def events = listOfSavedEvents.get(date) match {
        case Some(events) => getCalendarEntries(events)
        case None         => List[CalendarEntry]()
      }

      def getCalendarEntries(events: Seq[Event]): List[CalendarEntry] = events.toList map {
        case gig @ Gig(_, _, location)        => CalendarEntry(location, "gigLabel", gig)
        case memo @ Memo(_, _, text)          => CalendarEntry(text, "memoLabel", memo)
        case holiday @ Holiday(_, _, userId, _, _)  =>
          val user = Await.result(userRepository.getUser(userId), 10 seconds).flatMap(_.firstName).getOrElse("anonymus")
          CalendarEntry(user, "holidayLabel", holiday)
        case rehearsal @ Rehearsal(_, _, location, start, finish) =>
          CalendarEntry(s"$location ${start.format(rehearsalFormat)}-${finish.format(rehearsalFormat)}", "rehearsalLabel", rehearsal)
      }

      Day(date.format(eventRepository.idFormat), formattedDate, events, weekend)
    }

    def getDays(year: Int, month: Int): List[Day] =
      (1 to lengthOfMonth(year, month))
        .map(d => LocalDate.of(year, month, d))
        .foldLeft(List[Day]())((acc, elem) => getData(elem) :: acc)
        .reverse

    val getMonth = (month: Int) => {
      val thisMonth = if(month % 12 == 0) 12 else month % 12
      val thisYear  = if(month > 12) year + 1 else year

      val name = MonthOfYear.of(thisMonth)
        .toString
        .toLowerCase
        .capitalize

      val prefix = offset(thisYear, thisMonth)
      val days   = getDays(thisYear, thisMonth)
      val suffix = 1 to 42 - prefix.size - days.size map(_ => emptyDay)

      Month(name, (prefix ++ days ++ suffix) toList)
    }

    month until month + displayedMonths map getMonth toList
  }

  def lengthOfMonth(year: Int, month: Int) = {
    val MONTH_LENGTH      = Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    val LEAP_MONTH_LENGTH = Array(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    val isLeapYear        = new GregorianCalendar().isLeapYear(year)

    if (isLeapYear)
      LEAP_MONTH_LENGTH(month - 1)
    else
      MONTH_LENGTH(month - 1)
  }

  val emptyDay  = Day("empty", "", List[CalendarEntry](), "empty")

  def offset(year: Int, month: Int): immutable.Seq[Day] = {
    val localDate = LocalDate.of(year, month, 1)
    val offset    = (localDate.getDayOfWeek.getValue + 6) % 7
    1 to offset map(_ => emptyDay)
  }

  def addHoliday(start: LocalDate, finish: LocalDate, person: Int): Either[AppError, Unit] = {

    if(start.compareTo(finish) > 0) return Left(InvalidDateOrderError(start, finish))

    @tailrec
    def getPeriod(period: List[LocalDate] = List[LocalDate](), nextDay: LocalDate = start): List[LocalDate] = {
      if(nextDay.isEqual(finish)) return nextDay :: period
      getPeriod(nextDay :: period, nextDay.plusDays(1))
    }

    getPeriod().foreach(date => eventRepository.createHoliday(date, person, start, finish))

    Right(())
  }

  def getMembers = userRepository.getUsers

}
