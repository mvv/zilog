package com.github.mvv.zilog.impl

import java.util.concurrent.TimeUnit

import com.github.mvv.sredded.StructValue
import com.github.mvv.zilog.{Logger, Logging}
import zio.UIO
import zio.clock.Clock

final class TextLoggingService(clock: Clock.Service, logEntry: String => UIO[Unit]) extends Logging.Service {
  override type ResolvedLogger = String
  override def resolveLogger(level: Logging.Level)(implicit logger: Logger): UIO[Option[String]] = UIO.some(logger.name)
  override def log(logger: ResolvedLogger,
                   level: Logging.Level,
                   message: String,
                   structuredArgs: Logging.Args,
                   stackTrace: Logging.StackTrace,
                   sourceFile: String,
                   sourceClass: String,
                   sourceMethod: String,
                   sourceLine: Int): UIO[Unit] =
    clock.currentTime(TimeUnit.MILLISECONDS).flatMap { millis =>
      UIO.fiberId.flatMap { fiberId =>
        val builder = new java.lang.StringBuilder(if (stackTrace.isEmpty) 128 else 1024)
        builder
          .append(StructValue.Timestamp64(millis))
          .append(" [#")
          .append(fiberId.seqNumber)
          .append("] ")
          .append(level)
          .append(' ')
          .append(logger)
          .append(" - ")
          .append(message)
        if (structuredArgs.nonEmpty) {
          builder.append(" - ")
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
        }
        logEntry {
          stackTrace match {
            case Logging.NoStackTrace          => builder.toString
            case trace: Logging.SomeStackTrace => builder.append(trace.prettyPrint).toString
          }
        }
      }
    }
}
