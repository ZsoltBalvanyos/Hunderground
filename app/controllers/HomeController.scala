package controllers

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject._

import controllers.security.{SecuredAction, SecuredController, User}
import forms.BudgetForm
import models._
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.BudgetService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               SecuredAction: SecuredAction,
                               budgetService: BudgetService,
                               cache: SyncCacheApi)(implicit ec: ExecutionContext) extends SecuredController(cc, cache) with I18nSupport  {

  def index = Action { implicit request: Request[AnyContent] =>
    implicit val user = request.user
    Ok(views.html.index("Your new application is ready."))
  }

  def budgetForm = SecuredAction.async { implicit request =>
    implicit val user: Option[User] = request.user

    val now = LocalDate.now

    for {
      users: Seq[User] <- budgetService.userRepository.getUsers
      events: Seq[Event] <- budgetService.eventRepository.futureEvents(now.getYear, now.getMonthValue)
      entries: Seq[BudgetEntry] <- budgetService.budgetRepository.list
      entryViews: Seq[BudgetEntryView] <- Future(budgetService.getEntryViews(entries, events, users))
      individualBalances: Map[String, Double] <- budgetService.getIndividualBalance(users, entries)
    } yield
      Ok(views.html.budget(
        forms.BudgetForm.form(user.get),
        entryViews,
        budgetService.eventMappings(events).sortBy(_._2),
        budgetService.userMappings(users),
        budgetService.getBalance(entries),
        individualBalances)
      )
  }

  def budget = SecuredAction.async { implicit request =>
    implicit val user = request.user

    BudgetForm.form(user.get).bindFromRequest.fold(
      _     => Future(BadRequest),
      entry =>
        BudgetEntryDirection.fromString(entry.direction).toOption flatMap { direction =>
          user.map(u => {
            val sign = if(direction == Income) 1 else -1
            println(s"entry of ${entry.who}")
            budgetService.budgetRepository.create(
              entry.event,
              entry.date,
              entry.desc,
              entry.who,
              entry.done,
              entry.amount.toDouble * sign,
              direction,
              u.userID
            )
          })
        } match {
        case Some( b: Future[BudgetEntry]) => b.map(_ => Redirect(routes.HomeController.budgetForm()))
        case None => Future(Unauthorized)
      }
    )
  }
}
