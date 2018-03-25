package services

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.google.inject.Inject
import controllers.security.User
import models._
import repositories.{BudgetRepository, EventRepository, UserRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import cats.implicits._

class BudgetService @Inject() (val budgetRepository: BudgetRepository,
                               val userRepository: UserRepository,
                               val eventRepository: EventRepository) {

  val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def eventMappings(events: Seq[Event], eventId: Long) = {

    val mapped = events.flatMap(event => event match {
      case Gig(eventId, date, location) => Some((eventId.toString, s"${event.date.format(dtf)} $location"))
      case Memo(eventId, date, text) => Some((eventId.toString, s"${event.date.format(dtf)} $text"))
      case Rehearsal(eventId, date, location, start, finish) => Some((eventId.toString, s"${event.date.format(dtf)} $location"))
      case Holiday(_, _, _, _, _) => None
    })

    mapped.find(_._1.toLong == eventId).toIndexedSeq ++ mapped.filter(_._1.toLong != eventId).toIndexedSeq
  }

  def userMappings(users: Seq[User], userId: Int) = {
    val ordered: IndexedSeq[User] = users.find(_.userID == userId).toIndexedSeq ++ users.filter(_.userID != userId).toIndexedSeq
    ordered map (user =>
      (user.userID.toString, s"${user.firstName.getOrElse("anonymous")} ${user.lastName.getOrElse("")}"))
  }

  def getEntryViews(entries: Seq[BudgetEntry], events: Seq[Event], users: Seq[User]): Seq[BudgetEntryView] = entries.map(entry => {

    val payment = if(entry.done) "Done" else "Pending"
    val event = events.find(_.eventId == entry.event)

    val name = for {
      user      <- users.find(_.userID == entry.who)
      firstName <- user.firstName
      lastName  <- user.lastName
    } yield s"$firstName $lastName"

    def getView(eventType: String, eventDesc: String, eventDate: LocalDate) =
      BudgetEntryView(
        entry.budgetEntryId,
        entry.event,
        eventType,
        eventDesc,
        eventDate,
        entry.date,
        if(entry.desc.isEmpty) "..." else entry.desc,
        name.getOrElse("Unknown"),
        payment,
        entry.amount,
        entry.direction.toString,
        entry.by)

    event match {
      case None => getView("Unknown", "Unknown", entry.date)
      case Some(e) =>  e match {
        case Gig(eventId, date, location) => getView("Gig", location, date)
        case Memo(eventId, date, text) => getView("Memo", text, date)
        case Rehearsal(eventId, date, location, start, finish) => getView("Rehearsal", location, date)
        case Holiday(_, date, _, _, _) => getView("Holiday", "", date)
      }
    }
  })

  def getBalance(entries: Seq[BudgetEntry]): Double = entries.map(_.amount).sum

  def getEntry(entryId: Long) = budgetRepository.getEntry(entryId)

  def deleteEntry(entryId: Long) = budgetRepository.delete(entryId)

  def updateEntry(entry: BudgetEntry) = budgetRepository.update(entry)

  def getIndividualBalance(users: Seq[User], entries: Seq[BudgetEntry]): Future[Map[String, Double]] = {
    val sharedAmount = entries.filter(_.done == true).map(_.amount).sum / users.size

    def getName(userId: Int): Future[String] = {
      userRepository.getUser(userId).map(u => u match {
        case Some(user) if user.firstName.isDefined && user.lastName.isDefined =>
          s"${user.firstName.get} ${user.lastName.get}"
        case None =>
          "Mr Unknown"
      })
    }

    val sharedByIndividual: Future[Map[String, Double]] =
      users
        .map(_.userID).map(getName)
        .toList
        .sequence[Future, String]
        .map(_.map((_, sharedAmount)))
        .map(_.toMap)

    val personalAmount = entries
      .filter(_.done == false)
      .groupBy(_.who)
      .map(person => getName(person._1).map(k => (k, (person._2.map(_.amount).sum * -1))))
      .toList
      .sequence[Future, (String, Double)]
      .map(_.toMap)

    for {
      shared   <- sharedByIndividual
      personal <- personalAmount
    } yield shared |+| personal
  }
}
