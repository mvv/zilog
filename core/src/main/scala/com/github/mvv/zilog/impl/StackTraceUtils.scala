package com.github.mvv.zilog.impl

import com.github.mvv.zilog.Logging
import zio.{Cause, ZTrace}
import zio.internal.stacktracer.ZTraceElement

object StackTraceUtils {
  private def toThrowable(error: Any): Option[Throwable] =
    error match {
      case t: Throwable => Some(t)
      case _            => None
    }

  final private class CauseThrowable(cause: Cause[Any]) extends Throwable {
    private lazy val (message, error, trace) = {
      cause.find {
        case Cause.Traced(Cause.Die(e), trace)        => (s"Died: ${e.getMessage}", None, Some(trace))
        case Cause.Traced(Cause.Fail(e), trace)       => (s"Failed: $e", toThrowable(e), Some(trace))
        case Cause.Traced(Cause.Interrupt(by), trace) => (s"Interrupted by $by", None, Some(trace))
        case Cause.Die(e)                             => (s"Died: ${e.getMessage}", None, None)
        case Cause.Fail(e)                            => (s"Failed: $e", toThrowable(e), None)
        case Cause.Interrupt(by)                      => (s"Interrupted by $by", None, None)
      }.get
    }

    override def getCause: Throwable =
      trace.flatMap(_.parentTrace).map(ZTraceThrowable.spawnedFrom(_, error)).orElse(error).orNull
    override def getMessage: String = message

    override def getStackTrace: Array[StackTraceElement] =
      trace
        .map(_.stackTrace)
        .getOrElse(Nil)
        .iterator
        .map {
          case elem: ZTraceElement.SourceLocation => elem.toStackTraceElement
          case _: ZTraceElement.NoLocation        => new StackTraceElement("<none>", "<none>", "<none>", 0)
        }
        .toArray
  }

  final private class ZTraceThrowable(message: String, trace: ZTrace, cause: Option[Throwable]) extends Throwable {
    override def getCause: Throwable = trace.parentTrace.map(ZTraceThrowable.spawnedFrom(_, cause)).orElse(cause).orNull
    override def getMessage: String = message

    override def getStackTrace: Array[StackTraceElement] =
      trace.stackTrace.iterator.map {
        case elem: ZTraceElement.SourceLocation => elem.toStackTraceElement
        case _: ZTraceElement.NoLocation        => new StackTraceElement("<none>", "<none>", "<none>", 0)
      }.toArray
  }
  private object ZTraceThrowable {
    def trace(trace: ZTrace, cause: Option[Throwable]): ZTraceThrowable =
      new ZTraceThrowable("Trace", trace, cause)
    def spawnedFrom(trace: ZTrace, cause: Option[Throwable]): ZTraceThrowable =
      new ZTraceThrowable("Spawned from", trace, cause)
  }

  def stackTraceError(stackTrace: Logging.StackTrace): Option[Throwable] =
    stackTrace match {
      case Logging.NoStackTrace                   => None
      case Logging.ThrowableStackTrace(error)     => Some(error)
      case Logging.CauseStackTrace(cause)         => Some(new CauseThrowable(cause))
      case Logging.ZTraceStackTrace(trace, error) => Some(ZTraceThrowable.trace(trace, error))
    }
}
