package models

case class Song(songId: Long, artist: String, title: String, status: SongStatus, key: String)