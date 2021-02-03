name := "akka-parent-npe"

version := "0.1"

lazy val scala212 = "2.12.11"
lazy val scala213 = "2.13.4"
lazy val supportedScalaVersions = List(scala212, scala213)

ThisBuild / scalaVersion := scala212

crossScalaVersions := supportedScalaVersions

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.4"
)