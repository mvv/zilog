package com.github.mvv.zilog.structured

import com.github.mvv.sredded.StructValue

final case class StructuredLayout(
    timestampMember: String,
    fiberIdMember: String,
    loggerMember: String,
    levelMember: String,
    messageMember: String,
    argsMember: String,
    stackTraceMember: String,
    sourceFileMember: String,
    sourceClassMember: String,
    sourceMethodMember: String,
    sourceLineMember: String,
    postProcess: StructValue.Mapping => StructValue.Mapping
)

object StructuredLayout {
  val Default = StructuredLayout(
    timestampMember = "timestamp",
    fiberIdMember = "fiberId",
    loggerMember = "logger",
    levelMember = "level",
    messageMember = "message",
    argsMember = "args",
    stackTraceMember = "stackTrace",
    sourceFileMember = "sourceFile",
    sourceClassMember = "sourceClass",
    sourceMethodMember = "sourceMethod",
    sourceLineMember = "sourceLine",
    postProcess = identity[StructValue.Mapping]
  )
}
