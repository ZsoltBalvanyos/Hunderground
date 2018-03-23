package models

import play.api.libs.json.Json

case class Song(songId: String, artist: String, title: String, status: SongStatus, key: String)

object Song {
//  implicit val eventFormat = Json.format[Song]
}