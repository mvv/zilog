package com.github.mvv.zilog.slf4j.impl

import com.github.mvv.sredded.StructValue
import com.github.mvv.zilog.Logging
import com.github.mvv.zilog.impl.StackTraceUtils
import org.slf4j.MDC
import zio.UIO

final class MdcLoggingService(nesting: Char) extends Slf4jLoggingService {
  import Slf4jLoggingService._

  private def flattenArgs(args: Iterator[(String, StructValue)], prefix: String): Iterator[(String, StructValue)] =
    args.flatMap {
      case (name, value) =>
        value match {
          case StructValue.Mapping(entries) =>
            flattenArgs(entries.iterator, s"$prefix$name$nesting")
          case _ =>
            Iterator(s"$prefix$name" -> value)
        }
    }

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
      val oldValues = flattenArgs(structuredArgs.structured.iterator, "").map {
        case (key, value) =>
          val oldValue = Option(MDC.get(key))
          MDC.put(key,
                  value match {
                    case StructValue.String(str) => str
                    case _                       => value.toString
                  })
          key -> oldValue
      }.toList
      doLog(logger, level, escapedMessage, error)
      oldValues.foreach {
        case (key, oldValue) =>
          oldValue match {
            case Some(value) =>
              MDC.put(key, value)
            case None =>
              MDC.remove(key)
          }
      }
    }
  }
}
