package com.github.mvv.zilog

import org.slf4j.LoggerFactory

import scala.reflect.{classTag, ClassTag}

final class LoggerContext(val underlying: org.slf4j.Logger) extends AnyVal {
  def isLevelEnabled(level: Level): Boolean =
    level match {
      case Level.Error => underlying.isErrorEnabled
      case Level.Warn  => underlying.isWarnEnabled
      case Level.Info  => underlying.isInfoEnabled
      case Level.Debug => underlying.isDebugEnabled
      case Level.Trace => underlying.isTraceEnabled
    }
  def log(level: Level, format: String, args: Array[Any]): Unit =
    level match {
      case Level.Error => underlying.error(format, args.asInstanceOf[Array[AnyRef]]: _*)
      case Level.Warn  => underlying.warn(format, args.asInstanceOf[Array[AnyRef]]: _*)
      case Level.Info  => underlying.info(format, args.asInstanceOf[Array[AnyRef]]: _*)
      case Level.Debug => underlying.debug(format, args.asInstanceOf[Array[AnyRef]]: _*)
      case Level.Trace => underlying.trace(format, args.asInstanceOf[Array[AnyRef]]: _*)
    }
}

object LoggerContext {
  def apply(name: String): LoggerContext = new LoggerContext(LoggerFactory.getLogger(name))
  def apply[A: ClassTag]: LoggerContext = new LoggerContext(LoggerFactory.getLogger(classTag[A].runtimeClass))
}
