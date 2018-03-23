package models

import org.scalatest.FlatSpec
import util.CommonSpec
import org.scalacheck.ScalacheckShapeless._

class SongStatusSpec extends FlatSpec with CommonSpec {

  "SongStatus" should "create Ready status" in {
    SongStatus.fromString("ready") shouldBe Right(Ready)
  }
  "SongStatus" should "create Backlog status" in {
    SongStatus.fromString("backlog") shouldBe Right(Backlog)
  }
  "SongStatus" should "return error message" in {
    SongStatus.fromString("xxx") shouldBe Left("'xxx' is not a valid status")
  }
}
