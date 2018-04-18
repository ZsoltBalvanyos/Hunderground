package repositories

import java.sql.{Date, Time}
import java.time.{LocalDate, LocalTime}

import com.google.inject.Inject
import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class BudgetRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private implicit val localDateToDate = dbConfig.profile.MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l),
    d => d.toLocalDate
  )

  private implicit val localDateTimeToTime = dbConfig.profile.MappedColumnType.base[LocalTime, Time](
    l => Time.valueOf(l),
    t => t.toLocalTime
  )

  private implicit val songStatusCodec = dbConfig.profile.MappedColumnType.base[BudgetEntryDirection, String](
    budgetEntryDirection => {
      budgetEntryDirection match {
        case Income => "income"
        case Expense => "expense"
      }
    },
    string => BudgetEntryDirection.fromString(string).right.get
  )

  private class BudgetEntryTable(tag: Tag) extends Table[BudgetEntry](tag, "budget_entry") {
    def budgetEntryId = column[Long]("budget_entry_id", O.PrimaryKey, O.AutoInc)
    def event = column[Long]("event")
    def date = column[LocalDate]("date")
    def desc = column[String]("desc")
    def who = column[Int]("who")
    def done = column[Boolean]("done")
    def amount = column[Double]("amount")
    def direction = column[BudgetEntryDirection]("direction")
    def by = column[Int]("by")
    override def * = (budgetEntryId, event, date, desc, who, done, amount, direction, by) <> ((BudgetEntry.apply _).tupled, BudgetEntry.unapply)
  }

  private val budgetEntries = TableQuery[BudgetEntryTable]
  db.run(DBIO.seq(budgetEntries.schema.create))

  def create(event: Long,
             date: LocalDate,
             desc: String,
             who: Int,
             done: Boolean,
             amount: Double,
             direction: BudgetEntryDirection,
             by: Int) =
    db.run {
    (budgetEntries.map(entry => (entry.event, entry.date, entry.desc, entry.who, entry.done, entry.amount, entry.direction, entry.by))
      returning budgetEntries.map(_.budgetEntryId)
      into ((data, id) => BudgetEntry(id, data._1, data._2, data._3, data._4, data._5, data._6, data._7, data._8))
      ) += (event, date, desc, who, done, amount, direction, by)
    }

  def list(): Future[Seq[BudgetEntry]] = db.run(budgetEntries.result)

  def delete(budgetEntryId: Long) = db.run(budgetEntries.filter(_.budgetEntryId === budgetEntryId).delete)

  def update(entry: BudgetEntry) = db.run(budgetEntries.filter(_.budgetEntryId === entry.budgetEntryId).update(entry))

  def getEntry(entryId: Long): Future[Option[BudgetEntry]] = db.run(budgetEntries.filter(_.budgetEntryId === entryId).result).map(_.headOption)
}
