package com.github.mvv.zilog.impl

import com.github.mvv.zilog.{Logger, Logging}
import zio.UIO

object NopLoggingService extends Logging.Service {
  override type ResolvedLogger = Unit
  override def resolveLogger(level: Logging.Level)(implicit logger: Logger): UIO[Option[ResolvedLogger]] = UIO.none
  override def log(logger: ResolvedLogger,
                   level: Logging.Level,
                   message: String,
                   structuredArgs: Logging.Args,
                   stackTrace: Logging.StackTrace,
                   sourceFile: String,
                   sourceClass: String,
                   sourceMethod: String,
                   sourceLine: Int): UIO[Unit] = UIO.unit
}
