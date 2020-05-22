# Zilog
[![Release Version](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.github.mvv.zilog/zilog_2.13.svg)](https://oss.sonatype.org/content/repositories/releases/com/github/mvv/zilog)
[![Snapshot Version](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.github.mvv.zilog/zilog_2.13.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/github/mvv/zilog)
[![Build Status](https://travis-ci.com/mvv/zilog.svg?branch=master)](https://travis-ci.com/mvv/zilog)

Structured logging library for [ZIO](https://zio.dev)

## Using Zilog in your project

Add Zilog to your dependencies

```scala
libraryDependencies += "com.github.mvv.zilog" %% "zilog" % "0.1-M10"
```

If you plan to use custom compound structured arguments (like in the example
below), you might want to also add `sredded-generic` dependency for the
`deriveStructured` macro:

```scala
libraryDependencies += "com.github.mvv.sredded" %% "sredded-generic" % "0.1-M2" % Provided
```

Running the following example program

```scala
import com.github.mvv.sredded.StructuredMapping
import com.github.mvv.sredded.generic.deriveStructured
import com.github.mvv.zilog._
import zio.{App, ZEnv, ZIO}

final case class Request(src: String, method: String, path: String)
object Request {
  implicit val structured: StructuredMapping[Request] = deriveStructured
}

object RequestKey extends Logging.Key[Request]("request")
object CorrelationIdKey extends Logging.Key[String]("correlationId")
object CustomerIdKey extends Logging.Key[Long]("customerId")

object LoggingApp extends App {
  implicit val logger: Logger = Logger[LoggingApp.type]
  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    {
      log.withLogArgs(RequestKey(Request("src", "GET", "/foo")), CorrelationIdKey("someId")) {
        log.info("Logic go brrrrr", CustomerIdKey(123))
      }
    } .provideCustomLayer(Logging.consoleJson()).as(0)
}
```

should produce something like (as a single line)

```json
{
  "timestamp": "2020-05-01T00:00:00.000Z",
  "fiberId": "#0",
  "logger": "LoggingApp$",
  "level": "INFO",
  "message": "Logic go brrrrr",
  "args": {
    "request": {
      "src": "src",
      "method": "GET",
      "path": "/foo"
    },
    "correlationId": "someId",
    "customerId": 123
  },
  "stackTrace": null,
  "sourceFile": "LoggingApp.scala",
  "sourceClass": "LoggingApp",
  "sourceMethod": "run",
  "sourceLine": 20
}
```

## Submodules

* `zilog-sager` allows you to access `Logging` instances in [Sager](https://github.com/mvv/sager) environments
* `zilog-over-slf4j` provides a `Logging` service that uses SLF4J as a backend
