package controllers

import controllers.security.SecuredAction
import org.scalatest.FlatSpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{ActionBuilder, AnyContent, ControllerComponents, Request}
import util.{CommonSpec, RawModels}
import org.scalacheck.ScalacheckShapeless._

import scala.concurrent.Future

class SongControllerSpec extends FlatSpec with CommonSpec {

  val controllerComponents: ControllerComponents = new GuiceApplicationBuilder().injector().instanceOf[ControllerComponents]
  val songController: SongController = new GuiceApplicationBuilder().injector().instanceOf[SongController]

  it should "" in forAll { (rawSong: RawSong) =>
    songController.addSong(rawSong.artist, rawSong.title, rawSong.key, "ready")
    println(songController.getSongs.getClass)
    println(songController.getSongs.apply())
    songController
  }

}
