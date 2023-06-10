package de.felixscheinost.klox

class Token(
  val type: TokenType,
  val lexeme: String,
  val literal: Any?,
  val line: Int
) {
  override fun toString(): String {
    return "$type $lexeme $literal"
  }
}