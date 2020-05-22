package com.github.mvv.zilog.sager.impl

import com.github.mvv.zilog.Logging
import com.github.mvv.zilog.impl.LoggingMacros

import scala.reflect.macros.blackbox

class SagerLoggingMacros(val c: blackbox.Context) extends LoggingMacros {
  import c.universe._
  import LoggingMacros._

  override protected def log(level: c.Expr[Logging.Level],
                             stackTrace: StackTraceExpr[c.Expr],
                             message: c.Expr[String],
                             args: Seq[c.Expr[Logging.Args]]): Tree = {
    val logging = TermName(c.freshName("logging"))
    val service = TermName(c.freshName("service"))
    q"""
       _root_.zio.ZIO.accessM[_root_.com.github.mvv.zilog.sager.Logging] {
         ($logging: _root_.com.github.mvv.zilog.sager.Logging) =>
           val $service = $logging.value
           ${log(service, level, stackTrace, message, args)}
       }
     """
  }
}
