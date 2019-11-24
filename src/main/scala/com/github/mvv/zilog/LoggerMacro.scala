package com.github.mvv.zilog

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

class LoggerMacro(val c: blackbox.Context) {
  import c.universe._

  private val Error = c.Expr(q"_root_.com.github.mvv.zilog.Level.Error")
  private val Warn = c.Expr(q"_root_.com.github.mvv.zilog.Level.Warn")
  private val Info = c.Expr(q"_root_.com.github.mvv.zilog.Level.Info")
  private val Debug = c.Expr(q"_root_.com.github.mvv.zilog.Level.Debug")
  private val Trace = c.Expr(q"_root_.com.github.mvv.zilog.Level.Trace")

  def logError(message: c.Expr[String]): Tree =
    log(Error, None, message)
  def logErrorWithThrowable(e: c.Expr[Throwable], message: c.Expr[String]): Tree =
    log(Error, Some(e), message)
  def logWarn(message: c.Expr[String]): Tree =
    log(Warn, None, message)
  def logWarnWithThrowable(e: c.Expr[Throwable], message: c.Expr[String]): Tree =
    log(Warn, Some(e), message)
  def logInfo(message: c.Expr[String]): Tree =
    log(Info, None, message)
  def logInfoWithThrowable(e: c.Expr[Throwable], message: c.Expr[String]): Tree =
    log(Info, Some(e), message)
  def logDebug(message: c.Expr[String]): Tree =
    log(Debug, None, message)
  def logDebugWithThrowable(e: c.Expr[Throwable], message: c.Expr[String]): Tree =
    log(Debug, Some(e), message)
  def logTrace(message: c.Expr[String]): Tree =
    log(Trace, None, message)
  def logTraceWithThrowable(e: c.Expr[Throwable], message: c.Expr[String]): Tree =
    log(Trace, Some(e), message)

  private object ListOfStrings {
    def unapply(list: List[Tree]): Option[List[String]] = {
      @tailrec
      def loop(acc: List[String], left: List[Tree]): Option[List[String]] =
        left.headOption match {
          case Some(Literal(Constant(s: String))) =>
            loop(s :: acc, left.tail)
          case Some(_) =>
            None
          case None =>
            Some(acc.reverse)
        }
      loop(Nil, list)
    }
  }

  def log(level: c.Expr[Level], error: Option[c.Expr[Throwable]], message: c.Expr[String]): Tree = {
    val (format, args) = message.tree match {
      // 2.11 and 2.12
      case Apply(Select(Apply(Select(prefix, TermName("apply")), ListOfStrings(formatPieces)), TermName("s")), args)
          if prefix.tpe.typeSymbol == typeOf[StringContext.type].typeSymbol && formatPieces.size == args.size + 1 =>
        (Literal(Constant(formatPieces.mkString("{}"))), args)
      // 2.13+
      case Typed(expr, _) =>
        @tailrec
        def linearize(acc: List[Tree], lhs: Tree): List[Tree] =
          lhs match {
            case Apply(Select(next, TermName("$plus")), List(arg))
                if next.tpe.typeSymbol == typeOf[String].typeSymbol =>
              linearize(arg :: acc, next)
            case other =>
              other :: acc
          }
        @tailrec
        def loop(format: String, args: Seq[Tree], rhs: Seq[Tree], isArg: Boolean): (Tree, Seq[Tree]) =
          rhs.headOption match {
            case Some(next) if isArg =>
              loop(s"$format{}", args :+ next, rhs.tail, false)
            case Some(Literal(Constant(s: String))) =>
              loop(s"$format$s", args, rhs.tail, true)
            case Some(next) =>
              loop(s"$format{}", args :+ next, rhs.tail, false)
            case None =>
              (Literal(Constant(format)), args)
          }
        loop("", Vector.empty, linearize(Nil, expr), false)
      case _ =>
        (message.tree, Seq.empty)
    }
    val argsWithError = error.fold(args)(e => args :+ e.tree)
    val ctx = TermName(c.freshName("ctx"))
    q"""{
          val $ctx = implicitly[_root_.com.github.mvv.zilog.LoggerContext]
          if ($ctx.isLevelEnabled($level)) {
            ${c.prefix}.log($level, $format, ${if (argsWithError.isEmpty) {
      q"_root_.scala.collection.immutable.Nil"
    } else {
      q"_root_.scala.Array[_root_.scala.Any](..$argsWithError)"
    }})
          } else {
            _root_.zio.ZIO.unit
          }
        }"""
  }
}
