package com.github.mvv.zilog.impl

import java.util.concurrent.TimeUnit

import com.github.mvv.sredded.{StructValue, Structured}
import com.github.mvv.zilog.structured.StructuredLayout
import com.github.mvv.zilog.{Logger, Logging}
import zio.UIO
import zio.clock.Clock

final class StructuredLoggingService(clock: Clock.Service,
                                     layout: StructuredLayout,
                                     logEntry: StructValue.Mapping => UIO[Unit])
    extends Logging.Service {
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
        logEntry {
          layout.postProcess {
            StructValue
              .Mapping(
                Array(
                  layout.timestampMember -> StructValue.Timestamp64(millis),
                  layout.fiberIdMember -> Structured(s"#${fiberId.seqNumber}"),
                  layout.loggerMember -> Structured(logger),
                  layout.levelMember -> Structured(level.toString),
                  layout.messageMember -> Structured(message),
                  layout.argsMember -> StructValue.Mapping(structuredArgs.structured),
                  layout.stackTraceMember -> (stackTrace match {
                    case Logging.NoStackTrace          => StructValue.Null
                    case trace: Logging.SomeStackTrace => Structured(trace.prettyPrint)
                  }),
                  layout.sourceFileMember -> Structured(sourceFile),
                  layout.sourceClassMember -> Structured(sourceClass),
                  layout.sourceMethodMember -> Structured(sourceMethod),
                  layout.sourceLineMember -> Structured(sourceLine)
                )
              )
          }
        }
      }
    }
}
