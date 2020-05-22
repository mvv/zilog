package com.github.mvv.zilog

import zio.ZIO

import scala.reflect.{classTag, ClassTag}

final class Logger private (val name: String) {
  import Logger._

  private var cache: (Key[_], Any) = initialCacheValue

  def cached[R, E, A](key: Key[A])(value: => ZIO[R, E, A]): ZIO[R, E, A] =
    ZIO.effectTotal(cache).flatMap {
      case (cachedKey, cachedValue) =>
        if (key eq cachedKey) {
          ZIO.succeed(cachedValue.asInstanceOf[A])
        } else {
          value.flatMap(v => ZIO.effectTotal { cache = (key, v); v })
        }
    }

  def sub(sub: String): Logger = new Logger(s"$name.$sub")
}

object Logger {
  private val initialCacheValue: (Key[_], Any) = (null, null)

  def apply(name: String): Logger = new Logger(name)
  def apply[A: ClassTag]: Logger = new Logger(classTag[A].runtimeClass.getTypeName)
  def apply(clazz: Class[_]): Logger = new Logger(clazz.getTypeName)

  final class Key[A]
  def key[A]: Key[A] = new Key[A]
}
