import sbt._

object Dependencies {

  lazy val slick           = "com.typesafe.slick"   % "slick_2.11"            % "3.0.0"
  lazy val h2              = "com.h2database"       % "h2"                    % "1.4.196"
  lazy val mysql           = "mysql"                % "mysql-connector-java"  % "5.1.16"
  lazy val postgresql      = "org.postgresql"       % "postgresql"            % "9.3-1102-jdbc41"
  lazy val pgSlick         = "com.github.tminglei" 	%% "slick-pg" 			      % "0.16.0"
  lazy val typeSafeConfig  = "com.typesafe"         % "config"                % "1.3.1"
  lazy val abstractj       = "org.abstractj.kalium" % "kalium"                % "0.7.0"
  lazy val scalaTest       = "org.scalatest"       %% "scalatest"             % "3.0.3"   % Test
  lazy val scalaCheck      = "org.scalacheck"      %% "scalacheck"            % "1.13.5"  % Test
  lazy val scalaCheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.6" % Test


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
