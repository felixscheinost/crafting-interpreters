package de.felixscheinost.klox

data class LoxSyntaxError(
  val line: Int,
  val message: String,
  val where: String = ""
)