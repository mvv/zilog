package com.github.mvv.zilog

import com.github.mvv.sredded.{StructValue, Structured}
import com.github.mvv.zilog.impl.{
  LoggingMacros,
  MapArgsLoggingService,
  MapMessageLoggingService,
  NopLoggingService,
  StructuredLoggingService,
  TextLoggingService,
  WithMinLevelLoggingService
}
import com.github.mvv.zilog.structured.StructuredLayout
import zio.{Cause, UIO, ULayer, URLayer, ZLayer, ZTrace}
import zio.clock.Clock
import zio.console.Console

import scala.language.experimental.macros

object Logging {
  sealed trait Level {
    protected val code: Int
  }
  object Level {
    implicit val levelOrdering: Ordering[Level] = { (x: Level, y: Level) => x.code.compare(y.code) }
  }

  case object Fatal extends Level {
    override protected val code = 5
    override def toString: String = "FATAL"
  }
  case object Error extends Level {
    override protected val code = 4
    override def toString: String = "ERROR"
  }
  case object Warn extends Level {
    override protected val code = 3
    override def toString: String = "WARN"
  }
  case object Info extends Level {
    override protected val code = 2
    override def toString: String = "INFO"
  }
  case object Debug extends Level {
    override protected val code = 1
    override def toString: String = "DEBUG"
  }
  case object Trace extends Level {
    override protected val code = 0
    override def toString: String = "TRACE"
  }

  sealed trait Args {
    def isEmpty: Boolean
    def nonEmpty: Boolean
    def structured: Iterable[(String, StructValue)]
    def -[A](key: Key[A]): Args
    def ++(args: Args): Args
    def apply[A](key: Key[A]): Option[A]
  }
  object Args {
    private[Logging] object Empty extends Args {
      override def isEmpty: Boolean = true
      override def nonEmpty: Boolean = false
      override def structured: Iterable[(String, StructValue)] = Iterable.empty
      override def -[A](key: Key[A]): Args = this
      override def ++(args: Args): Args = args
      override def apply[A](key: Key[A]): Option[A] = None
      override def toString: String = "[]"
    }
  }

  val NoArgs: Args = Args.Empty

  class Key[A](final val name: String)(implicit val structured: Structured[A]) {
    final override def hashCode: Int = super.hashCode
    final override def equals(that: Any): Boolean = super.equals(that)
    final def apply(value: A): Args = new Key.One[A](this, value)
    final def unapply(args: Args): Option[A] = args(this)
  }

  object Key {
    final private class Many(val map: Map[Key[_], One[_]]) extends Args { self =>
      override def isEmpty: Boolean = false
      override def nonEmpty: Boolean = true
      override def structured: Iterable[(String, StructValue)] =
        new Iterable[(String, StructValue)] {
          override def iterator: Iterator[(String, StructValue)] =
            self.map.values.iterator.map { arg =>
              arg.key.name -> arg.structValue
            }
          override def size: Int = self.map.size
        }
      override def -[A](key: Key[A]): Args = {
        val map1 = map - key
        map.size match {
          case 0 => NoArgs
          case 1 => map.values.head
          case _ => new Many(map1)
        }
      }
      override def ++(args: Args): Args =
        args match {
          case Args.Empty  => this
          case one: One[_] => new Many(map.updated(one.key, one))
          case many: Many  => new Many(map ++ many.map)
        }
      override def apply[A](key: Key[A]): Option[A] = map.get(key).map(_.value.asInstanceOf[A])
      override def toString: String = s"[${map.valuesIterator.mkString(",")}]"
    }

    final private class One[A](val key: Key[A], val value: A) extends Args { self =>
      override def isEmpty: Boolean = false
      override def nonEmpty: Boolean = true
      def structValue: StructValue = key.structured(value)
      override def structured: Iterable[(String, StructValue)] = Seq(key.name -> structValue)
      override def -[B](key: Key[B]): Args = if (self.key eq key) NoArgs else this
      override def ++(args: Args): Args =
        args match {
          case Args.Empty  => this
          case one: One[_] => if (one.key == key) one else new Many(Map(key -> this, one.key -> one))
          case many: Many  => new Many(Map[Key[_], One[_]](key -> this) ++ many.map)
        }
      override def apply[B](key: Key[B]): Option[B] = if (self.key eq key) Some(value.asInstanceOf[B]) else None
      override def toString: String = s"[${key.name}=$value]"
    }

    def apply[A: Structured](name: String): Key[A] = new Key[A](name)
  }

  sealed trait StackTrace {
    def isEmpty: Boolean
    def nonEmpty: Boolean
  }
  case object NoStackTrace extends StackTrace {
    override def isEmpty: Boolean = true
    override def nonEmpty: Boolean = false
  }
  sealed trait SomeStackTrace extends StackTrace {
    final override def isEmpty: Boolean = false
    final override def nonEmpty: Boolean = true
    def prettyPrint: String
  }
  final case class ThrowableStackTrace(error: Throwable) extends SomeStackTrace {
    override def prettyPrint: String = Cause.fail(error).prettyPrint
  }
  final case class CauseStackTrace(cause: Cause[Any]) extends SomeStackTrace {
    override def prettyPrint: String = cause.prettyPrint
  }
  final case class ZTraceStackTrace(trace: ZTrace, error: Option[Throwable]) extends SomeStackTrace {
    override def prettyPrint: String =
      error.map(e => Cause.traced(Cause.fail(e), trace).prettyPrint).getOrElse(trace.prettyPrint)
  }

  trait Service {
    type ResolvedLogger
    def resolveLogger(level: Level)(implicit logger: Logger): UIO[Option[ResolvedLogger]]
    def log(logger: ResolvedLogger,
            level: Level,
            message: String,
            structuredArgs: Logging.Args,
            stackTrace: StackTrace,
            sourceFile: String,
            sourceClass: String,
            sourceMethod: String,
            sourceLine: Int): UIO[Unit]
    final def fatal(message: String, args: Logging.Args*): UIO[Unit] = macro LoggingMacros.prefixLogFatal
    final def fatal(error: Throwable, message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogFatalWithThrowable
    final def fatal(cause: Cause[Any], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogFatalWithCause
    final def fatal(trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogFatalWithTrace
    final def fatal(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogFatalWithErrorTrace
    final def error(message: String, args: Logging.Args*): UIO[Unit] = macro LoggingMacros.prefixLogError
    final def error(error: Throwable, message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogErrorWithThrowable
    final def error(cause: Cause[Any], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogErrorWithCause
    final def error(trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogErrorWithTrace
    final def error(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogErrorWithErrorTrace
    final def warn(message: String, args: Logging.Args*): UIO[Unit] = macro LoggingMacros.prefixLogWarn
    final def warn(error: Throwable, message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogWarnWithThrowable
    final def warn(cause: Cause[Any], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogWarnWithCause
    final def warn(trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogWarnWithTrace
    final def warn(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogWarnWithErrorTrace
    final def info(message: String, args: Logging.Args*): UIO[Unit] = macro LoggingMacros.prefixLogInfo
    final def info(error: Throwable, message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogInfoWithThrowable
    final def info(cause: Cause[Any], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogInfoWithCause
    final def info(trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogInfoWithTrace
    final def info(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogInfoWithErrorTrace
    final def debug(message: String, args: Logging.Args*): UIO[Unit] = macro LoggingMacros.prefixLogDebug
    final def debug(error: Throwable, message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogDebugWithThrowable
    final def debug(cause: Cause[Any], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogDebugWithCause
    final def debug(trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogDebugWithTrace
    final def debug(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogDebugWithErrorTrace
    final def trace(message: String, args: Logging.Args*): UIO[Unit] = macro LoggingMacros.prefixLogTrace
    final def trace(error: Throwable, message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogTraceWithThrowable
    final def trace(cause: Cause[Any], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogTraceWithCause
    final def trace(trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogTraceWithTrace
    final def trace(error: Any, trace: Option[ZTrace], message: String, args: Logging.Args*): UIO[Unit] =
      macro LoggingMacros.prefixLogTraceWithErrorTrace
  }

  object Service {
    val nop: Service = NopLoggingService
    def text(clock: Clock.Service)(logEntry: String => UIO[Unit]): Service = new TextLoggingService(clock, logEntry)
    def structured(clock: Clock.Service, layout: StructuredLayout)(
        logEntry: StructValue.Mapping => UIO[Unit]): Service =
      new StructuredLoggingService(clock = clock, layout, logEntry = logEntry)
    def withMessage(service: Service)(f: String => String): Service = new MapMessageLoggingService(service, f)
    def withArgs(service: Service)(f: Logging.Args => Logging.Args): Service = new MapArgsLoggingService(service, f)
    def withMinLevel(service: Service, minLevel: Level): Service = new WithMinLevelLoggingService(service, minLevel)
  }

  val any: URLayer[Logging, Logging] = ZLayer.requires[Logging]
  val nop: ULayer[Logging] = ZLayer.succeed(Service.nop)
  def text(f: String => UIO[Unit]): URLayer[Clock, Logging] =
    ZLayer.fromService[Clock.Service, Service](Service.text(_)(f))
  val consoleText: URLayer[Console with Clock, Logging] =
    ZLayer.fromServices[Console.Service, Clock.Service, Service] { (console, clock) =>
      Service.text(clock)(console.putStrLn)
    }
  def structured(layout: StructuredLayout)(f: StructValue.Mapping => UIO[Unit]): URLayer[Clock, Logging] =
    ZLayer.fromService[Clock.Service, Service](Service.structured(_, layout)(f))
  def consoleJson(layout: StructuredLayout = StructuredLayout.Default): URLayer[Console with Clock, Logging] =
    ZLayer.fromServices[Console.Service, Clock.Service, Service] { (console, clock) =>
      import com.github.mvv.sredded.json._
      Service.structured(clock, layout)(entry => console.putStrLn(entry.asJsonString))
    }
  def mapMessage(f: String => String): URLayer[Logging, Logging] =
    ZLayer.fromService[Service, Service](Service.withMessage(_)(f))
  def mapArgs(f: Logging.Args => Logging.Args): URLayer[Logging, Logging] =
    ZLayer.fromService[Service, Service](Service.withArgs(_)(f))
  def withMinLevel(minLevel: Level): URLayer[Logging, Logging] =
    ZLayer.fromService[Service, Service](Service.withMinLevel(_, minLevel))
}
