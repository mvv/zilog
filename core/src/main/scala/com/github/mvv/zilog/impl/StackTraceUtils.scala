package com.github.mvv.zilog.impl

import com.github.mvv.zilog.Logging
import zio.{Cause, ZTrace}
import zio.internal.stacktracer.ZTraceElement

import java.io.PrintStream
import scala.annotation.tailrec

object StackTraceUtils {
  private def toThrowable(error: Any): Option[Throwable] =
    error match {
      case t: Throwable => Some(t)
      case _            => None
    }

  sealed private trait CustomPrintStackTraceThrowable extends Throwable {
    protected def stackTraceElements: Iterator[StackTraceElement]
    override def getStackTrace: Array[StackTraceElement] = stackTraceElements.toArray

    override def printStackTrace(printStream: PrintStream): Unit = {
      printStream.println(this)
      stackTraceElements.foreach { ste =>
        if (ste.getClassName.isEmpty) {
          printStream.println(ste.getMethodName)
        } else {
          printStream.print("\tat ")
          printStream.println(ste)
        }
      }
      CustomPrintStackTraceThrowable.printCauseStackTrace(getCause, Set(this), printStream)
    }
  }

  private object CustomPrintStackTraceThrowable {
    @tailrec
    private def printCauseStackTrace(cause: Throwable, seen: Set[Throwable], printStream: PrintStream): Unit = {
      if (cause == null) {
        return
      }
      if (seen.contains(cause)) {
        printStream.println(s"Caused by: [CIRCULAR REFERENCE: $cause]")
        return
      }
      printStream.print("Caused by: ")
      printStream.println(cause)
      cause.getStackTrace.foreach { ste =>
        printStream.print("\tat ")
        printStream.println(ste)
      }
      printCauseStackTrace(cause.getCause, seen + cause, printStream)
    }
  }

  final private class CauseThrowable(cause: Cause[Any]) extends CustomPrintStackTraceThrowable {
    private lazy val (message, error, trace) = {
      cause.find {
        case Cause.Traced(Cause.Die(e), trace)        => (s"Died with $e", Some(e), Some(trace))
        case Cause.Traced(Cause.Fail(e), trace)       => (s"Failed with $e", toThrowable(e), Some(trace))
        case Cause.Traced(Cause.Interrupt(by), trace) => (s"Interrupted by $by", None, Some(trace))
        case Cause.Die(e)                             => (s"Died with $e", Some(e), None)
        case Cause.Fail(e)                            => (s"Failed with $e", toThrowable(e), None)
        case Cause.Interrupt(by)                      => (s"Interrupted by $by", None, None)
      }.get
    }

    override def getCause: Throwable = error.orNull
    override def getMessage: String = message
    override def toString: String = message

    override protected def stackTraceElements: Iterator[StackTraceElement] =
      trace.map(traceElements).getOrElse(Iterator.empty)
  }

  final private class ZTraceThrowable(trace: ZTrace, cause: Option[Throwable]) extends CustomPrintStackTraceThrowable {
    override def getCause: Throwable = cause.orNull
    override def getMessage: String = "Stack trace"
    override def toString: String = "Stack trace"
    override protected def stackTraceElements: Iterator[StackTraceElement] = traceElements(trace)
  }

  private def traceElements(trace: ZTrace): Iterator[StackTraceElement] = {
    val elems = trace.stackTrace.iterator.map {
      case elem: ZTraceElement.SourceLocation => elem.toStackTraceElement
      case _: ZTraceElement.NoLocation        => new StackTraceElement("<none>", "<none>", null, -1)
    }
    trace.parentTrace match {
      case Some(parentTrace) =>
        elems ++ Iterator.single(
          new StackTraceElement("", s"Spawned by Fiber ${parentTrace.fiberId}", null, -1)) ++ traceElements(parentTrace)
      case None =>
        elems
    }
  }

  def stackTraceError(stackTrace: Logging.SomeStackTrace): Throwable =
    stackTrace match {
      case Logging.ThrowableStackTrace(error)     => error
      case Logging.CauseStackTrace(cause)         => new CauseThrowable(cause)
      case Logging.ZTraceStackTrace(trace, error) => new ZTraceThrowable(trace, error)
    }

  def stackTraceError(stackTrace: Logging.StackTrace): Option[Throwable] =
    stackTrace match {
      case Logging.NoStackTrace         => None
      case some: Logging.SomeStackTrace => Some(stackTraceError(some))
    }
}
