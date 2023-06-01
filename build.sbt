name := "scala-result"
ThisBuild / organization := "dev.jsbrucker"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val scala213 = "2.13.10"
lazy val scala212 = "2.12.17"
lazy val supportedScalaVersions = List(scala212, scala213)
ThisBuild / scalaVersion := scala213

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val core = (project in file("core"))
  .settings(
    crossScalaVersions := supportedScalaVersions,
    Compile / doc / scalacOptions += "-groups",
    doctestTestFramework := DoctestTestFramework.ScalaTest,
    doctestScalaTestVersion := Some("3.2.12"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest-funspec" % "3.2.12" % Test,
      "org.scalatestplus" %% "scalacheck-1-16" % "3.2.12.0" % Test
    ),
    Compile / compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) => List("-Werror")
        // 2.12 has expected warnings for
        // * @implicitNotFound annotation usage
        case _             => Nil
      }
    },
  )

