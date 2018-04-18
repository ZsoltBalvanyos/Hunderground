package controllers

import java.time.LocalDate
import javax.inject.Inject

import controllers.security.{SecuredAction, SecuredController, User}
import forms.{BudgetForm, BudgetUpdateForm}
import models._
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, ControllerComponents, Request}
import services.BudgetService

import scala.concurrent.{ExecutionContext, Future}

class BudgetController @Inject()(cc: ControllerComponents,
                                 SecuredAction: SecuredAction,
                                 budgetService: BudgetService,
                                 cache: SyncCacheApi)(implicit ec: ExecutionContext) extends SecuredController(cc, cache) with I18nSupport  {


  def budgetPage = SecuredAction.async { implicit request =>
    implicit val user: Option[User] = request.user

    val now = LocalDate.now

    for {
      users: Seq[User] <- budgetService.userRepository.getUsers
      events: Seq[Event] <- budgetService.eventRepository.list
      entries: Seq[BudgetEntry] <- budgetService.budgetRepository.list
      entryViews: Seq[BudgetEntryView] <- Future(budgetService.getEntryViews(entries, events, users))
      individualBalances: Map[String, Double] <- budgetService.getIndividualBalance(users, entries)
    } yield
      Ok(views.html.budget(
        forms.BudgetForm.form(user.get),
        entryViews,
        budgetService.eventMappings(events, None),
        budgetService.userMappings(users, user.get.userID),
        budgetService.getBalance(entries),
        individualBalances)
      )
  }

  def budgetUpdate(entryId: String) = SecuredAction.async { implicit request: Request[AnyContent] =>
    implicit val user = request.user

    val now = LocalDate.now

    def getForm(entry: Option[BudgetEntry]) = entry match {
      case None => forms.BudgetForm.form(user.get)
      case Some(data) =>
        BudgetForm
          .form(user.get)
          .fill(BudgetForm.Data(
            data.budgetEntryId,
            data.date,
            data.desc,
            data.who,
            data.done,
            BigDecimal(data.amount),
            data.direction.toString))
    }

    for {
      entry: Option[BudgetEntry] <- budgetService.getEntry(entryId.toLong)
      users: Seq[User] <- budgetService.userRepository.getUsers
      events: Seq[Event] <- budgetService.eventRepository.list
      entryViews: Seq[BudgetEntryView] <- Future(budgetService.getEntryViews(Seq(entry.get), events, users))
    } yield
      Ok(views.html.budgetUpdate(
        getForm(entry),
        entryViews.head,
        budgetService.eventMappings(events, entry),
        budgetService.userMappings(users, entry.get.who))
      )
  }

  def delete(entryId: String) = SecuredAction.async { implicit request =>
    implicit val user = request.user

    budgetService.deleteEntry(entryId.toLong).map(_ => Ok)
  }

  def update = SecuredAction.async { implicit request =>
    implicit val user = request.user

    BudgetUpdateForm.form.bindFromRequest.fold(
      _ => Future(BadRequest),
      entry => {
        BudgetEntryDirection.fromString(entry.direction).toOption flatMap { direction: BudgetEntryDirection =>
          user.map(u => {
            val sign = if (direction == Income) 1 else -1

              BudgetEntry(
                entry.entryId,
                entry.event,
                entry.date,
                entry.desc,
                entry.who,
                entry.done,
                Math.abs(entry.amount.toDouble) * sign,
                direction,
                u.userID
              )
          })
        } match {
          case Some(budgetEntry) => {
            budgetService.updateEntry(budgetEntry)
            Future(Redirect(routes.BudgetController.budgetPage()))
          }
          case None => Future(Unauthorized)
        }
      }
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
          case Some( b: Future[BudgetEntry]) => b.map(_ => Redirect(routes.BudgetController.budgetPage()))
          case None => Future(Unauthorized)
        }
    )
  }
}
