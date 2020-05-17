package com.github.mvv

import zio.Has

package object zilog {
  final type Logging = Has[Logging.Service]
}
