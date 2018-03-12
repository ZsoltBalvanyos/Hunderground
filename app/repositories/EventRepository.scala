package repositories

import java.sql.{Date, Time}
import java.time.{LocalDate, LocalTime}
import java.time.format.DateTimeFormatter

import cats.Applicative
import com.google.inject.Inject
import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import cats.data._
import cats.implicits._
import cats.instances.future._

import scala.collection.immutable

class EventRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

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
  private class GigTable(tag: Tag) extends Table[Gig](tag, "gig") {
    def eventId     = column[Long]("event_id", O.PrimaryKey, O.AutoInc)
    def date        = column[LocalDate]("date")
    def location    = column[String]("location")

    def * = (eventId, date, location) <> ((Gig.apply _).tupled, Gig.unapply)
  }

  private val gigs = TableQuery[GigTable]
  db.run(DBIO.seq(gigs.schema.create))

  def createGig(date: LocalDate, location: String): Future[Gig] = db.run {
    (gigs.map(e => (e.date, e.location))
      returning gigs.map(_.eventId)
      into ((data, id) => Gig(id, data._1, data._2))
      ) += (date, location)
  }

  def listGigs(): Future[Seq[Gig]] = db.run {
    gigs.result
  }

  def futureGigs(year: Int, month: Int): Future[Seq[Gig]] = db.run {
    gigs.filter(_.date > LocalDate.of(year, month, 1)).result
  }

  /*MEMO*/
  private class MemoTable(tag: Tag) extends Table[Memo](tag, "memo") {
    def eventId     = column[Long]("event_id", O.PrimaryKey, O.AutoInc)
    def date        = column[LocalDate]("date")
    def memo    = column[String]("memo")

    def * = (eventId, date, memo) <> ((Memo.apply _).tupled, Memo.unapply)
  }

  private val memos = TableQuery[MemoTable]
  db.run(DBIO.seq(memos.schema.create))

  def createMemo(date: LocalDate, memo: String): Future[Memo] = db.run {
    (memos.map(e => (e.date, e.memo))
      returning memos.map(_.eventId)
      into ((data, id) => Memo(id, data._1, data._2))
      ) += (date, memo)
  }

  def listMemos(): Future[Seq[Memo]] = db.run {
    memos.result
  }

  def futureMemos(year: Int, month: Int): Future[Seq[Memo]] = db.run {
    memos.filter(_.date > LocalDate.of(year, month, 1)).result
  }

  /*HOLIDAY*/
  private class HolidayTable(tag: Tag) extends Table[Holiday](tag, "holiday") {
    def eventId     = column[Long]("event_id", O.PrimaryKey, O.AutoInc)
    def date        = column[LocalDate]("date")
    def userId      = column[Int]("user_id")

    def * = (eventId, date, userId) <> ((Holiday.apply _).tupled, Holiday.unapply)
  }

  private val holidays = TableQuery[HolidayTable]
  db.run(DBIO.seq(holidays.schema.create))

  def createHoliday(date: LocalDate, userId: Int): Future[Holiday] = db.run {
    (holidays.map(e => (e.date, e.userId))
      returning holidays.map(_.eventId)
      into ((data, id) => Holiday(id, data._1, data._2))
      ) += (date, userId)
  }

  def listHolidays(): Future[Seq[Holiday]] = db.run {
    holidays.result
  }

  def futureHolidays(year: Int, month: Int): Future[Seq[Holiday]] = db.run {
    holidays.filter(_.date > LocalDate.of(year, month, 1)).result
  }

  /*REHEARSAL*/
  private class RehearsalTable(tag: Tag) extends Table[Rehearsal](tag, "rehearsal") {
    def eventId     = column[Long]("event_id", O.PrimaryKey, O.AutoInc)
    def date        = column[LocalDate]("date")
    def location    = column[String]("location")
    def start       = column[LocalTime]("start")
    def finish      = column[LocalTime]("finish")

    def * = (eventId, date, location, start, finish) <> ((Rehearsal.apply _).tupled, Rehearsal.unapply)
  }

  private val rehearsals = TableQuery[RehearsalTable]
  db.run(DBIO.seq(rehearsals.schema.create))

  def createRehearsal(date: LocalDate, location: String, start: LocalTime, finish: LocalTime): Future[Rehearsal] = db.run {
    (rehearsals.map(e => (e.date, e.location, e.start, e.finish))
      returning rehearsals.map(_.eventId)
      into ((data, id) => Rehearsal(id, data._1, data._2, data._3, data._4))
      ) += (date, location, start, finish)
  }

  def listRehearsals(): Future[Seq[Rehearsal]] = db.run {
    rehearsals.result
  }

  def futureRehearsals(year: Int, month: Int): Future[Seq[Rehearsal]] = db.run {
    rehearsals.filter(_.date > LocalDate.of(year, month, 1)).result
  }


}
