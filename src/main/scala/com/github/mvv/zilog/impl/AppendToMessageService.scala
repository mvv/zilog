package com.github.mvv.zilog.impl

import com.github.mvv.zilog.ImplicitArgsLogger.Service
import com.github.mvv.zilog.{Level, Logger, LoggerContext}
import zio.URIO

private[zilog] class AppendToMessageService(underlying: Logger.Service, implicitArgs: Map[String, Any])
    extends Service {
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
