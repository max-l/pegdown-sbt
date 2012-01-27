
import sbt._
import Keys._


object Buildz extends Build {

  def buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.strong-links",
    version := "0.1",
    scalaVersion := "2.9.1"
  )
  
  lazy val scalaforms = Project(
    id = "pegdown-sbt",
    base = file("."),    
    settings = buildSettings ++Seq(
	  libraryDependencies  ++=  Seq("org.pegdown" % "pegdown" % "1.1.0"),
	  sbtPlugin := true
	)
  )
}
