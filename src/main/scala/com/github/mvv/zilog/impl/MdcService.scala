package com.github.mvv.zilog.impl

import com.github.mvv.zilog.ImplicitArgsLogger.Service
import com.github.mvv.zilog.{Level, LoggerContext}
import org.slf4j.MDC
import zio.UIO

private[zilog] class MdcService(implicitArgs: Map[String, Any]) extends Service {
  override def withImplicitLogArgs(args: (String, Any)*): Service = new MdcService(implicitArgs ++ args)

  override def log(level: Level, format: String, args: Array[Any])(implicit ctx: LoggerContext): UIO[Unit] =
    UIO.effectTotal {
      val oldValues = implicitArgs.iterator.map {
        case (key, value) =>
          val oldValue = Option(MDC.get(key))
          MDC.put(key, value.toString)
          key -> oldValue
      }.toSeq
      ctx.log(level, format, args)
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
