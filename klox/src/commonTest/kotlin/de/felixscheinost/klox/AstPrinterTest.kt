package de.felixscheinost.klox

import kotlin.test.Test
import kotlin.test.assertEquals

class AstPrinterTest {
  @Test
  fun testAstPrinter() {
    val expression: Expr = Expr.Binary(
      Expr.Unary(
        Token(TokenType.MINUS, "-", null, 1),
        Expr.Literal(123)
      ),
      Token(TokenType.STAR, "*", null, 1),
      Expr.Grouping(
        Expr.Literal(45.67)
      )
    )
    assertEquals(
      "(* (- 123) (group 45.67))",
      expression.accept(AstPrinter())
    )
  }
}