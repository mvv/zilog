package com.github.mvv.zilog.impl

import com.github.mvv.zilog.Logging
import zio.{Cause, ZTrace}

import scala.annotation.tailrec
import scala.reflect.macros.blackbox
import scala.language.higherKinds

class PrefixLoggingMacros(val c: blackbox.Context) extends LoggingMacros {
  import c.universe._
  import LoggingMacros._

  override protected def log(level: c.Expr[Logging.Level],
                             stackTrace: StackTraceExpr[c.Expr],
                             message: c.Expr[String],
                             args: Seq[c.Expr[Logging.Args]]): Tree =
    log(c.prefix.tree, level, stackTrace, message, args)
}
