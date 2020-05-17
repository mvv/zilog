package com.github.mvv.zilog.impl

import com.github.mvv.zilog.Logging
import zio.{Cause, ZTrace}

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

trait LoggingMacros {
  val c: blackbox.Context
  import c.universe._
  import LoggingMacros._

  final protected val Fatal = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Fatal")
  final protected val Error = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Error")
  final protected val Warn = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Warn")
  final protected val Info = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Info")
  final protected val Debug = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Debug")
  final protected val Trace = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Trace")

  def fatal(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Fatal, StackTraceExpr.Empty, message, args)
  def fatalWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Fatal, StackTraceExpr.FromThrowable(error), message, args)
  def fatalWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Fatal, StackTraceExpr.FromCause(cause), message, args)
  def fatalWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Fatal, StackTraceExpr.FromTrace(trace, None), message, args)
  def fatalWithErrorTrace(error: c.Expr[Any],
                          trace: c.Expr[Option[ZTrace]],
                          message: c.Expr[String],
                          args: c.Expr[Logging.Args]*): Tree =
    log(Fatal, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def error(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Error, StackTraceExpr.Empty, message, args)
  def errorWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Error, StackTraceExpr.FromThrowable(error), message, args)
  def errorWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Error, StackTraceExpr.FromCause(cause), message, args)
  def errorWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Error, StackTraceExpr.FromTrace(trace, None), message, args)
  def errorWithErrorTrace(error: c.Expr[Any],
                          trace: c.Expr[Option[ZTrace]],
                          message: c.Expr[String],
                          args: c.Expr[Logging.Args]*): Tree =
    log(Error, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def warn(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Warn, StackTraceExpr.Empty, message, args)
  def warnWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Warn, StackTraceExpr.FromThrowable(error), message, args)
  def warnWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Warn, StackTraceExpr.FromCause(cause), message, args)
  def warnWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Warn, StackTraceExpr.FromTrace(trace, None), message, args)
  def warnWithErrorTrace(error: c.Expr[Any],
                         trace: c.Expr[Option[ZTrace]],
                         message: c.Expr[String],
                         args: c.Expr[Logging.Args]*): Tree =
    log(Warn, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def info(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Info, StackTraceExpr.Empty, message, args)
  def infoWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Info, StackTraceExpr.FromThrowable(error), message, args)
  def infoWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Info, StackTraceExpr.FromCause(cause), message, args)
  def infoWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Info, StackTraceExpr.FromTrace(trace, None), message, args)
  def infoWithErrorTrace(error: c.Expr[Any],
                         trace: c.Expr[Option[ZTrace]],
                         message: c.Expr[String],
                         args: c.Expr[Logging.Args]*): Tree =
    log(Info, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def debug(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Debug, StackTraceExpr.Empty, message, args)
  def debugWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Debug, StackTraceExpr.FromThrowable(error), message, args)
  def debugWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Debug, StackTraceExpr.FromCause(cause), message, args)
  def debugWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Debug, StackTraceExpr.FromTrace(trace, None), message, args)
  def debugWithErrorTrace(error: c.Expr[Any],
                          trace: c.Expr[Option[ZTrace]],
                          message: c.Expr[String],
                          args: c.Expr[Logging.Args]*): Tree =
    log(Debug, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def trace(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Trace, StackTraceExpr.Empty, message, args)
  def traceWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Trace, StackTraceExpr.FromThrowable(error), message, args)
  def traceWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Trace, StackTraceExpr.FromCause(cause), message, args)
  def traceWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    log(Trace, StackTraceExpr.FromTrace(trace, None), message, args)
  def traceWithErrorTrace(error: c.Expr[Any],
                          trace: c.Expr[Option[ZTrace]],
                          message: c.Expr[String],
                          args: c.Expr[Logging.Args]*): Tree =
    log(Trace, StackTraceExpr.FromTrace(trace, Some(error)), message, args)

  protected def log(level: c.Expr[Logging.Level],
                    stackTrace: StackTraceExpr[c.Expr],
                    message: c.Expr[String],
                    args: Seq[c.Expr[Logging.Args]]): Tree

  @tailrec
  private def className(sym: Symbol): String =
    if (sym == NoSymbol) {
      "<none>"
    } else if (sym.isClass) {
      sym.asClass.fullName
    } else {
      className(sym.owner)
    }

  @tailrec
  private def classAndMethodName(sym: Symbol): (String, String) =
    if (sym == NoSymbol) {
      ("<none>", "<none>")
    } else if (sym.isMethod) {
      (className(sym.owner), sym.asMethod.name.decodedName.toString)
    } else {
      classAndMethodName(sym.owner)
    }

  private val throwableType: c.Type = c.typeOf[Throwable]

  final protected def log(service: Tree,
                          level: c.Expr[Logging.Level],
                          stackTrace: StackTraceExpr[c.Expr],
                          message: c.Expr[String],
                          args: Seq[c.Expr[Logging.Args]]): Tree = {
    val combinedArgs = args.foldLeft[Tree](q"_root_.com.github.mvv.zilog.Logging.NoArgs") {
      case (acc, next) => q"($acc ++ $next)"
    }
    val translatedStackTrace = stackTrace match {
      case StackTraceExpr.Empty =>
        q"_root_.com.github.mvv.zilog.Logging.NoStackTrace"
      case StackTraceExpr.FromThrowable(error) =>
        q"_root_.com.github.mvv.zilog.Logging.ThrowableStackTrace($error)"
      case StackTraceExpr.FromTrace(traceOption, errorExprOption) =>
        val (errorOption, errorStackTrace) =
          errorExprOption match {
            case Some(error) =>
              if (error.actualType <:< throwableType) {
                (q"_root_.scala.Some($error)", q"_root_.com.github.mvv.zilog.Logging.ThrowableStackTrace($error)")
              } else if (throwableType <:< error.actualType.erasure) {
                val t1 = TermName(c.freshName("t"))
                val errorOption =
                  q"""
                 $error match {
                   case $t1: $throwableType => _root_.scala.Some($t1)
                   case _ => _root_.scala.None
                 }
               """
                val t2 = TermName(c.freshName("t"))
                val errorStackTrace =
                  q"""
                 $error match {
                   case $t2: $throwableType => _root_.com.github.mvv.zilog.Logging.ThrowableStackTrace($t2)
                   case _ => _root_.com.github.mvv.zilog.Logging.NoStackTrace
                 }
               """
                (errorOption, errorStackTrace)
              } else {
                c.error(c.enclosingPosition, s"Error type ${error.actualType} is not a supertype of Throwable")
                (c.universe.EmptyTree, c.universe.EmptyTree)
              }
            case None =>
              (q"_root_.scala.None", q"_root_.com.github.mvv.zilog.Logging.NoStackTrace")
          }
        val trace = TermName(c.freshName("trace"))
        q"""
           $traceOption match {
             case _root_.scala.Some($trace) =>
              _root_.com.github.mvv.zilog.Logging.ZTraceStackTrace($trace, $errorOption)
             case _root_.scala.None =>
               $errorStackTrace
           }
         """
      case StackTraceExpr.FromCause(cause) =>
        q"_root_.com.github.mvv.zilog.Logging.CauseStackTrace($cause)"
    }
    val logger = TermName(c.freshName("logger"))
    val (sourceClass, sourceMethod) = classAndMethodName(c.internal.enclosingOwner)
    q"""
       $service.resolveLogger($level).flatMap {
         case _root_.scala.None =>
           _root_.zio.ZIO.unit
         case _root_.scala.Some($logger) =>
           $service.log($logger, $level, $message, $combinedArgs, $translatedStackTrace,
                        ${c.enclosingPosition.source.file.name},
                        $sourceClass,
                        $sourceMethod,
                        ${c.enclosingPosition.line})
       }
     """
  }
}

object LoggingMacros {
  sealed trait StackTraceExpr[+E[_]]
  object StackTraceExpr {
    final case class FromThrowable[E[_]](error: E[Throwable]) extends StackTraceExpr[E]
    final case class FromTrace[E[_]](trace: E[Option[ZTrace]], error: Option[E[Any]]) extends StackTraceExpr[E]
    final case class FromCause[E[_]](cause: E[Cause[Any]]) extends StackTraceExpr[E]
    case object Empty extends StackTraceExpr[Nothing]
  }
}
