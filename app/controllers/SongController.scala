package controllers

import com.google.inject.Inject
import controllers.security.{SecuredAction, SecuredController}
import models.Song
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
        val songs = groupedSongs.get("song").getOrElse(List[Song]())
        val backlogs = groupedSongs.get("backlog").getOrElse(List[Song]())
        Ok(views.html.song(songs, backlogs))
      })
  }

  def addSong(artist: String, title: String, key: String, status: String) = SecuredAction { implicit request =>
    implicit val user = request.user
    songRepository.create(artist, title, status, key)
    Ok
  }

  def deleteSong(songId: String) = SecuredAction { implicit request =>
    implicit val user = request.user
    songRepository.delete(songId)
    Ok
  }

}
