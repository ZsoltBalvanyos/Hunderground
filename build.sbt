import Dependencies._
import play.sbt.PlayImport.specs2

name := "Hunderground"
 
version := "1.0" 
      
lazy val `Hunderground` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  ehcache,
  guice,
  h2,
  mysql,
  typeSafeConfig,
  Cats.core,
  PlaySlick.slick,
  PlaySlick.slickEvolutions,
  specs2 % Test,
  scalaTest,
  scalaCheck,
  scalaCheckShapeless
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )
