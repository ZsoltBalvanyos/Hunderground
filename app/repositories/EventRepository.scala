package repositories

import java.sql.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.google.inject.Inject
import models.Event
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class EventRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  val idFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  private implicit val localDateToDate = dbConfig.profile.MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l),
    d => d.toLocalDate
  )

  private class EventTable(tag: Tag) extends Table[Event](tag, "event") {
    def eventId     = column[Long]("event_id", O.PrimaryKey, O.AutoInc)
    def location    = column[String]("location")
    def date        = column[LocalDate]("date")
    def description = column[String]("description")

    def * = (eventId, location, date, description) <> ((Event.apply _).tupled, Event.unapply)
  }

  private val events = TableQuery[EventTable]
  db.run(DBIO.seq(events.schema.create))

  def create(location: String, date: LocalDate, description: String): Future[Event] = db.run {
    (events.map(e => (e.location, e.date, e.description))
      returning events.map(_.eventId)
      into ((data, id) => Event(id, data._1, data._2, data._3))
      ) += (location, date, description)
  }

  def list(): Future[Seq[Event]] = db.run {
    events.result
  }

  def futureEvents(year: Int, month: Int): Future[Seq[Event]] = db.run {
    events.filter(_.date > LocalDate.of(year, month, 1)).result
  }

}
