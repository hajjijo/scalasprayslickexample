package com.vixxx123.logger

sealed class LogMessage(val msg: String, val tag: String, logType: LogLevel)

case class Debug(override val msg: String, override val tag: String) extends LogMessage(msg, tag, DebugLevel)
case class Info(override val msg: String, override val tag: String) extends LogMessage(msg, tag, InfoLevel)
case class Error(override val msg: String, override val tag: String, cause: Throwable, stack: Array[StackTraceElement])
  extends LogMessage(msg, tag, ErrorLevel)