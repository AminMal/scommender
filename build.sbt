name := "recommender"

version := "0.1"

scalaVersion := "2.13.7"

idePackagePrefix := Some("ir.ac.usc")

import Dependencies._

lazy val root = (project in file("."))
  .settings(
    name := "recommender",
//    libraryDependencies += scalaTest % Test,
    libraryDependencies ++= Seq(
      sparkCore,
      sparkMLlib,
      AkkaActorsTyped,
      AkkaStreams,
      AkkaHttp
    )
  )