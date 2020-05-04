package com.github.mvv.zilog.impl

import com.github.mvv.zilog.{Logger, Logging}
import zio.UIO

final class MapArgsLoggingService(val service: Logging.Service, f: Logging.Args => Logging.Args)
    extends Logging.Service {
  override type ResolvedLogger = service.ResolvedLogger
  override def resolveLogger(level: Logging.Level)(implicit logger: Logger): UIO[Option[ResolvedLogger]] =
    service.resolveLogger(level)
  override def log(logger: ResolvedLogger,
                   level: Logging.Level,
                   message: String,
                   structuredArgs: Logging.Args,
                   stackTrace: Logging.StackTrace,
                   sourceFile: String,
                   sourceClass: String,
                   sourceMethod: String,
                   sourceLine: Int): UIO[Unit] =
    service.log(logger,
                level,
                message,
                f(structuredArgs),
                stackTrace,
                sourceFile,
                sourceClass,
                sourceMethod,
                sourceLine)
}
