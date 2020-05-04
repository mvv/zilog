package com.github.mvv.zilog.test

import java.time.{DateTimeException, OffsetDateTime}
import java.util.concurrent.TimeUnit

import zio.{IO, UIO, ZLayer}
import zio.clock.Clock
import zio.duration.Duration

class StoppedClock(epochMillisecond: Long) extends Clock.Service {
  override def currentTime(unit: TimeUnit): UIO[Long] =
    UIO.effectTotal(unit.convert(epochMillisecond, TimeUnit.MILLISECONDS))
  override def currentDateTime: IO[DateTimeException, OffsetDateTime] = ???
  override def nanoTime: UIO[Long] = ???
  override def sleep(duration: Duration): UIO[Unit] = ???
}

object StoppedClock {
  def at(ts: Long): ZLayer[Any, Nothing, Clock] = ZLayer.succeed(new StoppedClock(ts))
}
