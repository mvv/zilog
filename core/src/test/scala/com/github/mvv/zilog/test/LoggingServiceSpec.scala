package com.github.mvv.zilog.test

import com.github.mvv.zilog.{Logger, Logging}
import zio.test._
import zio.test.environment.TestEnvironment
import zio.test.Assertion.isUnit

object LoggingServiceSpec extends DefaultRunnableSpec {
  implicit val logger: Logger = Logger[LoggingServiceSpec.type]

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("LoggingService")(
      testM("Macro expansion") {
        assertM(Logging.Service.withMessage(Logging.Service.nop)(identity[String]).info("foo"))(isUnit)
      }
    )
}
