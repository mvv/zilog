package com.github.mvv.zilog.impl

import com.github.mvv.zilog.Logging
import zio.{Cause, ZTrace}

import scala.reflect.macros.blackbox

class EnvLoggingMacros(val c: blackbox.Context) extends LoggingMacros {
  import c.universe._
  import LoggingMacros._

  override protected def log(level: c.Expr[Logging.Level],
                             stackTrace: StackTraceExpr[c.Expr],
                             message: c.Expr[String],
                             args: Seq[c.Expr[Logging.Args]]): Tree = {
    val logging = TermName(c.freshName("logging"))
    val service = TermName(c.freshName("service"))
    q"""
       _root_.zio.ZIO.accessM[_root_.com.github.mvv.zilog.Logging] {
         ($logging: _root_.com.github.mvv.zilog.Logging) =>
           val $service = $logging.get
           ${log(service, level, stackTrace, message, args)}
       }
     """
  }
}
