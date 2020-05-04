package com.github.mvv.zilog.impl

import com.github.mvv.zilog.Logging
import zio.{Cause, ZTrace}

import scala.annotation.tailrec
import scala.reflect.macros.blackbox
import scala.language.higherKinds

class LoggingMacros(val c: blackbox.Context) {
  import c.universe._
  import LoggingMacros._

  private val Fatal = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Fatal")
  private val Error = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Error")
  private val Warn = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Warn")
  private val Info = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Info")
  private val Debug = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Debug")
  private val Trace = c.Expr(q"_root_.com.github.mvv.zilog.Logging.Trace")

  def prefixLogFatal(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Fatal, StackTraceExpr.Empty, message, args)
  def prefixLogFatalWithThrowable(error: c.Expr[Throwable],
                                  message: c.Expr[String],
                                  args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Fatal, StackTraceExpr.FromThrowable(error), message, args)
  def prefixLogFatalWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Fatal, StackTraceExpr.FromCause(cause), message, args)
  def prefixLogFatalWithTrace(trace: c.Expr[Option[ZTrace]],
                              message: c.Expr[String],
                              args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Fatal, StackTraceExpr.FromTrace(trace, None), message, args)
  def prefixLogFatalWithErrorTrace(error: c.Expr[Any],
                                   trace: c.Expr[Option[ZTrace]],
                                   message: c.Expr[String],
                                   args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Fatal, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def prefixLogError(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Error, StackTraceExpr.Empty, message, args)
  def prefixLogErrorWithThrowable(error: c.Expr[Throwable],
                                  message: c.Expr[String],
                                  args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Error, StackTraceExpr.FromThrowable(error), message, args)
  def prefixLogErrorWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Error, StackTraceExpr.FromCause(cause), message, args)
  def prefixLogErrorWithTrace(trace: c.Expr[Option[ZTrace]],
                              message: c.Expr[String],
                              args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Error, StackTraceExpr.FromTrace(trace, None), message, args)
  def prefixLogErrorWithErrorTrace(error: c.Expr[Any],
                                   trace: c.Expr[Option[ZTrace]],
                                   message: c.Expr[String],
                                   args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Error, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def prefixLogWarn(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Warn, StackTraceExpr.Empty, message, args)
  def prefixLogWarnWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Warn, StackTraceExpr.FromThrowable(error), message, args)
  def prefixLogWarnWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Warn, StackTraceExpr.FromCause(cause), message, args)
  def prefixLogWarnWithTrace(trace: c.Expr[Option[ZTrace]],
                             message: c.Expr[String],
                             args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Warn, StackTraceExpr.FromTrace(trace, None), message, args)
  def prefixLogWarnWithErrorTrace(error: c.Expr[Any],
                                  trace: c.Expr[Option[ZTrace]],
                                  message: c.Expr[String],
                                  args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Warn, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def prefixLogInfo(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Info, StackTraceExpr.Empty, message, args)
  def prefixLogInfoWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Info, StackTraceExpr.FromThrowable(error), message, args)
  def prefixLogInfoWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Info, StackTraceExpr.FromCause(cause), message, args)
  def prefixLogInfoWithTrace(trace: c.Expr[Option[ZTrace]],
                             message: c.Expr[String],
                             args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Info, StackTraceExpr.FromTrace(trace, None), message, args)
  def prefixLogInfoWithErrorTrace(error: c.Expr[Any],
                                  trace: c.Expr[Option[ZTrace]],
                                  message: c.Expr[String],
                                  args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Info, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def prefixLogDebug(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Debug, StackTraceExpr.Empty, message, args)
  def prefixLogDebugWithThrowable(error: c.Expr[Throwable],
                                  message: c.Expr[String],
                                  args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Debug, StackTraceExpr.FromThrowable(error), message, args)
  def prefixLogDebugWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Debug, StackTraceExpr.FromCause(cause), message, args)
  def prefixLogDebugWithTrace(trace: c.Expr[Option[ZTrace]],
                              message: c.Expr[String],
                              args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Debug, StackTraceExpr.FromTrace(trace, None), message, args)
  def prefixLogDebugWithErrorTrace(error: c.Expr[Any],
                                   trace: c.Expr[Option[ZTrace]],
                                   message: c.Expr[String],
                                   args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Debug, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def prefixLogTrace(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Trace, StackTraceExpr.Empty, message, args)
  def prefixLogTraceWithThrowable(error: c.Expr[Throwable],
                                  message: c.Expr[String],
                                  args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Trace, StackTraceExpr.FromThrowable(error), message, args)
  def prefixLogTraceWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Trace, StackTraceExpr.FromCause(cause), message, args)
  def prefixLogTraceWithTrace(trace: c.Expr[Option[ZTrace]],
                              message: c.Expr[String],
                              args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Trace, StackTraceExpr.FromTrace(trace, None), message, args)
  def prefixLogTraceWithErrorTrace(error: c.Expr[Any],
                                   trace: c.Expr[Option[ZTrace]],
                                   message: c.Expr[String],
                                   args: c.Expr[Logging.Args]*): Tree =
    prefixLog(Trace, StackTraceExpr.FromTrace(trace, Some(error)), message, args)

  private def prefixLog(level: c.Expr[Logging.Level],
                        stackTrace: StackTraceExpr[c.Expr],
                        message: c.Expr[String],
                        args: Seq[c.Expr[Logging.Args]]): Tree =
    log(c.prefix.tree, level, stackTrace, message, args)

  def envLogFatal(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Fatal, StackTraceExpr.Empty, message, args)
  def envLogFatalWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Fatal, StackTraceExpr.FromThrowable(error), message, args)
  def envLogFatalWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Fatal, StackTraceExpr.FromCause(cause), message, args)
  def envLogFatalWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Fatal, StackTraceExpr.FromTrace(trace, None), message, args)
  def envLogFatalWithErrorTrace(error: c.Expr[Any],
                                trace: c.Expr[Option[ZTrace]],
                                message: c.Expr[String],
                                args: c.Expr[Logging.Args]*): Tree =
    envLog(Fatal, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def envLogError(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Error, StackTraceExpr.Empty, message, args)
  def envLogErrorWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Error, StackTraceExpr.FromThrowable(error), message, args)
  def envLogErrorWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Error, StackTraceExpr.FromCause(cause), message, args)
  def envLogErrorWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Error, StackTraceExpr.FromTrace(trace, None), message, args)
  def envLogErrorWithErrorTrace(error: c.Expr[Any],
                                trace: c.Expr[Option[ZTrace]],
                                message: c.Expr[String],
                                args: c.Expr[Logging.Args]*): Tree =
    envLog(Error, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def envLogWarn(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Warn, StackTraceExpr.Empty, message, args)
  def envLogWarnWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Warn, StackTraceExpr.FromThrowable(error), message, args)
  def envLogWarnWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Warn, StackTraceExpr.FromCause(cause), message, args)
  def envLogWarnWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Warn, StackTraceExpr.FromTrace(trace, None), message, args)
  def envLogWarnWithErrorTrace(error: c.Expr[Any],
                               trace: c.Expr[Option[ZTrace]],
                               message: c.Expr[String],
                               args: c.Expr[Logging.Args]*): Tree =
    envLog(Warn, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def envLogInfo(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Info, StackTraceExpr.Empty, message, args)
  def envLogInfoWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Info, StackTraceExpr.FromThrowable(error), message, args)
  def envLogInfoWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Info, StackTraceExpr.FromCause(cause), message, args)
  def envLogInfoWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Info, StackTraceExpr.FromTrace(trace, None), message, args)
  def envLogInfoWithErrorTrace(error: c.Expr[Any],
                               trace: c.Expr[Option[ZTrace]],
                               message: c.Expr[String],
                               args: c.Expr[Logging.Args]*): Tree =
    envLog(Info, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def envLogDebug(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Debug, StackTraceExpr.Empty, message, args)
  def envLogDebugWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Debug, StackTraceExpr.FromThrowable(error), message, args)
  def envLogDebugWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Debug, StackTraceExpr.FromCause(cause), message, args)
  def envLogDebugWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Debug, StackTraceExpr.FromTrace(trace, None), message, args)
  def envLogDebugWithErrorTrace(error: c.Expr[Any],
                                trace: c.Expr[Option[ZTrace]],
                                message: c.Expr[String],
                                args: c.Expr[Logging.Args]*): Tree =
    envLog(Debug, StackTraceExpr.FromTrace(trace, Some(error)), message, args)
  def envLogTrace(message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Trace, StackTraceExpr.Empty, message, args)
  def envLogTraceWithThrowable(error: c.Expr[Throwable], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Trace, StackTraceExpr.FromThrowable(error), message, args)
  def envLogTraceWithCause(cause: c.Expr[Cause[Any]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Trace, StackTraceExpr.FromCause(cause), message, args)
  def envLogTraceWithTrace(trace: c.Expr[Option[ZTrace]], message: c.Expr[String], args: c.Expr[Logging.Args]*): Tree =
    envLog(Trace, StackTraceExpr.FromTrace(trace, None), message, args)
  def envLogTraceWithErrorTrace(error: c.Expr[Any],
                                trace: c.Expr[Option[ZTrace]],
                                message: c.Expr[String],
                                args: c.Expr[Logging.Args]*): Tree =
    envLog(Trace, StackTraceExpr.FromTrace(trace, Some(error)), message, args)

  private def envLog(level: c.Expr[Logging.Level],
                     stackTrace: StackTraceExpr[c.Expr],
                     message: c.Expr[String],
                     args: Seq[c.Expr[Logging.Args]]): Tree = {
    val logging = TermName(c.freshName("logging"))
    val service = TermName(c.freshName("service"))
    q"""
       _root_.zio.ZIO.accessM[_root_.com.github.mvv.zilog.Logging] {
         ($logging: _root_.com.github.mvv.zilog.Logging) =>
           val $service = $logging.get
           ${log(q"$service", level, stackTrace, message, args)}
       }
     """
  }

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

  private def log(service: Tree,
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
  sealed private trait StackTraceExpr[+E[_]]
  private object StackTraceExpr {
    final case class FromThrowable[E[_]](error: E[Throwable]) extends StackTraceExpr[E]
    final case class FromTrace[E[_]](trace: E[Option[ZTrace]], error: Option[E[Any]]) extends StackTraceExpr[E]
    final case class FromCause[E[_]](cause: E[Cause[Any]]) extends StackTraceExpr[E]
    case object Empty extends StackTraceExpr[Nothing]
  }
}
