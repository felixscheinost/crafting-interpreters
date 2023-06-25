package de.felixscheinost.klox

class LoxRuntimeError(
  val token: Token,
  message: String
) : RuntimeException(message)