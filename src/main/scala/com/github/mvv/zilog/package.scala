package com.github.mvv

import zio.{Has, URIO, ZIO}

package object zilog extends Logger.Interface[Has[Logger.Service]] {
  type Logger = Has[Logger.Service]
  type ImplicitArgsLogger = Has[ImplicitArgsLogger.Service]

  override def log(level: Level, format: String, args: Array[Any])(implicit ctx: LoggerContext): URIO[Logger, Unit] =
    ZIO.accessM[Logger](_.get.log(level, format, args))
}
