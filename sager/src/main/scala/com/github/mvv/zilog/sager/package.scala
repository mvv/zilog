package com.github.mvv.zilog

import com.github.mvv.zilog
import com.github.mvv.sager.zio._
import com.github.mvv.sager.zio.clock.Clock
import com.github.mvv.sager.zio.console.Console
import com.github.mvv.sredded.StructValue
import com.github.mvv.zilog.structured.StructuredLayout
import zio.{UIO, ULayer, URLayer, ZLayer}

package object sager {
  type Logging = Haz[zilog.Logging.Service]
  type LoggingEnv[A <: zilog.Logging.Service] = Env[zilog.Logging.Service, A]
  val Logging: zilog.Logging.type = zilog.Logging
  type Logger = zilog.Logger
  val Logger: zilog.Logger.type = zilog.Logger

  object SagerLogging {
    val any: URLayer[Logging, Logging] = ZLayer.requires[Logging]
    val nop: ULayer[Logging] = ZLayer.succeedHaz(Logging.Service.nop)
    def text(f: String => UIO[Unit]): URLayer[Clock, Logging] =
      ZLayer.fromServiceHaz[Clock.Service, Logging.Service](Logging.Service.text(_)(f))
    val consoleText: URLayer[Console with Clock, Logging] =
      ZLayer.fromServicesHaz[Console.Service, Clock.Service, Logging.Service] { (console, clock) =>
        Logging.Service.text(clock)(console.putStrLn)
      }
    def structured(layout: StructuredLayout)(f: StructValue.Mapping => UIO[Unit]): URLayer[Clock, Logging] =
      ZLayer.fromServiceHaz[Clock.Service, Logging.Service](Logging.Service.structured(_, layout)(f))
    def consoleJson(layout: StructuredLayout = StructuredLayout.Default): URLayer[Console with Clock, Logging] =
      ZLayer.fromServicesHaz[Console.Service, Clock.Service, Logging.Service] { (console, clock) =>
        import com.github.mvv.sredded.json._
        Logging.Service.structured(clock, layout)(entry => console.putStrLn(entry.asJsonString))
      }
    def mapMessage(f: String => String): URLayer[Logging, Logging] =
      ZLayer.fromServiceHaz[Logging.Service, Logging.Service](Logging.Service.withMessage(_)(f))
    def mapArgs(f: Logging.Args => Logging.Args): URLayer[Logging, Logging] =
      ZLayer.fromServiceHaz[Logging.Service, Logging.Service](Logging.Service.withArgs(_)(f))
    def withMinLevel(minLevel: Logging.Level): URLayer[Logging, Logging] =
      ZLayer.fromServiceHaz[Logging.Service, Logging.Service](Logging.Service.withMinLevel(_, minLevel))
  }
}
