//package repositories
//
//import java.time.LocalDate
//
//import models.Event
//import play.api.db.slick.DatabaseConfigProvider
//import slick.jdbc.JdbcProfile
//import slick.lifted.TableQuery
//import slick.lifted
//
//import scala.concurrent.{ExecutionContext, Future}
//
//abstract class EventRep[E <: Event](dbConfigProvider: DatabaseConfigProvider) {
//
//  val table: TableQuery[E]
//
//  private val dbConfig = dbConfigProvider.get[JdbcProfile]
//  import dbConfig._
//  import profile.api._
//
//  def listEvents = db.run(table.result)
//
////  def futureEvents(year: Int, month: Int): Future[Seq[E]] = db.run {
////    table.filter(_.date.compareTo(LocalDate.now) > 0).result
////  }
//}
