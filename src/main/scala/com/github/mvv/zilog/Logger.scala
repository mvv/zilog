package com.github.mvv.zilog

import zio.{Has, UIO, ULayer, URIO, ZLayer}

import scala.language.experimental.macros

object Logger {
  trait Interface[-R] {
    def log(level: Level, format: String, args: Array[Any])(implicit ctx: LoggerContext): URIO[R, Unit]
    final def error(message: String): URIO[R, Unit] = macro LoggerMacro.logError
    final def error(e: Throwable, message: String): URIO[R, Unit] = macro LoggerMacro.logErrorWithThrowable
    final def warn(message: String): URIO[R, Unit] = macro LoggerMacro.logWarn
    final def warn(e: Throwable, message: String): URIO[R, Unit] = macro LoggerMacro.logWarnWithThrowable
    final def info(message: String): URIO[R, Unit] = macro LoggerMacro.logInfo
    final def info(e: Throwable, message: String): URIO[R, Unit] = macro LoggerMacro.logInfoWithThrowable
    final def debug(message: String): URIO[R, Unit] = macro LoggerMacro.logDebug
    final def debug(e: Throwable, message: String): URIO[R, Unit] = macro LoggerMacro.logDebugWithThrowable
    final def trace(message: String): URIO[R, Unit] = macro LoggerMacro.logTrace
    final def trace(e: Throwable, message: String): URIO[R, Unit] = macro LoggerMacro.logTraceWithThrowable
  }

  trait Service extends Interface[Any]

  object Service {
    val live: Service = new Service {
      override def log(level: Level, format: String, args: Array[Any])(implicit ctx: LoggerContext): UIO[Unit] =
        UIO.effectTotal(ctx.log(level, format, args))
    }
    def prefix(prefix: String, service: Service): Service = new Service {
      override def log(level: Level, format: String, args: Array[Any])(implicit ctx: LoggerContext): URIO[Any, Unit] =
        service.log(level, s"$prefix$format", args)
    }
  }

  val any: ZLayer[Logger, Nothing, Logger] = ZLayer.requires[Logger]
  val live: ULayer[Logger] = ZLayer.succeed(Service.live)
  def prefix(prefix: String): ZLayer[Logger, Nothing, Logger] =
    ZLayer.requires[Logger].map(has => Has(Service.prefix(prefix, has.get)))

  val NoArgs: Array[Any] = Array.empty[Any]
}
