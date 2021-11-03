package com.github.mvv.zilog.test

import com.github.mvv.zilog.Logging
import com.github.mvv.zilog.impl.StackTraceUtils
import zio.Task
import zio.test.environment.TestEnvironment
import zio.test._
import zio.test.Assertion._

object StackTraceUtilsSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("StackTraceUtils")(
      testM("stackTraceError") {
        stackTraceErrorEntry()
          .foldCause(
            failure = { cause =>
              val e = StackTraceUtils.stackTraceError(Logging.CauseStackTrace(cause))
              Some(e)
            },
            success = { _ =>
              Option.empty[Throwable]
            }
          )
          .map {
            result =>
              assert(result.map(_.getStackTrace.toList).getOrElse(Nil)) {
                hasAt(0)(stackTraceElementMethodName(startsWithString("stackTraceErrorLevel2"))) &&
                hasAt(1)(stackTraceElementMethodName(startsWithString("stackTraceErrorLevel1"))) &&
                hasAt(2)(stackTraceElementMethodName(startsWithString("stackTraceErrorEntry")))
              } && assert(result.flatMap(e => Option(e.getCause)).map(_.getStackTrace.toList).getOrElse(Nil)) {
                hasAt(0)(stackTraceElementMethodName(startsWithString("stackTraceErrorEffectLevel1"))) &&
                hasAt(1)(stackTraceElementMethodName(startsWithString("stackTraceErrorEffectEntry"))) &&
                hasAt(2)(stackTraceElementMethodName(startsWithString("$anonfun$stackTraceErrorLevel2")))
              }
          }
      }
    )

  def stackTraceElementMethodName(assertion: Assertion[String]): Assertion[StackTraceElement] =
    assertionRec[StackTraceElement, String]("stackTraceElementMethodName")(Render.param(assertion))(assertion)(ste =>
      Some(ste.getMethodName))

  def stackTraceErrorEntry(): Task[Unit] =
    for {
      _ <- Task.succeed("entry_before")
      _ <- stackTraceErrorLevel1()
      _ <- Task.succeed("entry_after")
    } yield ()

  def stackTraceErrorLevel1(): Task[Unit] =
    for {
      _ <- Task.succeed("level1_before")
      _ <- stackTraceErrorLevel2()
      _ <- Task.succeed("level2_after")
    } yield ()

  def stackTraceErrorLevel2(): Task[Unit] =
    for {
      _ <- Task.succeed("level1_before")
      _ <- Task.effect(stackTraceErrorEffectEntry())
      _ <- Task.succeed("level2_after")
    } yield ()

  def stackTraceErrorEffectEntry(): Unit =
    stackTraceErrorEffectLevel1()

  def stackTraceErrorEffectLevel1(): Unit =
    throw new RuntimeException("failed")
}
