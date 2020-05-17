package com.github.mvv.zilog

import com.github.mvv.zilog.impl.EnvLoggingMacros
import zio.{Cause, Has, URIO, ZIO, ZTrace}

import scala.language.experimental.macros

package object log {
  def fatal(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro EnvLoggingMacros.fatal
  def fatal(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.fatalWithThrowable
  def fatal(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.fatalWithCause
  def fatal(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.fatalWithTrace
  def fatal(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.fatalWithErrorTrace
  def error(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro EnvLoggingMacros.error
  def error(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.errorWithThrowable
  def error(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.errorWithCause
  def error(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.errorWithTrace
  def error(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.errorWithErrorTrace
  def warn(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro EnvLoggingMacros.warn
  def warn(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.warnWithThrowable
  def warn(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.warnWithCause
  def warn(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.warnWithTrace
  def warn(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.warnWithErrorTrace
  def info(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro EnvLoggingMacros.info
  def info(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.infoWithThrowable
  def info(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.infoWithCause
  def info(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.infoWithTrace
  def info(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.infoWithErrorTrace
  def debug(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro EnvLoggingMacros.debug
  def debug(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.debugWithThrowable
  def debug(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.debugWithCause
  def debug(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.debugWithTrace
  def debug(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.debugWithErrorTrace
  def trace(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro EnvLoggingMacros.trace
  def trace(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.traceWithThrowable
  def trace(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.traceWithCause
  def trace(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.traceWithTrace
  def trace(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro EnvLoggingMacros.traceWithErrorTrace

  def mapLogMessage[R <: Logging, E, A](
      f: String => String
  )(zio: => ZIO[R, E, A]): ZIO[R, E, A] =
    zio.provideSomeLayer[R](Logging.mapMessage(f))
  def withLogPrefix[R <: Logging, E, A](
      prefix: String
  )(zio: => ZIO[R, E, A]): ZIO[R, E, A] =
    mapLogMessage(message => s"$prefix$message")(zio)
  def mapLogArgs[R <: Logging, E, A](
      f: Logging.Args => Logging.Args
  )(zio: => ZIO[R, E, A]): ZIO[R, E, A] =
    zio.provideSomeLayer[R](Logging.mapArgs(f))
  def withLogArgs[R <: Logging, E, A](
      args: Logging.Args*
  )(zio: => ZIO[R, E, A]): ZIO[R, E, A] = {
    val combined = args.foldLeft(Logging.NoArgs)(_ ++ _)
    mapLogArgs(combined ++ _)(zio)
  }
  def withMinLogLevel[R <: Logging, E, A](
      minLevel: Logging.Level
  )(zio: => ZIO[R, E, A]): ZIO[R, E, A] =
    zio.provideSomeLayer[R](Logging.withMinLevel(minLevel))
}
