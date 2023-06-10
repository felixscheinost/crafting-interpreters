package de.felixscheinost.klox

import de.felixscheinost.klox.TokenType.*

class Scanner(
  private val context: InterpretationContext,
  private val source: String
) {
  private val tokens: MutableList<Token> = ArrayList()
  private var start = 0
  private var current = 0
  private var line = 1

  fun scanTokens(): List<Token> {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current
      scanToken()
    }
    tokens.add(Token(EOF, "", null, line))
    return tokens
  }

  private fun isAtEnd(): Boolean {
    return current >= source.length
  }

  private fun scanToken() {
    when (val c = advance()) {
      '(' -> addToken(LEFT_PAREN)
      ')' -> addToken(RIGHT_PAREN)
      '{' -> addToken(LEFT_BRACE)
      '}' -> addToken(RIGHT_BRACE)
      ',' -> addToken(COMMA)
      '.' -> addToken(DOT)
      '-' -> addToken(MINUS)
      '+' -> addToken(PLUS)
      ';' -> addToken(SEMICOLON)
      '*' -> addToken(STAR)
      '!' -> addToken(if (advanceOnMatch('=')) BANG_EQUAL else BANG)
      '=' -> addToken(if (advanceOnMatch('=')) EQUAL_EQUAL else EQUAL)
      '<' -> addToken(if (advanceOnMatch('=')) LESS_EQUAL else LESS)
      '>' -> addToken(if (advanceOnMatch('=')) GREATER_EQUAL else GREATER)

      '/' -> {
        if (advanceOnMatch('/')) {
          // Handle comments
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) {
            advance()
          }
        } else if (advanceOnMatch('*')) {
          // Handle block comments
          // (Don't support nesting)
          // A block comment goes until the first */
          while (!isAtEnd() && !(peek() == '*' && peekNext() == '/')) {
            if (peek() == '\n') {
              line++
            }
            advance()
          }
          if (peek() == '*' && peekNext() == '/') {
            advance()
            advance()
          }
        } else {
          addToken(SLASH)
        }
      }


      ' ', '\r', '\t' -> {
        // Ignore whitespace.
      }

      '\n' -> line++

      '"' -> string()

      else -> {
        if (c.isDigit()) {
          number()
        } else if (c.isLetterOrDigit()) {
          identifier();
        } else {
          context.error(line, "Unexpected character '$c'")
        }
      }
    }
  }

  private fun string() {
    while (peek() != '"' && !isAtEnd()) {
      // For no particular reason, Lox supports multi-line strings.
      // There are pros and cons to that, but prohibiting them was a little more complex than allowing them, so I left them in.
      if (peek() == '\n') {
        line++
      }
      advance()
    }
    if (isAtEnd()) {
      context.error(line, "Unterminated string.")
      return
    }

    // The closing ".
    advance()

    // Trim the surrounding quotes.
    val value = source.substring(start + 1, current - 1)
    addToken(STRING, value)
  }

  private fun number() {
    while (peek().isDigit()) {
      advance()
    }

    // Look for a fractional part.
    if (peek() == '.' && peekNext().isDigit()) {
      // Consume the "."
      advance()
      while (peek().isDigit()) {
        advance()
      }
    }

    addToken(NUMBER, source.substring(start, current).toDouble())
  }

  private fun identifier() {
    while (isAlphaNumeric(peek())) {
      advance()
    }
    val text = source.substring(start, current)
    addToken(keywords[text] ?: IDENTIFIER)
  }

  private fun advance(): Char {
    return source[current++]
  }

  private fun advanceOnMatch(expected: Char): Boolean {
    if (peek() != expected) {
      return false
    }
    current++
    return true
  }

  private fun peek(): Char {
    return if (isAtEnd()) '\u0000' else source[current]
  }

  private fun peekNext(): Char {
    return if (current + 1 >= source.length) {
      '\u0000'
    } else {
      source[current + 1]
    }
  }

  private fun isAlpha(c: Char): Boolean {
    return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
  }

  private fun isAlphaNumeric(c: Char): Boolean {
    return isAlpha(c) || c.isDigit()
  }

  private fun addToken(type: TokenType, literal: Any? = null) {
    val text = source.substring(start, current)
    tokens.add(Token(type, text, literal, line))
  }

  companion object {
    private val keywords = mapOf(
      "and" to AND,
      "class" to CLASS,
      "else" to ELSE,
      "false" to FALSE,
      "for" to FOR,
      "fun" to FUN,
      "if" to IF,
      "nil" to NIL,
      "or" to OR,
      "print" to PRINT,
      "return" to RETURN,
      "super" to SUPER,
      "this" to THIS,
      "true" to TRUE,
      "var" to VAR,
      "while" to WHILE,
    )
  }
}
