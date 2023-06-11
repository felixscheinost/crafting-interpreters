package de.felixscheinost.klox

import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
  @Test
  fun basicParserTest() {
    assertParse(
      "1 * 2.5",
      "(* 1.0 2.5)"
    )
    assertParse(
      "1 * 2.5 * 5",
      "(* (* 1.0 2.5) 5.0)"
    )
    assertParse(
      "1 * 2.5 + 2 * 2",
      "(+ (* 1.0 2.5) (* 2.0 2.0))"
    )
  }

  private fun assertParse(source: String, expectedAstPrinted: String) {
    val context = InterpretationContext()
    val scanner = Scanner(context, source)
    val tokens: List<Token> = scanner.scanTokens()
    val expression = Parser(context, tokens).parse()
    assertEquals(expectedAstPrinted, expression!!.accept(AstPrinter))
  }
}