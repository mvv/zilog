import sbt._
import Keys._
import xerial.sbt.Sonatype._

inThisBuild(
  Seq(
    organization := "com.github.mvv.zilog",
    version := "0.1-M1",
    homepage := Some(url("https://github.com/mvv/zilog")),
    scmInfo := Some(ScmInfo(url("https://github.com/mvv/zilog"), "scm:git@github.com:mvv/zilog.git")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(id = "mvv",
                name = "Mikhail Vorozhtsov",
                email = "mikhail.vorozhtsov@gmail.com",
                url = url("https://github.com/mvv"))
    ),
    sonatypeProjectHosting := Some(GitHubHosting("mvv", "zilog", "mikhail.vorozhtsov@gmail.com"))
  )
)

ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / publishMavenStyle := true

lazy val sonatypeBundleReleaseIfNotSnapshot: Command = Command.command("sonatypeBundleReleaseIfNotSnapshot") { state =>
  val extracted = Project.extract(state)
  if (extracted.get(isSnapshot)) {
    val log = extracted.get(sLog)
    log.info("Snapshot version, doing nothing")
    state
  } else {
    Command.process("sonatypeBundleRelease", state)
  }
}

inThisBuild(
  Seq(
    crossScalaVersions := Seq("2.13.1", "2.12.10", "2.11.12"),
    scalaVersion := crossScalaVersions.value.head,
    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xfatal-warnings")
  )
)

def isPriorTo2_13(version: String): Boolean =
  CrossVersion.partialVersion(version) match {
    case Some((2, minor)) => minor < 13
    case _                => false
  }

lazy val zilog = (project in file("."))
  .settings(
    name := "zilog",
    sonatypeProfileName := "com.github.mvv",
    sonatypeSessionName := s"Zilog_${version.value}",
    commands += sonatypeBundleReleaseIfNotSnapshot,
    scalacOptions ++= {
      if (isPriorTo2_13(scalaVersion.value)) {
        Nil
      } else {
        Seq("-Ymacro-annotations")
      }
    },
    libraryDependencies ++=
      Seq(
        "dev.zio" %% "zio" % "1.0.0-RC16" % Provided,
        "org.slf4j" % "slf4j-api" % "1.7.29",
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        "org.specs2" %% "specs2-core" % "4.8.1" % Test
      ),
    libraryDependencies ++= {
      if (isPriorTo2_13(scalaVersion.value)) {
        Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
      } else {
        Nil
      }
    }
  )
