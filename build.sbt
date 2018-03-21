import Dependencies._
import play.sbt.PlayImport.specs2

name := "Hunderground"
 
version := "1.0" 
      
enablePlugins(PlayScala)

scalaVersion := "2.12.4"

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