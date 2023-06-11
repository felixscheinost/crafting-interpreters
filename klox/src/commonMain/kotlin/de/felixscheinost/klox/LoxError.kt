package de.felixscheinost.klox

class LoxError(
  val line: Int,
  val message: String,
  val where: String = ""
)