package com.github.mvv.zilog.test

import com.github.mvv.zilog.{log, Logger, Logging}
import zio.{Ref, ZIO}
import zio.test.environment.TestEnvironment
import zio.test._
import zio.test.Assertion._

object TextLoggingSpec extends DefaultRunnableSpec {
  implicit val logger: Logger = Logger[TextLoggingSpec.type]

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("TextLogging")(
      testM("log") {
        traceLogEntries {
          log.withLogArgs(BarKey(1.23f)) {
            log.error("Something happened", FooKey(123))
          }
        }.zip(ZIO.fiberId).map {
          case (entries, fiberId) =>
            assert(entries) {
              equalTo(
                List(
                  s"1970-01-01T00:00:00Z [#${fiberId.seqNumber}] ERROR ${logger.name} - Something happened - bar=1.23, foo=123"
                )
              )
            }
        }
      }
    )

  object FooKey extends Logging.Key[Int]("foo")
  object BarKey extends Logging.Key[Float]("bar")

  def traceLogEntries[E](zio: ZIO[TestEnvironment with Logging, E, Any]): ZIO[TestEnvironment, E, List[String]] =
    Ref.make[Seq[String]](Vector.empty).flatMap { entriesRef =>
      zio
        .provideSomeLayer[TestEnvironment](StoppedClock.at(0L) >>> Logging.text(entry => entriesRef.update(_ :+ entry)))
        .zipRight(entriesRef.get.map(_.toList))
    }
}
