package com.github.mvv.zilog.test

import com.github.mvv.zilog
import com.github.mvv.zilog.{Level, Logger, LoggerContext}
import org.specs2.mutable.Specification
import zio.BootstrapRuntime

class LoggerSpec extends Specification with BootstrapRuntime {
  "Logger" >> {
    "should log" >> {
      val logger = new TestLogger(Level.Debug)
      unsafeRunSync {
        implicit val ctx = new LoggerContext(logger)
        zilog.info(s"Start ${1 + 2}${8} and ${"bar"} end").provideCustomLayer(Logger.live)
      }
      logger.entries.toList mustEqual List(
        (Level.Info, "Start {}{} and {} end", List(3, 8, "bar"))
      )
    }

    "should log messages without arguments" >> {
      val logger = new TestLogger(Level.Debug)
      unsafeRunSync {
        implicit val ctx = new LoggerContext(logger)
        zilog.warn("Message").provideCustomLayer(Logger.live)
      }
      logger.entries.toList mustEqual List(
        (Level.Warn, "Message", Nil)
      )
    }

    "should log errors" >> {
      val e = new RuntimeException
      val logger = new TestLogger(Level.Debug)
      unsafeRunSync {
        implicit val ctx = new LoggerContext(logger)
        zilog.error(e, s"${1} plus ${2}").provideCustomLayer(Logger.live)
      }
      logger.entries.toList mustEqual List(
        (Level.Error, "{} plus {}", List(1, 2, e))
      )
    }
  }
}

object LoggerSpec {
  final val Const: String = "const"
}
