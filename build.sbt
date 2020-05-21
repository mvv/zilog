import sbt._
import Keys._
import xerial.sbt.Sonatype._

inThisBuild(
  Seq(
    organization := "com.github.mvv.zilog",
    version := "0.1-M7", // next is M8
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
    crossScalaVersions := Seq("2.13.2", "2.12.11"),
    scalaVersion := crossScalaVersions.value.head,
    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xfatal-warnings")
  )
)

def isPriorTo2_13(version: String): Boolean =
  CrossVersion.partialVersion(version) match {
    case Some((2, minor)) => minor < 13
    case _                => false
  }

lazy val macroSettings: Seq[Def.SettingsDefinition] =
  Seq(
    scalacOptions ++= {
      if (isPriorTo2_13(scalaVersion.value)) {
        Nil
      } else {
        Seq("-Ymacro-annotations")
      }
    },
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
    libraryDependencies ++= {
      if (isPriorTo2_13(scalaVersion.value)) {
        Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
      } else {
        Nil
      }
    }
  )

val zioVersion = "1.0.0-RC19-2"
val zio = "dev.zio" %% "zio" % zioVersion

lazy val zioTestSettings: Seq[Def.SettingsDefinition] =
  Seq(
    libraryDependencies ++= Seq("dev.zio" %% "zio-test" % zioVersion % Test,
                                "dev.zio" %% "zio-test-sbt" % zioVersion % Test),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )

lazy val zilog = (project in file("."))
  .settings(
    crossScalaVersions := Nil,
    skip in publish := true,
    sonatypeProfileName := "com.github.mvv",
    sonatypeSessionName := s"Zilog_${version.value}",
    commands += sonatypeBundleReleaseIfNotSnapshot
  )
  .aggregate(core, sager, overSlf4j)

lazy val core = (project in file("core"))
  .settings(
    name := "zilog",
    description := "Structured logging library for ZIO",
    libraryDependencies ++=
      Seq(
        zio % Provided,
        "com.github.mvv.sredded" %% "sredded-json" % "0.1-M2"
      )
  )
  .settings(macroSettings: _*)
  .settings(zioTestSettings: _*)

lazy val sager = (project in file("sager"))
  .settings(
    name := "zilog-sager",
    description := "Using generic records to carry logging service in environment",
    libraryDependencies ++=
      Seq(zio % Provided, "com.github.mvv.sager" %% "sager-zio" % "0.1-M2")
  )
  .settings(macroSettings: _*)
  .settings(zioTestSettings: _*)
  .dependsOn(core)

lazy val overSlf4j = (project in file("over-slf4j"))
  .settings(
    name := "zilog-over-sfl4j",
    description := "SLF4J backend for Zilog",
    libraryDependencies ++= Seq(zio % Provided, "org.slf4j" % "slf4j-api" % "1.7.30")
  )
  .dependsOn(core)
