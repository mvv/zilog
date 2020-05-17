package com.github.mvv.zilog.sager

import com.github.mvv.sager.Record
import com.github.mvv.sager.zio.FoundService
import com.github.mvv.zilog.sager.impl.SagerLoggingMacros
import zio.{Cause, URIO, ZIO, ZTrace}

import scala.language.experimental.macros

package object log {
  def fatal(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro SagerLoggingMacros.fatal
  def fatal(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.fatalWithThrowable
  def fatal(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.fatalWithCause
  def fatal(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.fatalWithTrace
  def fatal(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.fatalWithErrorTrace
  def error(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro SagerLoggingMacros.error
  def error(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.errorWithThrowable
  def error(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.errorWithCause
  def error(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.errorWithTrace
  def error(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.errorWithErrorTrace
  def warn(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro SagerLoggingMacros.warn
  def warn(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.warnWithThrowable
  def warn(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.warnWithCause
  def warn(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.warnWithTrace
  def warn(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.warnWithErrorTrace
  def info(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro SagerLoggingMacros.info
  def info(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.infoWithThrowable
  def info(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.infoWithCause
  def info(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.infoWithTrace
  def info(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.infoWithErrorTrace
  def debug(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro SagerLoggingMacros.debug
  def debug(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.debugWithThrowable
  def debug(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.debugWithCause
  def debug(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.debugWithTrace
  def debug(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.debugWithErrorTrace
  def trace(message: String, args: Logging.Args*): URIO[Logging, Unit] = macro SagerLoggingMacros.trace
  def trace(error: Throwable, message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.traceWithThrowable
  def trace(cause: Cause[Any], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.traceWithCause
  def trace(trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.traceWithTrace
  def trace(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): URIO[Logging, Unit] =
    macro SagerLoggingMacros.traceWithErrorTrace

  def mapLogMessage[R <: Record, E, A](
      f: String => String
  )(zio: => ZIO[R, E, A])(implicit found: FoundService[Logging.Service, R]): ZIO[R, E, A] =
    zio.provideSome[R](_.updateMono[Logging.Service](Logging.Service.withMessage(_: Logging.Service)(f)))
  def withLogPrefix[R <: Record, E, A](
      prefix: String
  )(zio: => ZIO[R, E, A])(implicit found: FoundService[Logging.Service, R]): ZIO[R, E, A] =
    mapLogMessage(message => s"$prefix$message")(zio)
  def mapLogArgs[R <: Record, E, A](
      f: Logging.Args => Logging.Args
  )(zio: => ZIO[R, E, A])(implicit found: FoundService[Logging.Service, R]): ZIO[R, E, A] =
    zio.provideSome[R](_.updateMono[Logging.Service](Logging.Service.withArgs(_: Logging.Service)(f)))
  def withLogArgs[R <: Record, E, A](
      args: Logging.Args*
  )(zio: => ZIO[R, E, A])(implicit found: FoundService[Logging.Service, R]): ZIO[R, E, A] = {
    val combined = args.foldLeft(Logging.NoArgs)(_ ++ _)
    mapLogArgs(combined ++ _)(zio)
  }
  def withMinLogLevel[R <: Record, E, A](
      minLevel: Logging.Level
  )(zio: => ZIO[R, E, A])(implicit found: FoundService[Logging.Service, R]): ZIO[R, E, A] =
    zio.provideSome[R](_.updateMono[Logging.Service](Logging.Service.withMinLevel(_, minLevel)))
}
