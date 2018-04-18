package repositories

import com.google.inject.Inject
import models.{Backlog, Ready, Song, SongStatus}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class SongRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private implicit val songStatusCodec = dbConfig.profile.MappedColumnType.base[SongStatus, String](
    songStatus => {
      songStatus match {
        case Ready => "ready"
        case Backlog => "backlog"
      }
    },
    string => SongStatus.fromString(string).right.get
  )

  private class SongTable(tag: Tag) extends Table[Song](tag, "song") {
    def songId = column[Long]("song_id", O.PrimaryKey, O.AutoInc)
    def artist = column[String]("artist")
    def title  = column[String]("title")
    def status = column[SongStatus]("status")
    def key    = column[String]("key")
    override def * = (songId, artist, title, status, key) <> ((Song.apply _).tupled, Song.unapply)
  }

  private val songs = TableQuery[SongTable]
  db.run(DBIO.seq(songs.schema.create))

  def create(artist: String, title: String, status: SongStatus, key: String) = db.run {
    (songs.map(s => (s.artist, s.title, s.status, s.key))
      returning songs.map(_.songId)
      into ((data, id) => Song(id, data._1, data._2, data._3, data._4))
      ) += (artist, title, status, key)
  }

  def list(): Future[Seq[Song]] = db.run(songs.result)

  def delete(songId: Long) = db.run(songs.filter(_.songId === songId).delete)
}
