import sbt._

object Dependencies {

  val slick           = "com.typesafe.slick"   % "slick_2.11"            % "3.0.0"
  val h2              = "com.h2database"       % "h2"                    % "1.4.196"
  val mysql           = "mysql"                % "mysql-connector-java"  % "5.1.16"
  val typeSafeConfig  = "com.typesafe"         % "config"                % "1.3.1"
  val abstractj       = "org.abstractj.kalium" % "kalium" % "0.7.0"
  val scalaTest       = "org.scalatest"       %% "scalatest" % "3.0.3"   %  Test

  object PlaySlick {
    private val version = "3.0.3"

    lazy val slick = "com.typesafe.play" %% "play-slick" % version
    lazy val slickEvolutions = "com.typesafe.play" %% "play-slick-evolutions" % version
  }

  object Cats {
    private val version = "1.0.1"

    lazy val core = "org.typelevel" % "cats-core_2.12" % version
  }

  object Circe {
    private val version = "0.9.0"

    lazy val core     = "io.circe"     %% "circe-core"        % version
    lazy val literal  = "io.circe"     %% "circe-literal"     % version
    lazy val parser   = "io.circe"     %% "circe-parser"      % version
    lazy val generic  = "io.circe"     %% "circe-generic"     % version
    lazy val shapes   = "io.circe"     %% "circe-shapes"      % version
    lazy val java8    = "io.circe"     %% "circe-java8"       % version
  }

}
