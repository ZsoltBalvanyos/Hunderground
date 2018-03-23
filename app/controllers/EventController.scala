package controllers

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalTime}
import java.util.Date

import com.google.inject.Inject
import controllers.security.{SecuredAction, SecuredController, User}
import models._
import play.api.cache.SyncCacheApi
import play.api.mvc.{BaseController, ControllerComponents, Result}
import repositories.{EventRepository, UserRepository}
import services.CalendarService

import scala.collection.immutable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._

class EventController @Inject()(cc: ControllerComponents,
                                calendarService: CalendarService,
                                SecuredAction: SecuredAction,
                                cache: SyncCacheApi,
                                eventRepository: EventRepository,
                                userRepository: UserRepository)
  extends SecuredController(cc, cache) {

  val dateSorter = (x: Event, y: Event) => x.date.isBefore(y.date)

  def getEvents = SecuredAction.async {implicit request =>
    implicit val user = request.user

    val now = LocalDate.now
    val year = now.getYear
    val month = now.getMonthValue

    for {
      gigs        <- eventRepository.futureGigs(year, month)
      rehearsals  <- eventRepository.futureRehearsals(year, month)
      memos       <- eventRepository.futureMemos(year, month)
      holidays    <- eventRepository.futureHolidays(year, month).map(holidayDisplay).map(_.distinct)
    } yield Ok(views.html.events(gigs.sortWith(dateSorter), rehearsals.sortWith(dateSorter), holidays, memos.sortWith(dateSorter)))
  }

  def holidayDisplay(holidays: Seq[Holiday]): Seq[HolidayDisplay] = holidays map { holiday =>
      Await.result(userRepository.getUser(holiday.userId), 10 seconds) match {
        case Some(user) => HolidayDisplay(s"${user.firstName.getOrElse("")} ${user.lastName.getOrElse("")}", holiday.start, holiday.finish)
        case None => HolidayDisplay(s"Somebody", holiday.start, holiday.finish)
      }
    }

  def addRehearsal(date: String, location: String, start: String, duration: String) = SecuredAction {implicit request =>
    implicit val user = request.user

    eventRepository.createRehearsal(getLocalDate(date), location, getTime(start), getTime(start).plusHours(duration.toInt))
    Ok
  }

  def addGig(date: String, location: String) = SecuredAction {implicit request =>
    implicit val user = request.user

    eventRepository.createGig(getLocalDate(date), location)
    Ok
  }

  def addMemo(date: String, memo: String) = SecuredAction {implicit request =>
    implicit val user = request.user

    eventRepository.createMemo(getLocalDate(date), memo)
    Ok
  }

  def addHoliday(start: String, person: String, till: String) = SecuredAction {implicit request =>
    implicit val user = request.user

    calendarService.addHoliday(getLocalDate(start), getLocalDate(till), person.toInt)
    Ok
  }

  def getLocalDate(date: String) = LocalDate.parse(date, eventRepository.idFormat)

  def getTime(time: String) = LocalTime.parse(time)


  def updateRehearsal(date: String, location: String, start: String, duration: String, eventId: String) = SecuredAction {implicit request =>
    implicit val user = request.user
    Await.result(eventRepository.updateRehearsal(Rehearsal(eventId.toLong, getLocalDate(date), location, getTime(start), getTime(start).plusHours(duration.toInt))), 10 seconds)
    Ok
  }

  def updateGig(date: String, location: String, eventId: String) = SecuredAction {implicit request =>
    implicit val user = request.user
    Await.result(eventRepository.updateGig(Gig(eventId.toLong, getLocalDate(date), location)), 10 seconds)
    Ok
  }

  def updateMemo(date: String, memo: String, eventId: String) = SecuredAction {implicit request =>
    implicit val user = request.user
    Await.result(eventRepository.updateMemo(Memo(eventId.toLong, getLocalDate(date), memo)), 10 seconds)
    Ok
  }

  def updateHoliday(userId: String, oldStart: String, oldFinish: String, newStart: String, newFinish: String) = SecuredAction {implicit request =>
    implicit val user = request.user

    Await.result(eventRepository.deleteHoliday(userId.toInt, getLocalDate(oldStart), getLocalDate(oldFinish)), 10 seconds)
    calendarService.addHoliday(getLocalDate(newStart), getLocalDate(newFinish), userId.toInt)
    Ok
  }

  def deleteRehearsal(eventId: String) = SecuredAction { implicit request =>
    implicit val user = request.user

    Await.result(eventRepository.deleteRehearsal(eventId), 10 seconds)
    Ok
  }

  def deleteGig(eventId: String) = SecuredAction { implicit request =>
    implicit val user = request.user

    Await.result(eventRepository.deleteGig(eventId), 10 seconds)
    Ok
  }

  def deleteMemo(eventId: String) = SecuredAction { implicit request =>
    implicit val user = request.user

    Await.result(eventRepository.deleteMemo(eventId), 10 seconds)
    Ok
  }

  def deleteHoliday(userId: String, start: String, finish: String) = SecuredAction { implicit request =>
    implicit val user = request.user

    Await.result(eventRepository.deleteHoliday(userId.toInt, getLocalDate(start), getLocalDate(finish)), 10 seconds)
    Ok
  }

}
