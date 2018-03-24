package repositories

import java.sql.{Date, Time}
import java.time.{LocalDate, LocalTime}
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

import com.google.inject.Inject
import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.{JdbcBackend, JdbcProfile}
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class EventRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  val eventId = new AtomicLong(0)

  case class EventId(id: Long)

  implicit private class EventIdTable(tag: Tag) extends Table[EventId](tag, "event_id") {
    def eventId     = column[Long]("id", O.PrimaryKey, O.AutoInc)
    override def * = eventId <> ((EventId.apply _), EventId.unapply)
  }

  private val eventIds = TableQuery[EventIdTable]
  db.run(DBIO.seq(eventIds.schema.create))

  def getEventId: Future[EventId] = db.run {
    (eventIds.map(e => ())
      returning eventIds.map(_.eventId)
      into ((data, id) => EventId(id))
      ) += ()
  }


  val idFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  private implicit val localDateToDate = dbConfig.profile.MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l),
    d => d.toLocalDate
  )

  private implicit val localDateTimeToTime = dbConfig.profile.MappedColumnType.base[LocalTime, Time](
    l => Time.valueOf(l),
    t => t.toLocalTime
  )

  def list(): Future[Seq[Event]] =
    for{
      gigs        <- listGigs()
      memos       <- listMemos()
      holidays    <- listHolidays()
      rehearsals  <- listRehearsals()
    } yield gigs ++ memos ++ holidays ++ rehearsals



  def futureEvents(year: Int, month: Int): Future[Seq[Event]] =
    for {
      gigs        <- futureGigs(year, month)
      memos       <- futureHolidays (year, month)
      holidays    <- futureMemos (year, month)
      rehearsals  <- futureRehearsals (year, month)
    } yield gigs ++ memos ++ holidays ++ rehearsals


  /*GIG*/
  implicit private class GigTable(tag: Tag) extends Table[Gig](tag, "gig") {
    def eventId     = column[Long]("event_id", O.PrimaryKey)
    def date        = column[LocalDate]("date")
    def location    = column[String]("location")

    def * = (eventId, date, location) <> ((Gig.apply _).tupled, Gig.unapply)
  }

  private val gigs = TableQuery[GigTable]
  db.run(DBIO.seq(gigs.schema.create))

  def createGig(date: LocalDate, location: String): Future[Gig] =
    getEventId
      .map(eventId => Gig(eventId.id, date, location))
      .flatMap(gig => db.run(gigs +=  gig).map(_ => gig))

  def listGigs(): Future[Seq[Gig]] = db.run {
    gigs.result
  }

  def futureGigs(year: Int, month: Int): Future[Seq[Gig]] = db.run {
    gigs.filter(_.date > LocalDate.of(year, month, 1)).result
  }

  def deleteGig(eventId: String): Future[Int] = db.run {
    gigs.filter(_.eventId === eventId.toLong).delete
  }

  def updateGig(gig: Gig) = db.run {
    gigs.filter(_.eventId === gig.eventId).update(gig)
  }

  /*MEMO*/
  private class MemoTable(tag: Tag) extends Table[Memo](tag, "memo") {
    def eventId     = column[Long]("event_id", O.PrimaryKey)
    def date        = column[LocalDate]("date")
    def memo    = column[String]("memo")

    def * = (eventId, date, memo) <> ((Memo.apply _).tupled, Memo.unapply)
  }

  private val memos: TableQuery[MemoTable]= TableQuery[MemoTable]
  db.run(DBIO.seq(memos.schema.create))

  def createMemo(date: LocalDate, text: String): Future[Memo] =
    getEventId
      .map(eventId => Memo(eventId.id, date, text))
      .flatMap(memo => db.run(memos += memo).map(_ => memo))

  def listMemos(): Future[Seq[Memo]] = db.run {
    memos.result
  }

  def futureMemos(year: Int, month: Int): Future[Seq[Memo]] = db.run {
    memos.filter(_.date > LocalDate.of(year, month, 1)).result
  }

  def deleteMemo(eventId: String): Future[Int] = db.run {
    memos.filter(_.eventId === eventId.toLong).delete
  }

  def updateMemo(memo: Memo) = db.run {
    memos.filter(_.eventId === memo.eventId).update(memo)
  }

  /*HOLIDAY*/
  private class HolidayTable(tag: Tag) extends Table[Holiday](tag, "holiday") {
    def eventId     = column[Long]("event_id", O.PrimaryKey)
    def date        = column[LocalDate]("date")
    def userId      = column[Int]("user_id")
    def start       = column[LocalDate]("start")
    def finish      = column[LocalDate]("finish")

    def * = (eventId, date, userId, start, finish) <> ((Holiday.apply _).tupled, Holiday.unapply)
  }

  private val holidays = TableQuery[HolidayTable]
  db.run(DBIO.seq(holidays.schema.create))

  def createHoliday(date: LocalDate, userId: Int, start: LocalDate, finish: LocalDate): Future[Holiday] =
    getEventId
      .map(eventId => Holiday(eventId.id, date, userId, start, finish))
      .flatMap(holiday => db.run(holidays += holiday).map(_ => holiday))


  def listHolidays(): Future[Seq[Holiday]] = db.run {
    holidays.result
  }

  def futureHolidays(year: Int, month: Int): Future[Seq[Holiday]] = db.run {
    holidays.filter(_.date > LocalDate.of(year, month, 1)).result
  }

  def deleteHoliday(eventId: String): Future[Int] = db.run {
    holidays.filter(_.eventId === eventId.toLong).delete
  }

  def deleteHoliday(userId: Int, start: LocalDate, finish: LocalDate): Future[Int] = db.run {
    holidays
      .filter(_.userId === userId)
      .filter(_.start === start)
      .filter(_.finish === finish)
      .delete
  }

  def updateHoliday(holiday: Holiday) = db.run {
    holidays.filter(_.eventId === holiday.eventId).update(holiday)
  }

  /*REHEARSAL*/
  private class RehearsalTable(tag: Tag) extends Table[Rehearsal](tag, "rehearsal") {
    def eventId     = column[Long]("event_id", O.PrimaryKey)
    def date        = column[LocalDate]("date")
    def location    = column[String]("location")
    def start       = column[LocalTime]("start")
    def finish      = column[LocalTime]("finish")

    def * = (eventId, date, location, start, finish) <> ((Rehearsal.apply _).tupled, Rehearsal.unapply)
  }

  private val rehearsals = TableQuery[RehearsalTable]
  db.run(DBIO.seq(rehearsals.schema.create))

  def createRehearsal(date: LocalDate, location: String, start: LocalTime, finish: LocalTime): Future[Rehearsal] =
    getEventId
      .map(eventId => Rehearsal(eventId.id, date, location, start, finish))
      .flatMap(rehearsal => db.run(rehearsals += rehearsal).map(_ => rehearsal))


  def listRehearsals(): Future[Seq[Rehearsal]] = db.run {
    rehearsals.result
  }

  def futureRehearsals(year: Int, month: Int): Future[Seq[Rehearsal]] = db.run {
    rehearsals.filter(_.date > LocalDate.of(year, month, 1)).result
  }

  def deleteRehearsal(eventId: String): Future[Int] = db.run {
    rehearsals.filter(_.eventId === eventId.toLong).delete
  }

  def updateRehearsal(rehearsal: Rehearsal) = db.run {
    rehearsals.filter(_.eventId === rehearsal.eventId).update(rehearsal)
  }

}
