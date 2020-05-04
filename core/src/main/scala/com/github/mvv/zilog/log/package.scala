package com.github.mvv.zilog

import com.github.mvv.zilog.impl.LoggingMacros
import zio.{Cause, Has, URIO, ZIO, ZTrace}

import scala.language.experimental.macros

package object log {
  final def fatal(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro LoggingMacros.envLogFatal
  final def fatal(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogFatalWithThrowable
  final def fatal(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogFatalWithCause
  final def fatal(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogFatalWithTrace
  final def fatal(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogFatalWithErrorTrace
  final def error(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro LoggingMacros.envLogError
  final def error(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogErrorWithThrowable
  final def error(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogErrorWithCause
  final def error(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogErrorWithTrace
  final def error(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogErrorWithErrorTrace
  final def warn(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro LoggingMacros.envLogWarn
  final def warn(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogWarnWithThrowable
  final def warn(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogWarnWithCause
  final def warn(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogWarnWithTrace
  final def warn(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogWarnWithErrorTrace
  final def info(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro LoggingMacros.envLogInfo
  final def info(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogInfoWithThrowable
  final def info(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogInfoWithCause
  final def info(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogInfoWithTrace
  final def info(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogInfoWithErrorTrace
  final def debug(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro LoggingMacros.envLogDebug
  final def debug(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogDebugWithThrowable
  final def debug(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogDebugWithCause
  final def debug(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogDebugWithTrace
  final def debug(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogDebugWithErrorTrace
  final def trace(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro LoggingMacros.envLogTrace
  final def trace(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogTraceWithThrowable
  final def trace(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogTraceWithCause
  final def trace(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogTraceWithTrace
  final def trace(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro LoggingMacros.envLogTraceWithErrorTrace

  def mapLogMessage[R <: Has[_] with Logging, E, A](
      f: String => String
  )(zio: => ZIO[R, E, A]): ZIO[R, E, A] =
    zio.provideSomeLayer[R](Logging.mapMessage(f))
  def withLogPrefix[R <: Has[_] with Logging, E, A](
      prefix: String
  )(zio: => ZIO[R, E, A]): ZIO[R, E, A] =
    mapLogMessage(message => s"$prefix$message")(zio)
  def mapLogArgs[R <: Has[_] with Logging, E, A](
      f: Logging.Args => Logging.Args
  )(zio: => ZIO[R, E, A]): ZIO[R, E, A] =
    zio.provideSomeLayer[R](Logging.mapArgs(f))
  def withLogArgs[R <: Has[_] with Logging, E, A](
      args: Logging.Args*
  )(zio: => ZIO[R, E, A]): ZIO[R, E, A] = {
    val combined = args.foldLeft(Logging.NoArgs)(_ ++ _)
    mapLogArgs(combined ++ _)(zio)
  }
  def withMinLogLevel[R <: Has[_] with Logging, E, A](
      minLevel: Logging.Level
  )(zio: => ZIO[R, E, A]): ZIO[R, E, A] =
    zio.provideSomeLayer[R](Logging.withMinLevel(minLevel))
}
