package com.github.mvv.zilog

import org.slf4j.MDC
import zio.{FiberRef, UIO, ZIO}

trait ImplicitArgsLogger extends Logger {
  override def logger: ImplicitArgsLogger.Service[Any]
}

object ImplicitArgsLogger {
  trait Service[-R] extends Logger.Service[R] {
    def withImplicitLogArgs[R1 <: R, E, A](args: (String, Any)*)(zio: ZIO[R1, E, A]): ZIO[R1, E, A]
  }

  sealed abstract private class FiberRefService[-R](fiberRef: FiberRef[Map[String, Any]]) extends Service[R] {
    final override def withImplicitLogArgs[R1 <: R, E, A](args: (String, Any)*)(zio: ZIO[R1, E, A]): ZIO[R1, E, A] =
      fiberRef.get.flatMap { current =>
        fiberRef.locally(current ++ args)(zio)
      }
    def log(level: Level, format: String, explicitArgs: Array[Any], implicitArgs: Map[String, Any])(
        implicit ctx: LoggerContext
    ): ZIO[R, Nothing, Unit]
    final override def log(level: Level, format: String, args: Array[Any])(
        implicit ctx: LoggerContext
    ): ZIO[R, Nothing, Unit] =
      fiberRef.get.flatMap(log(level, format, args, _))
  }

  object Service {
    def apply[R](
        f: (LoggerContext, Level, String, Array[Any], Map[String, Any]) => ZIO[R, Nothing, Unit]
    ): UIO[Service[R]] =
      FiberRef.make(Map.empty[String, Any], (first: Map[String, Any], _: Map[String, Any]) => first).map { fiberRef =>
        new FiberRefService[R](fiberRef) {
          override def log(level: Level, format: String, explicitArgs: Array[Any], implicitArgs: Map[String, Any])(
              implicit ctx: LoggerContext
          ): ZIO[R, Nothing, Unit] =
            f(ctx, level, format, explicitArgs, implicitArgs)
        }
      }
  }

  val mdc: UIO[Service[Any]] = Service {
    (ctx: LoggerContext, level: Level, format: String, explicitArgs: Array[Any], implicitArgs: Map[String, Any]) =>
      ZIO.effectTotal {
        val oldValues = implicitArgs.iterator.map {
          case (key, value) =>
            val oldValue = Option(MDC.get(key))
            MDC.put(key, value.toString)
            key -> oldValue
        }.toSeq
        ctx.log(level, format, explicitArgs)
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

  private def countPlaceholdersIn(format: String): Int = {
    var i = format.indexOf("{}")
    var counter = 0
    if (i == 0) {
      counter += 1
      i = format.indexOf("{}", 2)
    }
    while (i > 0) {
      if (format.charAt(i - 1) != '\\') {
        counter += 1
      }
      i = format.indexOf("{}", i + 2)
    }
    counter
  }

  val appendToMessage: UIO[Service[Any]] = Service {
    (ctx: LoggerContext, level: Level, format: String, explicitArgs: Array[Any], implicitArgs: Map[String, Any]) =>
      ZIO.effectTotal {
        val (fullFormat, fullArgs) = if (implicitArgs.isEmpty) {
          (format, explicitArgs)
        } else {
          val formatSuffix = implicitArgs.keysIterator.map { key =>
            s"$key = {}"
          }.mkString("; ")
          val numFormatArgs = countPlaceholdersIn(format)
          val numExplicitArgs = explicitArgs.length
          val fullArgs = numFormatArgs.compare(numExplicitArgs) match {
            case -1 =>
              val (formatArgs, extraArgs) = explicitArgs.splitAt(numFormatArgs)
              formatArgs ++ implicitArgs.values ++ extraArgs
            case 1 =>
              explicitArgs ++ Seq.fill(numFormatArgs - numExplicitArgs)(null) ++ implicitArgs.values
            case _ =>
              explicitArgs ++ implicitArgs.values
          }
          (s"$format; $formatSuffix", fullArgs)
        }
        ctx.log(level, fullFormat, fullArgs)
      }
  }
}
