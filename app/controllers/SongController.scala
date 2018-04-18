package controllers

import com.google.inject.Inject
import controllers.security.{SecuredAction, SecuredController}
import models.{Backlog, Ready, Song, SongStatus}
import play.api.cache.SyncCacheApi
import play.api.mvc.ControllerComponents
import repositories.SongRepository

import scala.concurrent.ExecutionContext.Implicits.global

class SongController @Inject()(cc: ControllerComponents,
                               songRepository: SongRepository,
                               SecuredAction: SecuredAction,
                               cache: SyncCacheApi)
  extends SecuredController(cc, cache) {

  def getSongs = SecuredAction.async { implicit request =>
    implicit val user = request.user

    songRepository
      .list()
      .map(songs => songs.groupBy(_.status))
      .map(groupedSongs => {
        val songs = groupedSongs.get(Ready).getOrElse(List[Song]())
        val backlogs = groupedSongs.get(Backlog).getOrElse(List[Song]())
        Ok(views.html.song(songs, backlogs))
      })
  }

  def addSong(artist: String, title: String, key: String, status: String) = SecuredAction { implicit request =>
    implicit val user = request.user
    SongStatus.fromString(status).fold(
      _ => BadRequest(s"Invalid song status: $status"),
      songStatus => {
        songRepository.create(artist, title, songStatus, key)
        Ok
      }
    )
  }

  def deleteSong(songId: String) = SecuredAction { implicit request =>
    implicit val user = request.user
    songRepository.delete(songId.toLong)
    Ok
  }

}
