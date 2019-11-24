package com.github.mvv

import zio.ZIO

package object zilog extends Logger.Service[Logger] with ImplicitArgsLogger.Service[ImplicitArgsLogger] {
  override def log(level: Level, format: String, args: Array[Any])(
      implicit ctx: LoggerContext
  ): ZIO[Logger, Nothing, Unit] =
    ZIO.accessM[Logger](_.logger.log(level, format, args))
  override def withImplicitLogArgs[R1 <: ImplicitArgsLogger, E, A](
      args: (String, Any)*
  )(zio: ZIO[R1, E, A]): ZIO[R1, E, A] =
    ZIO.accessM[R1](_.logger.withImplicitLogArgs(args: _*)(zio))
}
