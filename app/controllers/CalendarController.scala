package controllers

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

import controllers.security.{SecuredAction, SecuredController}
import play.api.cache.SyncCacheApi
import play.api.mvc._
import services.CalendarService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalendarController @Inject()(val cc: ControllerComponents,
                                   calendarService: CalendarService,
                                   SecuredAction: SecuredAction,
                                   cache: SyncCacheApi)
  extends SecuredController(cc, cache) {

  val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def calendar = SecuredAction.async(parse.defaultBodyParser) { implicit request =>
    implicit val user = request.user
    Future(Redirect(routes.CalendarController.calendarFromDate(LocalDate.now.format(dtf))))
  }

  def calendarFromDate(selectedDate: String) = SecuredAction.async {implicit request =>
    implicit val user = request.user

    val date = LocalDate.parse(selectedDate, dtf)
    val months = calendarService.getCalendar(12, 42, date.getYear, date.getMonthValue)
    val firstDate = LocalDate.of(date.getYear, date.getMonthValue, 1).format(dtf)
    calendarService.getMembers.map(members => Ok(views.html.calendar(months, members.toList, firstDate)))
  }

}
