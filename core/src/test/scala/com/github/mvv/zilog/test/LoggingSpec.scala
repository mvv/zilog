package com.github.mvv.zilog.test

import com.github.mvv.zilog._
import zio.{Ref, UIO, ZIO, ZLayer}
import zio.test.environment.TestEnvironment
import zio.test._
import zio.test.Assertion._

object LoggingSpec extends DefaultRunnableSpec {
  implicit val logger: Logger = Logger[LoggingSpec.type]
  val SourceFile = "LoggingSpec.scala"
  val SourceClass = "com.github.mvv.zilog.test.LoggingSpec"
  val SourceMethod = "spec"

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("Logging")(
      testM("simple") {
        assertM {
          traceLogEntries {
            log.info("simple info") *> log.error("simple error")
          }
        }(
          equalTo(
            List(
              LogEntry(Logging.Info,
                       "simple info",
                       Logging.NoArgs,
                       Logging.NoStackTrace,
                       SourceFile,
                       SourceClass,
                       SourceMethod),
              LogEntry(Logging.Error,
                       "simple error",
                       Logging.NoArgs,
                       Logging.NoStackTrace,
                       SourceFile,
                       SourceClass,
                       SourceMethod)
            )
          )
        )
      },
      testM("withLogArgs") {
        assertM {
          traceLogEntries {
            log.withLogArgs(FooKey("a")) {
              log.info("msg")
            }
          }.map(_.map(_.structuredArgs))
        }(elems(hasArg(FooKey, equalTo("a"))))
      },
      testM("withLogArgs override") {
        assertM {
          traceLogEntries {
            log.withLogArgs(FooKey("a")) {
              log.info("msg", FooKey("b"))
            }
          }.map(_.map(_.structuredArgs))
        }(elems(hasArg(FooKey, equalTo("b"))))
      },
      testM("withLogArgs nested") {
        assertM {
          traceLogEntries {
            log.withLogArgs(FooKey("a")) {
              log.withLogArgs(FooKey("b")) {
                log.info("msg", BarKey("c"))
              }
            }
          }.map(_.map(_.structuredArgs))
        }(elems(hasArg(FooKey, equalTo("b")) && hasArg(BarKey, equalTo("c"))))
      },
      suite("stack trace")(
        testM("throwable") {
          assertM {
            traceLogEntries {
              ZIO.fail(new RuntimeException).catchAll { e =>
                log.error(e, s"error $e")
              }
            }.map(_.map(_.stackTrace))
          }(elems(isSubtype[Logging.ThrowableStackTrace](anything)))
        },
        testM("cause") {
          assertM {
            traceLogEntries {
              ZIO.fail(1).catchAllCause { c =>
                log.error(c, "error")
              }
            }.map(_.map(_.stackTrace))
          }(elems(isSubtype[Logging.CauseStackTrace](anything)))
        },
        testM("trace") {
          assertM {
            traceLogEntries {
              ZIO.fail(1).catchAllTrace {
                case (e, trace) =>
                  log.error(trace, s"error $e")
              }
            }.map(_.map(_.stackTrace))
          }(elems(isSubtype[Logging.ZTraceStackTrace](ztraceStackTraceError(isNone))))
        },
        testM("error + trace") {
          assertM {
            traceLogEntries {
              ZIO.fail(1: Any).catchAllTrace {
                case (e, trace) =>
                  log.error(e, trace, s"error $e")
              }
            }.map(_.map(_.stackTrace))
          }(elems(isSubtype[Logging.ZTraceStackTrace](ztraceStackTraceError(isNone))))
        },
        testM("throwable + trace (static)") {
          assertM {
            traceLogEntries {
              ZIO.fail(new RuntimeException).catchAllTrace {
                case (e, trace) =>
                  log.error(e, trace, s"error $e")
              }
            }.map(_.map(_.stackTrace))
          }(elems(isSubtype[Logging.ZTraceStackTrace](ztraceStackTraceError(isSome(anything)))))
        },
        testM("throwable + trace (runtime)") {
          assertM {
            traceLogEntries {
              ZIO.fail(new RuntimeException: Any).catchAllTrace {
                case (e, trace) =>
                  log.error(e, trace, s"error $e")
              }
            }.map(_.map(_.stackTrace))
          }(elems(isSubtype[Logging.ZTraceStackTrace](ztraceStackTraceError(isSome(anything)))))
        }
      )
    )

  final case class LogEntry(level: Logging.Level,
                            message: String,
                            structuredArgs: Logging.Args,
                            stackTrace: Logging.StackTrace,
                            sourceFile: String,
                            sourceClass: String,
                            sourceMethod: String)

  def tracingLogging: UIO[TracingLoggingService] =
    Ref.make[Seq[LogEntry]](Vector.empty).map(new TracingLoggingService(_))
  def traceLogEntries[E](zio: ZIO[TestEnvironment with Logging, E, Any]): ZIO[TestEnvironment, E, List[LogEntry]] =
    tracingLogging.flatMap(logging =>
      zio.provideSomeLayer[TestEnvironment](ZLayer.succeed(logging: Logging.Service)) *> logging.entries)

  final class TracingLoggingService(log: Ref[Seq[LogEntry]]) extends Logging.Service {
    override type ResolvedLogger = Unit
    override def resolveLogger(level: Logging.Level)(implicit logger: Logger): UIO[Option[Unit]] =
      UIO.succeed(Some(()))
    override def log(logger: Unit,
                     level: Logging.Level,
                     message: String,
                     structuredArgs: Logging.Args,
                     stackTrace: Logging.StackTrace,
                     sourceFile: String,
                     sourceClass: String,
                     sourceMethod: String,
                     sourceLine: Int): UIO[Unit] =
      log.update(_ :+ LogEntry(level, message, structuredArgs, stackTrace, sourceFile, sourceClass, sourceMethod))

    def entries: UIO[List[LogEntry]] = log.get.map(_.toList)
  }

  object FooKey extends Logging.Key[String]("foo")
  object BarKey extends Logging.Key[String]("bar")

  private def hasArg[A](key: Logging.Key[A], assertion: Assertion[A]): Assertion[Logging.Args] =
    assertionRec[Logging.Args, A]("hasArg")(Render.param(key.name), Render.param(assertion))(assertion)(_(key))
  private def ztraceStackTraceError(assertion: Assertion[Option[Throwable]]): Assertion[Logging.ZTraceStackTrace] =
    assertionRec[Logging.ZTraceStackTrace, Option[Throwable]]("ztraceStackTraceError")(Render.param(assertion))(
      assertion)(stackTrace => Some(stackTrace.error))
  private def elems[A](assertions: Assertion[A]*): Assertion[Seq[A]] = {
    lazy val result: Assertion[Seq[A]] = assertionDirect[Seq[A]]("elems")(assertions.map(Render.param): _*) { seq =>
      if (seq.size == assertions.size) {
        assertions
          .zip(seq)
          .map {
            case (assertion, elem) =>
              assertion.run(elem)
          }
          .reduceLeftOption(_ && _)
          .getOrElse {
            AssertionData(result, seq).asSuccess
          }
      } else {
        AssertionData(result, seq).asFailure
      }
    }
    result
  }
}
