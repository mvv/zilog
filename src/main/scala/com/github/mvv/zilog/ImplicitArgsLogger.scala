package com.github.mvv.zilog

import org.slf4j.MDC
import zio.{Has, Layer, UIO, URIO, ZIO, ZLayer}

object ImplicitArgsLogger {
  trait Service extends Logger.Service {
    def withImplicitLogArgs(args: (String, Any)*): Service
  }

  private class MdcService(implicitArgs: Map[String, Any]) extends Service {
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

  private class AppendToMessageService(underlying: Logger.Service, implicitArgs: Map[String, Any]) extends Service {
    import AppendToMessageService._
    override def withImplicitLogArgs(args: (String, Any)*): Service =
      new AppendToMessageService(underlying, implicitArgs ++ args)
    override def log(level: Level, format: String, args: Array[Any])(implicit ctx: LoggerContext): URIO[Any, Unit] = {
      val (fullFormat, fullArgs) = if (implicitArgs.isEmpty) {
        (format, args)
      } else {
        val formatSuffix = implicitArgs.keysIterator.map(key => s"$key = {}").mkString("; ")
        val numFormatArgs = countPlaceholdersIn(format)
        val numExplicitArgs = args.length
        val fullArgs = numFormatArgs.compare(numExplicitArgs) match {
          case -1 =>
            val (formatArgs, extraArgs) = args.splitAt(numFormatArgs)
            formatArgs ++ implicitArgs.values ++ extraArgs
          case 1 =>
            args ++ Seq.fill(numFormatArgs - numExplicitArgs)(null) ++ implicitArgs.values
          case _ =>
            args ++ implicitArgs.values
        }
        (s"$format; $formatSuffix", fullArgs)
      }
      underlying.log(level, fullFormat, fullArgs)
    }
  }

  object AppendToMessageService {
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
  }

  object Service {
    val mdc: Service = new MdcService(Map.empty)
    def appendToMessage(service: Logger.Service): Service = new AppendToMessageService(service, Map.empty)
  }

  val any: ZLayer[ImplicitArgsLogger, Nothing, ImplicitArgsLogger] = ZLayer.requires[ImplicitArgsLogger]
  val mdc: Layer[Nothing, ImplicitArgsLogger] = ZLayer.succeed(Service.mdc)
  val appendToMessage: ZLayer[Logger, Nothing, ImplicitArgsLogger] =
    ZLayer.requires[Logger].map(has => Has(Service.appendToMessage(has.get)))
}
