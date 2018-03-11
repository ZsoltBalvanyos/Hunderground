package repositories

import java.util.UUID

import com.google.inject.Inject
import controllers.security.User
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile


import scala.concurrent.{ExecutionContext, Future}

class UserRepo @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._


  private class UserTable(tag: Tag) extends Table[User](tag, "user") {

    def userId = column[Int]("user_id", O.PrimaryKey, O.AutoInc)
    def firstName = column[Option[String]]("first_name")
    def lastName = column[Option[String]]("last_name")
    def emailAddress = column[String]("email_address", O.Unique)
    def password = column[String]("password")
    def salt = column[String]("salt")
    def photo = column[Option[String]]("photo")

    override def * = (userId, firstName, lastName, emailAddress, password, salt, photo) <> ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UserTable]
  db.run(DBIO.seq(users.schema.create))

  def create(firstName: Option[String], lastName: Option[String], emailAddress: String, password: String, salt: String, photo: Option[String]): Future[User] = db.run {
    (users.map(u => (u.firstName, u.lastName, u.emailAddress, u.password, u.salt, u.photo))
      returning users.map(_.userId)
      into ((data, id) => User(id, data._1, data._2, data._3, data._4, data._5, data._6))
      ) += (firstName, lastName, emailAddress, password, salt, photo)
  }

  def getUser(userId: Int): Future[Option[User]] = db.run(users.filter(_.userId === userId).result.headOption)

  def getUsers: Future[Seq[User]] = db.run(users.result)

  def findByEmailAddress(email: String): Future[Option[User]] = db.run(users.filter(_.emailAddress === email).result.headOption)
}
