package com.github.mvv.zilog.test

import com.github.mvv.zilog
import com.github.mvv.zilog.{Level, Logger, LoggerContext}
import org.specs2.mutable.Specification
import zio.{DefaultRuntime, ZIO}

class LoggerSpec extends Specification with DefaultRuntime {
  "Logger" >> {
    "should log" >> {
      val logger = new TestLogger(Level.Debug)
      unsafeRunSync {
        implicit val ctx = new LoggerContext(logger)
        ZIO.provide(Logger.Default) {
          zilog.info(s"Start ${1 + 2}${8} and ${"bar"} end")
        }
      }
      logger.entries must containTheSameElementsAs(
        Seq(
          (Level.Info, "Start {}{} and {} end", List(3, 8, "bar"))
        ))
    }

    "should log errors" >> {
      val e = new RuntimeException
      val logger = new TestLogger(Level.Debug)
      unsafeRunSync {
        implicit val ctx = new LoggerContext(logger)
        ZIO.provide(Logger.Default) {
          zilog.error(e, s"${1} plus ${2}")
        }
      }
      logger.entries must containTheSameElementsAs(
        Seq(
          (Level.Error, "{} plus {}", List(1, 2, e))
        ))
    }
  }
}

object LoggerSpec {
  final val Const: String = "const"
}
