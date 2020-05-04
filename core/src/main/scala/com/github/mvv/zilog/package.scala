package com.github.mvv

import zio.{CanFail, Cause, Has, ZIO, ZTrace}

package object zilog {
  final type Logging = Has[Logging.Service]

  implicit final class ZioCauseZilogOps[E](val underlying: Cause[E]) extends AnyVal {
    def failureTraceOption: Option[(E, Option[ZTrace])] =
      underlying.find {
        case Cause.Traced(Cause.Fail(e), trace) => (e, Some(trace))
        case Cause.Fail(e)                      => (e, None)
      }
    def failureTraceOrCause: Either[(E, Option[ZTrace]), Cause[Nothing]] =
      failureTraceOption match {
        case Some(errorAndTrace) => Left(errorAndTrace)
        case None                => Right(underlying.asInstanceOf[Cause[Nothing]])
      }
  }

  implicit final class ZioZilogOps[R, E, A](val underlying: ZIO[R, E, A]) extends AnyVal {
    def foldTraceM[R1 <: R, E2, B](
        failure: ((E, Option[ZTrace])) => ZIO[R1, E2, B],
        success: A => ZIO[R1, E2, B]
    )(implicit ev: CanFail[E]): ZIO[R1, E2, B] =
      underlying.foldCauseM(_.failureTraceOrCause.fold(failure, ZIO.halt(_)), success)
    def tapErrorTrace[R1 <: R, E1 >: E](
        f: ((E, Option[ZTrace])) => ZIO[R1, E1, Any]
    )(implicit ev: CanFail[E]): ZIO[R1, E1, A] =
      underlying.foldCauseM(c => c.failureTraceOrCause.fold(f(_) *> ZIO.halt(c), _ => ZIO.halt(c)),
                            new ZIO.SucceedFn(f))
    def catchAllTrace[R1 <: R, E2, A1 >: A](
        h: ((E, Option[ZTrace])) => ZIO[R1, E2, A1]
    )(implicit ev: CanFail[E]): ZIO[R1, E2, A1] =
      foldTraceM[R1, E2, A1](h, new ZIO.SucceedFn(h))
    def catchSomeTrace[R1 <: R, E1 >: E, A1 >: A](
        pf: PartialFunction[(E, Option[ZTrace]), ZIO[R1, E1, A1]]
    )(implicit ev: CanFail[E]): ZIO[R1, E1, A1] = {
      def tryRescue(c: Cause[E]): ZIO[R1, E1, A1] =
        c.failureTraceOrCause.fold(t => pf.applyOrElse(t, (_: (E, Option[ZTrace])) => ZIO.halt(c)), ZIO.halt(_))
      underlying.foldCauseM[R1, E1, A1](tryRescue, new ZIO.SucceedFn(pf))
    }
  }
}
