name := "result.scala"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.13.8"

scalacOptions += "-Werror"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest-funspec" % "3.2.12" % Test,
  "org.scalatestplus" %% "scalacheck-1-16" % "3.2.12.0" % Test
)

doctestTestFramework := DoctestTestFramework.ScalaTest
doctestScalaTestVersion := Some("3.2.12")

Compile / doc / scalacOptions ++= Seq(
  "-groups"
)
