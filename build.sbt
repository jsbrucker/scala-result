import com.github.tkawachi.doctest.DoctestPlugin.autoImport.doctestScalaTestVersion

name := "scala-result"
version := "0.1.0-SNAPSHOT"

lazy val scala213 = "2.13.10"
lazy val scala212 = "2.12.17"
scalaVersion := scala213

lazy val core = (projectMatrix in file("."))
  .settings(name := "core")
  .jvmPlatform(scalaVersions = Seq(scala213, scala212))
  .settings(
    Compile / doc / scalacOptions += "-groups",
    doctestTestFramework := DoctestTestFramework.ScalaTest,
    doctestScalaTestVersion := Some("3.2.12"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest-funspec" % "3.2.12" % Test,
      "org.scalatestplus" %% "scalacheck-1-16" % "3.2.12.0" % Test
    )
  )
