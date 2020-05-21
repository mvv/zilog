package com.github.mvv.zilog.sager.test

import com.github.mvv.zilog.sager._
import zio.test._
import zio.test.environment.TestEnvironment
import zio.test.Assertion.isUnit

object SagerSpec extends DefaultRunnableSpec {
  implicit val logger: Logger = Logger[SagerSpec.type]

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("Sager")(
      testM("Macro expansion") {
        assertM(log.info("foo").provideLayer(SagerLogging.nop))(isUnit)
      }
    )
}
