package com.github.mvv.zilog.slf4j.impl

import com.github.mvv.zilog.{Logger, Logging}
import org.slf4j
import zio.UIO

import scala.annotation.tailrec

trait Slf4jLoggingService extends Logging.Service {
  import Slf4jLoggingService._

  final override type ResolvedLogger = org.slf4j.Logger
  final override def resolveLogger(level: Logging.Level)(implicit logger: Logger): UIO[Option[slf4j.Logger]] =
    logger.cached(LoggerKey)(UIO.effectTotal(org.slf4j.LoggerFactory.getLogger(logger.name))).flatMap { lg =>
      val enabled = level match {
        case Logging.Fatal | Logging.Error => lg.isErrorEnabled
        case Logging.Info                  => lg.isInfoEnabled
        case Logging.Warn                  => lg.isWarnEnabled
        case Logging.Debug                 => lg.isDebugEnabled
        case Logging.Trace                 => lg.isTraceEnabled
      }
      if (enabled) UIO.some(lg) else UIO.none
    }
}

object Slf4jLoggingService {
  val LoggerKey = Logger.key[org.slf4j.Logger]

  @tailrec
  def escapeMessageFrom(builder: java.lang.StringBuilder, message: String, start: Int): String =
    message.indexOf("{}") match {
      case -1 => builder.append(message.substring(start)).toString
      case i =>
        if (i > start) {
          builder.append(message.substring(start, i))
        }
        escapeMessageFrom(builder.append("\\{}"), message, i + 2)
    }

  def escapeMessage(message: String): String =
    message.indexOf("{}") match {
      case -1 => message
      case i =>
        val builder = new java.lang.StringBuilder(message.length + 1)
        if (i > 0) {
          builder.append(message.substring(0, i))
        }
        escapeMessageFrom(builder.append("\\{}"), message, i + 2)
    }

  def doLog(logger: org.slf4j.Logger, level: Logging.Level, message: String, error: Option[Throwable]): Unit =
    level match {
      case Logging.Fatal | Logging.Error =>
        error match {
          case None    => logger.error(message)
          case Some(e) => logger.error(message, e)
        }
      case Logging.Warn =>
        error match {
          case None    => logger.warn(message)
          case Some(e) => logger.warn(message, e)
        }
      case Logging.Info =>
        error match {
          case None    => logger.info(message)
          case Some(e) => logger.info(message, e)
        }
      case Logging.Debug =>
        error match {
          case None    => logger.debug(message)
          case Some(e) => logger.debug(message, e)
        }
      case Logging.Trace =>
        error match {
          case None    => logger.trace(message)
          case Some(e) => logger.trace(message, e)
        }
    }
}
