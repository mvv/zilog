package com.github.mvv.zilog

sealed trait Level {
  protected val code: Int
}

object Level {
  case object Error extends Level {
    override protected val code = 4
  }
  case object Warn extends Level {
    override protected val code = 3
  }
  case object Info extends Level {
    override protected val code = 2
  }
  case object Debug extends Level {
    override protected val code = 1
  }
  case object Trace extends Level {
    override protected val code = 0
  }

  implicit val levelOrdering: Ordering[Level] = new Ordering[Level] {
    override def compare(x: Level, y: Level): Int = x.code.compare(y.code)
  }
}
