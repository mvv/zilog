package com.github.mvv

import com.github.mvv.zilog.impl.{AppendToMessageService, MdcService}
import zio.{Has, Layer, UIO, ULayer, URIO, ZIO, ZLayer}

import scala.language.experimental.macros

package object zilog {
  final type Logger = Has[Logger.Service]
  final type ImplicitArgsLogger = Has[ImplicitArgsLogger.Service]

  def log(level: Level, format: String, args: Array[Any])(
      implicit ctx: LoggerContext
  ): URIO[Logger, Unit] =
    ZIO.accessM[Logger](_.get.log(level, format, args))
  final def error(message: String): URIO[Logger, Unit] = macro LoggerMacro.logError
  final def error(e: Throwable, message: String): URIO[Logger, Unit] =
    macro LoggerMacro.logErrorWithThrowable
  final def warn(message: String): URIO[Logger, Unit] = macro LoggerMacro.logWarn
  final def warn(e: Throwable, message: String): URIO[Logger, Unit] =
    macro LoggerMacro.logWarnWithThrowable
  final def info(message: String): URIO[Logger, Unit] = macro LoggerMacro.logInfo
  final def info(e: Throwable, message: String): URIO[Logger, Unit] =
    macro LoggerMacro.logInfoWithThrowable
  final def debug(message: String): URIO[Logger, Unit] = macro LoggerMacro.logDebug
  final def debug(e: Throwable, message: String): URIO[Logger, Unit] =
    macro LoggerMacro.logDebugWithThrowable
  final def trace(message: String): URIO[Logger, Unit] = macro LoggerMacro.logTrace
  final def trace(e: Throwable, message: String): URIO[Logger, Unit] =
    macro LoggerMacro.logTraceWithThrowable

  object Logger {
    trait Service {
      def log(level: Level, format: String, args: Array[Any])(implicit ctx: LoggerContext): UIO[Unit]
      final def error(message: String): UIO[Unit] = macro LoggerMacro.logError
      final def error(e: Throwable, message: String): UIO[Unit] = macro LoggerMacro.logErrorWithThrowable
      final def warn(message: String): UIO[Unit] = macro LoggerMacro.logWarn
      final def warn(e: Throwable, message: String): UIO[Unit] = macro LoggerMacro.logWarnWithThrowable
      final def info(message: String): UIO[Unit] = macro LoggerMacro.logInfo
      final def info(e: Throwable, message: String): UIO[Unit] = macro LoggerMacro.logInfoWithThrowable
      final def debug(message: String): UIO[Unit] = macro LoggerMacro.logDebug
      final def debug(e: Throwable, message: String): UIO[Unit] = macro LoggerMacro.logDebugWithThrowable
      final def trace(message: String): UIO[Unit] = macro LoggerMacro.logTrace
      final def trace(e: Throwable, message: String): UIO[Unit] = macro LoggerMacro.logTraceWithThrowable
    }

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

  object ImplicitArgsLogger {
    trait Service extends Logger.Service {
      def withImplicitLogArgs(args: (String, Any)*): Service
    }

    object Service {
      val mdc: Service = new MdcService(Map.empty)
      def appendToMessage(service: Logger.Service): Service = new AppendToMessageService(service, Map.empty)
    }

    val any: ZLayer[ImplicitArgsLogger, Nothing, Has[ImplicitArgsLogger.Service]] =
      ZLayer.requires[ImplicitArgsLogger]
    val mdc: Layer[Nothing, ImplicitArgsLogger] = ZLayer.succeed(Service.mdc)
    val appendToMessage: ZLayer[Logger, Nothing, ImplicitArgsLogger] =
      ZLayer.requires[Logger].map(has => Has(Service.appendToMessage(has.get)))
  }
}
