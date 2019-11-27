package com.github.mvv.zilog

import zio.ZIO

import scala.language.experimental.macros

trait Logger {
  def logger: Logger.Service[Any]
}

object Logger {
  trait Service[-R] {
    def log(level: Level, format: String, args: Array[Any])(implicit ctx: LoggerContext): ZIO[R, Nothing, Unit]
    final def error(message: String): ZIO[R, Nothing, Unit] = macro LoggerMacro.logError
    final def error(e: Throwable, message: String): ZIO[R, Nothing, Unit] = macro LoggerMacro.logErrorWithThrowable
    final def warn(message: String): ZIO[R, Nothing, Unit] = macro LoggerMacro.logWarn
    final def warn(e: Throwable, message: String): ZIO[R, Nothing, Unit] = macro LoggerMacro.logWarnWithThrowable
    final def info(message: String): ZIO[R, Nothing, Unit] = macro LoggerMacro.logInfo
    final def info(e: Throwable, message: String): ZIO[R, Nothing, Unit] = macro LoggerMacro.logInfoWithThrowable
    final def debug(message: String): ZIO[R, Nothing, Unit] = macro LoggerMacro.logDebug
    final def debug(e: Throwable, message: String): ZIO[R, Nothing, Unit] = macro LoggerMacro.logDebugWithThrowable
    final def trace(message: String): ZIO[R, Nothing, Unit] = macro LoggerMacro.logTrace
    final def trace(e: Throwable, message: String): ZIO[R, Nothing, Unit] = macro LoggerMacro.logTraceWithThrowable
  }

  object Default extends Logger {
    override val logger: Service[Any] = new Service[Any] {
      override def log(level: Level, format: String, args: Array[Any])(
          implicit ctx: LoggerContext
      ): ZIO[Any, Nothing, Unit] =
        ZIO.effectTotal(ctx.log(level, format, args))
    }
  }

  val NoArgs: Array[Any] = Array.empty[Any]
}
