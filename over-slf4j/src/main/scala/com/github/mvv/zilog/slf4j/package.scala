package com.github.mvv.zilog

import com.github.mvv.zilog.slf4j.impl.{
  AppendToMessageLoggingService,
  IgnoreLoggingService,
  MdcLoggingService,
  PassLoggingService
}
import zio.{ULayer, ZLayer}

package object slf4j {
  sealed trait StructuredArgsHandling
  object StructuredArgsHandling {
    case object Ignore extends StructuredArgsHandling
    case object AppendToMessage extends StructuredArgsHandling
    case object Pass extends StructuredArgsHandling
    final case class Mdc(nesting: Char = '.') extends StructuredArgsHandling
  }

  def slf4jLoggingService(argsHandling: StructuredArgsHandling): Logging.Service =
    argsHandling match {
      case StructuredArgsHandling.Ignore          => IgnoreLoggingService
      case StructuredArgsHandling.AppendToMessage => AppendToMessageLoggingService
      case StructuredArgsHandling.Pass            => PassLoggingService
      case StructuredArgsHandling.Mdc(nesting)    => new MdcLoggingService(nesting)
    }
  def slf4jLogging(argsHandling: StructuredArgsHandling): ULayer[Logging] =
    ZLayer.succeed(slf4jLoggingService(argsHandling))
}
