package com.github.mvv.zilog.test

import com.github.mvv.zilog.Level
import org.slf4j.Marker

class TestLogger(minLevel: Level) extends org.slf4j.Logger {
  import Ordering.Implicits._

  private var entriesVar = Vector.empty[(Level, String, List[Any])]

  override def getName: String = ???

  override def isErrorEnabled: Boolean = minLevel <= Level.Error
  override def isErrorEnabled(marker: Marker): Boolean = ???
  override def error(format: String, arg1: Any, arg2: Any): Unit = log(Level.Error, format, arg1, arg2)
  override def error(format: String, arg: Any): Unit = log(Level.Error, format, arg)
  override def error(format: String, arguments: AnyRef*): Unit = log(Level.Error, format, arguments: _*)
  override def error(msg: String): Unit = log(Level.Error, msg)
  override def error(msg: String, t: Throwable): Unit = log(Level.Error, msg, t)
  override def error(marker: Marker, format: String, arg1: Any, arg2: Any): Unit = ???
  override def error(marker: Marker, format: String, arg: Any): Unit = ???
  override def error(marker: Marker, format: String, arguments: AnyRef*): Unit = ???
  override def error(marker: Marker, msg: String): Unit = ???
  override def error(marker: Marker, msg: String, t: Throwable): Unit = ???

  override def isWarnEnabled: Boolean = minLevel <= Level.Warn
  override def isWarnEnabled(marker: Marker): Boolean = ???
  override def warn(format: String, arg1: Any, arg2: Any): Unit = log(Level.Warn, format, arg1, arg2)
  override def warn(format: String, arg: Any): Unit = log(Level.Warn, format, arg)
  override def warn(format: String, arguments: AnyRef*): Unit = log(Level.Warn, format, arguments: _*)
  override def warn(msg: String): Unit = log(Level.Warn, msg)
  override def warn(msg: String, t: Throwable): Unit = log(Level.Warn, msg, t)
  override def warn(marker: Marker, format: String, arg1: Any, arg2: Any): Unit = ???
  override def warn(marker: Marker, format: String, arg: Any): Unit = ???
  override def warn(marker: Marker, format: String, arguments: AnyRef*): Unit = ???
  override def warn(marker: Marker, msg: String): Unit = ???
  override def warn(marker: Marker, msg: String, t: Throwable): Unit = ???

  override def isInfoEnabled: Boolean = minLevel <= Level.Info
  override def isInfoEnabled(marker: Marker): Boolean = ???
  override def info(format: String, arg1: Any, arg2: Any): Unit = log(Level.Info, format, arg1, arg2)
  override def info(format: String, arg: Any): Unit = log(Level.Info, format, arg)
  override def info(format: String, arguments: AnyRef*): Unit = log(Level.Info, format, arguments: _*)
  override def info(msg: String): Unit = log(Level.Info, msg)
  override def info(msg: String, t: Throwable): Unit = log(Level.Info, msg, t)
  override def info(marker: Marker, format: String, arg1: Any, arg2: Any): Unit = ???
  override def info(marker: Marker, format: String, arg: Any): Unit = ???
  override def info(marker: Marker, format: String, arguments: AnyRef*): Unit = ???
  override def info(marker: Marker, msg: String): Unit = ???
  override def info(marker: Marker, msg: String, t: Throwable): Unit = ???

  override def isDebugEnabled: Boolean = minLevel <= Level.Debug
  override def isDebugEnabled(marker: Marker): Boolean = ???
  override def debug(format: String, arg1: Any, arg2: Any): Unit = log(Level.Debug, format, arg1, arg2)
  override def debug(format: String, arg: Any): Unit = log(Level.Debug, format, arg)
  override def debug(format: String, arguments: AnyRef*): Unit = log(Level.Debug, format, arguments: _*)
  override def debug(msg: String): Unit = log(Level.Debug, msg)
  override def debug(msg: String, t: Throwable): Unit = log(Level.Debug, msg, t)
  override def debug(marker: Marker, format: String, arg1: Any, arg2: Any): Unit = ???
  override def debug(marker: Marker, format: String, arg: Any): Unit = ???
  override def debug(marker: Marker, format: String, arguments: AnyRef*): Unit = ???
  override def debug(marker: Marker, msg: String): Unit = ???
  override def debug(marker: Marker, msg: String, t: Throwable): Unit = ???

  override def isTraceEnabled: Boolean = minLevel <= Level.Trace
  override def isTraceEnabled(marker: Marker): Boolean = ???
  override def trace(format: String, arg1: Any, arg2: Any): Unit = log(Level.Trace, format, arg1, arg2)
  override def trace(format: String, arg: Any): Unit = log(Level.Trace, format, arg)
  override def trace(format: String, arguments: AnyRef*): Unit = log(Level.Trace, format, arguments: _*)
  override def trace(msg: String): Unit = log(Level.Trace, msg)
  override def trace(msg: String, t: Throwable): Unit = log(Level.Trace, msg, t)
  override def trace(marker: Marker, format: String, arg1: Any, arg2: Any): Unit = ???
  override def trace(marker: Marker, format: String, arg: Any): Unit = ???
  override def trace(marker: Marker, format: String, arguments: AnyRef*): Unit = ???
  override def trace(marker: Marker, msg: String): Unit = ???
  override def trace(marker: Marker, msg: String, t: Throwable): Unit = ???

  private def log(level: Level, format: String, args: Any*): Unit =
    entriesVar :+= (level, format, args.toList)
  def entries: Seq[(Level, String, List[Any])] = entriesVar
}
