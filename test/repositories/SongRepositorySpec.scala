package repositories

import models.Song
import org.scalatest.FlatSpec
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalacheck.ScalacheckShapeless._
import util.CommonSpec

class SongRepositorySpec extends FlatSpec with CommonSpec {

  val songRepository: SongRepository = new GuiceApplicationBuilder().injector().instanceOf[SongRepository]

  it should "save and store song" in forAll() { rawSong: RawSong =>

    val song: Song = songRepository.create(rawSong.artist, rawSong.title, rawSong.status, rawSong.key).futureValue

    song.artist  shouldBe rawSong.artist
    song.title   shouldBe rawSong.title
    song.status  shouldBe rawSong.status
    song.key     shouldBe rawSong.key

    songRepository.list().futureValue should contain(song)

  }

  it should "delete song" in forAll() { rawSong: RawSong =>

    val song: Song = songRepository.create(rawSong.artist, rawSong.title, rawSong.status, rawSong.key).futureValue

    song.artist  shouldBe rawSong.artist
    song.title   shouldBe rawSong.title
    song.status  shouldBe rawSong.status
    song.key     shouldBe rawSong.key

    val deletedSongs: Int = songRepository.delete(song.songId).futureValue

    deletedSongs shouldBe 1

    songRepository.list().futureValue shouldNot contain(song)

  }

}
