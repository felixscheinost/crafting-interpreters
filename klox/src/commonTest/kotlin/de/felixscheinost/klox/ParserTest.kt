package de.felixscheinost.klox

import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
  @Test
  fun basicParserTest() {
    assertParse(
      "1.1 * 2.5",
      "(* 1.1 2.5)"
    )
    assertParse(
      "1.1 * 2.5 * 5.1",
      "(* (* 1.1 2.5) 5.1)"
    )
    assertParse(
      "1.1 * 2.5 + 2.1 * 2.1",
      "(+ (* 1.1 2.5) (* 2.1 2.1))"
    )
    assertParse(
      "1.1 * 2.5 + 2.1 * 2.1, 1.1 + 2.1, \"foo\"",
      "(, (, (+ (* 1.1 2.5) (* 2.1 2.1)) (+ 1.1 2.1)) foo)"
    )
    assertParse(
      """
        "a" 
          ? "ba" ? "bb" : "bc"
          : "c"
      """.trimIndent(),
      "(?: a (?: ba bb bc) c)"
    )
  }

  private fun assertParse(source: String, expectedAstPrinted: String) {
    val context = InterpretationContext()
    val scanner = Scanner(context, source)
    val tokens: List<Token> = scanner.scanTokens()
    val expression = Parser(context, tokens, allowParseLastLineAsExpression = true).parseAsSingleExpression()
    assertEquals(expectedAstPrinted, expression!!.accept(AstPrinter))
  }
}