package de.felixscheinost.klox

import de.felixscheinost.klox.TokenType.*


/**
 * TODO:
 *   - Synchronization: On error, skip tokens until we get to next statement. Then start parsing again to minimize cascaded errors.
 */
class Parser(
  private val context: InterpretationContext,
  private val tokens: List<Token>
) {

  fun parse(): Expr? {
    return try {
      expression()
    } catch (error: ParseError) {
      null
    }
  }

  private class ParseError : RuntimeException()

  private var current: Int = 0

  // Grammar: expression     → comma ;
  private fun expression(): Expr {
    return comma()
  }

  // Grammar: comma          → ternary ( "," ternary )* ;
  private fun comma(): Expr = handleLeftAssociateBinary(::ternary, COMMA)

  // Grammar: ternary        → equality ( "?" ternary ":" ternary )? ;
  private fun ternary(): Expr {
    val expr = equality()
    if (match(QUESTION_MARK)) {
      val first = ternary()
      consume(COLON, "Expect ':' in ternary.")
      val second = ternary()
      return Expr.Ternary(expr, first, second)
    }
    return expr
  }

  // Grammar: equality       → comparison ( ( "!=" | "==" ) comparison )* ;
  private fun equality(): Expr = handleLeftAssociateBinary(::comparison, BANG_EQUAL, EQUAL_EQUAL)

  // Grammar: comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )*
  private fun comparison(): Expr = handleLeftAssociateBinary(::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)

  //  Grammar: term           → factor ( ( "-" | "+" ) factor )* ;
  private fun term(): Expr = handleLeftAssociateBinary(::factor, MINUS, PLUS)

  // factor         → unary ( ( "/" | "*" ) unary )* ;
  private fun factor(): Expr = handleLeftAssociateBinary(::unary, SLASH, STAR)

  // Grammar: unary          → ( "!" | "-" ) unary
  //                         | primary ;
  private fun unary(): Expr {
    if (match(BANG, MINUS)) {
      val operator = previous()
      val right = unary()
      return Expr.Unary(operator, right)
    }
    return primary()
  }

  private fun primary(): Expr {
    if (match(FALSE)) return Expr.Literal(false)
    if (match(TRUE)) return Expr.Literal(true)
    if (match(NIL)) return Expr.Literal(null)
    if (match(NUMBER, STRING)) {
      return Expr.Literal(previous().literal)
    }
    if (match(LEFT_PAREN)) {
      val expr = expression()
      consume(RIGHT_PAREN, "Expect ')' after expression.")
      return Expr.Grouping(expr)
    }
    throw error(peek(), "Expect expression.")
  }

  private fun handleLeftAssociateBinary(nested: () -> Expr, vararg types: TokenType): Expr {
    var expr: Expr = nested()
    while (match(*types)) {
      val operator = previous()
      val right: Expr = nested()
      expr = Expr.Binary(expr, operator, right)
    }
    return expr
  }

  private fun match(vararg types: TokenType): Boolean {
    for (type in types) {
      if (check(type)) {
        advance()
        return true
      }
    }
    return false
  }

  private fun consume(type: TokenType, message: String): Token {
    if (check(type)) {
      return advance()
    }
    throw error(peek(), message)
  }

  private fun check(type: TokenType): Boolean {
    return if (isAtEnd()) false else peek().type == type
  }

  private fun advance(): Token {
    if (!isAtEnd()) {
      current++
    }
    return previous()
  }

  private fun isAtEnd(): Boolean {
    return peek().type == EOF
  }

  private fun peek(): Token {
    return tokens[current]
  }

  private fun previous(): Token {
    return tokens[current - 1]
  }

  private fun error(token: Token, message: String): ParseError {
    if (token.type == EOF) {
      context.syntaxError(line = token.line, where = " at end", message = message)
    } else {
      context.syntaxError(line = token.line, where = " at '${token.lexeme}'", message = message)
    }
    return ParseError()
  }

  /**
   * When the parser encounters an error, e.g. missing right operand in `1 + <missing>` we need to
   *  1) Report the error on this line
   *  but also 2) "synchronize" the parser to minimize cascading errors.
   *
   *  Without synchronization in the worst but pretty likely case every following token will be treated as an error as an error as it just doesn't make sense in the parser's curren state.
   *  But assuming that e.g. the next line of the program is probably ok we do a "synchronization" which means resetting the parser to a state where it can successfully consume tokens.
   *  For this we need to step back the parser state as well as possibly skip some tokens so that they "line up" again and we can continue parsing successfully.
   */
  private fun synchronize() {
    advance()
    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) {
        return
      }
      when (peek().type) {
        CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
        else -> {}
      }
      advance()
    }
  }
}