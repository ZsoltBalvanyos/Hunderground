import Dependencies._
import play.sbt.PlayImport.specs2
import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy

name := "Hunderground"
 
version := "1.0.16"
      
enablePlugins(PlayScala)

scalaVersion := "2.12.4"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _) => MergeStrategy.discard
  case _                       => MergeStrategy.first
}

libraryDependencies ++= Seq(
  ehcache,
  guice,
//  h2,
  mysql,
//  postgresql,
//  pgSlick,
  typeSafeConfig,
  Cats.core,
  PlaySlick.slick,
  PlaySlick.slickEvolutions,
  specs2 % Test,
  scalaTest,
  scalaCheck,
  scalaCheckShapeless
)