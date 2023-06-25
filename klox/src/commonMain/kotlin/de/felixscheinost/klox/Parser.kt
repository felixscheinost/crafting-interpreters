package de.felixscheinost.klox

import de.felixscheinost.klox.Expr.Assign
import de.felixscheinost.klox.TokenType.*

class Parser(
  private val context: InterpretationContext,
  private val tokens: List<Token>
) {

  private class ParseError : RuntimeException()

  private var current: Int = 0

  fun parseAsSingleExpression(): Expr? {
    return try {
      val expr = expression()
      if (!isAtEnd()) {
        error(peek(), "Couldn't parse source as single expression")
        return null
      }
      expr
    } catch (error: ParseError) {
      null
    }
  }

  fun parse() = program()

  // Grammar: program        → declaration* EOF ;
  private fun program(): List<Stmt> {
    return buildList {
      while (!isAtEnd()) {
        declaration()?.let(::add)
      }
    }
  }

  // We could treat variable declaration the same as any other statement but we don't
  // want to allow e.g. if (monday) var beverage = "espresso
  // This way we define two sets of statements: One who can be used everywhere, one who can be used only in certain places
  // Grammar: declaration    → varDecl
  //                         | statement ;
  private fun declaration(): Stmt? = try {
    if (match(VAR)) {
      varDeclaration()
    } else {
      statement()
    }
  } catch (error: ParseError) {
    synchronize()
    null
  }

  // Grammar: varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
  private fun varDeclaration(): Stmt {
    val name = consume(IDENTIFIER, "Expect variable name.")
    val initializer = if (match(EQUAL)) {
      expression()
    } else {
      null
    }
    consume(SEMICOLON, "Expect ';' after variable declaration.")
    return Stmt.Var(name, initializer)
  }

  // Grammar: statement      → exprStmt
  //                         | printStmt
  //                         | block ;
  private fun statement(): Stmt {
    if (match(PRINT)) {
      return printStatement()
    }
    if (match(LEFT_BRACE)) {
      return Stmt.Block(block())
    }
    return expressionStatement()
  }

  // Grammar: exprStmt       → expression ";" ;
  private fun printStatement(): Stmt {
    val value = expression()
    consume(SEMICOLON, "Expect ';' after value.")
    return Stmt.Print(value)
  }

  // Grammar: printStmt      → "print" expression ";" ;
  private fun expressionStatement(): Stmt {
    val expr = expression()
    consume(SEMICOLON, "Expect ';' after expression.")
    return Stmt.Expression(expr)
  }

  // Grammar: block          → "{" declaration* "}" ;
  private fun block(): List<Stmt> {
    val statements: MutableList<Stmt> = mutableListOf()
    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      declaration()?.let(statements::add)
    }
    consume(RIGHT_BRACE, "Expect '}' after block.")
    return statements
  }

  // Grammar: expression     → assignment ;
  private fun expression(): Expr {
    return assignment()
  }

  // assignment     → IDENTIFIER "=" assignment
  //                | comma ;
  private fun assignment(): Expr {
    val expr = comma()

    if (match(EQUAL)) {
      val equals = previous()
      val value = assignment()
      // TODO: Right now we only allow the expression on the left hand of the `=` to be a simple global variable name
      //       e.g. newPoint(x + 2, 0).y = 3; wouldn't work right now
      //       we will add that later
      if (expr is Expr.Variable) {
        return Assign(expr.name, value)
      }
      error(equals, "Invalid assignment target.")
    }

    return expr
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
    if (match(FALSE)) {
      return Expr.Literal(false)
    }
    if (match(TRUE)) {
      return Expr.Literal(true)
    }
    if (match(NIL)) {
      return Expr.Literal(null)
    }
    if (match(NUMBER, STRING)) {
      return Expr.Literal(previous().literal)
    }
    if (match(IDENTIFIER)) {
      return Expr.Variable(previous())
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
        else -> advance()
      }
    }
  }
}