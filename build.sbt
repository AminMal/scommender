name := "recommender"

version := "0.1"

scalaVersion := "2.13.7"

idePackagePrefix := Some("ir.ac.usc")

import Dependencies._

lazy val root = (project in file("."))
  .settings(
    name := "recommender",
    libraryDependencies += ScalaTest,
    libraryDependencies ++= Seq(
      SparkCore,
      SparkMLlib,
      AkkaActorsTyped,
      AkkaStreams,
      AkkaHttp,
      SprayJson,
      AkkaTest,
      MockitoMock,
      AkkaStreamTestKit,
      AkkaHttpTestKit
    )
  )