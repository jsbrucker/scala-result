name := "result"
ThisBuild / organization := "dev.jsbrucker"
ThisBuild / version := "1.0.0"

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
    moduleName := "result-core",
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

// Publishing Configuration

ThisBuild / organizationName := "jsbrucker"
ThisBuild / organizationHomepage := Some(url("https://jsbrucker.dev/scala-result"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/jsbrucker/scala-result"),
    "scm:git@github.com:jsbrucker/scala-result.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "jsbrucker",
    name = "John Brucker",
    email = "john+scala-result@jsbrucker.dev",
    url = url("https://jsbrucker.dev")
  )
)

ThisBuild / description := "Rust style Result type for Scala"
ThisBuild / licenses := List(
  "MIT" -> new URL("http://opensource.org/licenses/MIT")
)
ThisBuild / homepage := Some(url("https://jsbrucker.dev/scala-result"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  // For accounts created after Feb 2021:
  // val nexus = "https://s01.oss.sonatype.org/"
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

