package com.github.mvv.zilog.slf4j.impl

import com.github.mvv.sredded.StructValue
import com.github.mvv.zilog.Logging
import com.github.mvv.zilog.impl.StackTraceUtils
import zio.UIO

object AppendToMessageLoggingService extends Slf4jLoggingService {
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
    val escapedMessage = escapeMessage {
      if (structuredArgs.isEmpty) {
        message
      } else {
        val builder = new java.lang.StringBuilder
        builder.append(message).append(" - ")
        var first = true
        structuredArgs.structured.foreach {
          case (name, value) =>
            if (first) {
              first = false
            } else {
              builder.append(", ")
            }
            builder.append(StructValue.String(name)).append('=').append(value)
        }
        builder.toString
      }
    }
    val error = StackTraceUtils.stackTraceError(stackTrace)
    UIO.effectTotal {
      doLog(logger, level, escapedMessage, error)
    }
  }
}
