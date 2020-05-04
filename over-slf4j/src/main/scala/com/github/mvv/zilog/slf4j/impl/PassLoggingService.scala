package com.github.mvv.zilog.slf4j.impl

import com.github.mvv.zilog.Logging
import com.github.mvv.zilog.impl.StackTraceUtils
import zio.UIO

object PassLoggingService extends Slf4jLoggingService {
  import Slf4jLoggingService._

  override def log(logger: ResolvedLogger,
                   level: Logging.Level,
                   message: String,
                   structuredArgs: Logging.Args,
                   stackTrace: Logging.StackTrace,
                   sourceFile: String,
                   sourceClass: String,
                   sourceMethod: String,
                   sourceLine: Int): UIO[Unit] = {
    val escapedMessage = escapeMessage(message)
    val error = StackTraceUtils.stackTraceError(stackTrace)
    UIO.effectTotal {
      level match {
        case Logging.Fatal | Logging.Error =>
          error match {
            case None    => logger.error(escapedMessage, structuredArgs)
            case Some(e) => logger.error(escapedMessage, structuredArgs, e: Any)
          }
        case Logging.Warn =>
          error match {
            case None    => logger.warn(escapedMessage, structuredArgs)
            case Some(e) => logger.warn(escapedMessage, structuredArgs, e: Any)
          }
        case Logging.Info =>
          error match {
            case None    => logger.info(escapedMessage, structuredArgs)
            case Some(e) => logger.info(escapedMessage, structuredArgs, e: Any)
          }
        case Logging.Debug =>
          error match {
            case None    => logger.debug(escapedMessage, structuredArgs)
            case Some(e) => logger.debug(escapedMessage, structuredArgs, e: Any)
          }
        case Logging.Trace =>
          error match {
            case None    => logger.trace(escapedMessage, structuredArgs)
            case Some(e) => logger.trace(escapedMessage, structuredArgs, e: Any)
          }
      }
    }
  }
}
