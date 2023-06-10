package de.felixscheinost.klox

import de.felixscheinost.klox.TokenType.*
import kotlin.test.assertEquals
import kotlin.test.Test

class ScannerTest {
  @Test
  fun testScanner() {
    assertTokenTypes("()", LEFT_PAREN, RIGHT_PAREN, EOF)
    assertTokenTypes("", EOF)
    assertTokenTypes(
      "var foo = 2 + 2",
      VAR, IDENTIFIER, EQUAL, NUMBER, PLUS, NUMBER, EOF
    )
    assertTokenTypes(
      """
        // Multiline string
        var foo = "asf
           asfasf asfasf"
      """.trimIndent(),
      VAR, IDENTIFIER, EQUAL, STRING, EOF
    )
    assertTokenTypes(
      """
        /* This is a block comment */
        var foo = "asf
           asfasf asfasf"
      """.trimIndent(),
      VAR, IDENTIFIER, EQUAL, STRING, EOF
    )
    assertTokenTypes(
      """
        /* This is a block comment */
        var foo = "asf
           asfasf asfasf"
        /* This is an unterminated block comment which works
        
          asfasf
          asfasf
      """.trimIndent(),
      VAR, IDENTIFIER, EQUAL, STRING, EOF
    )
  }

  private fun assertTokenTypes(source: String, vararg expected: TokenType) {
    assertEquals(
      expected.toList(),
      Scanner(InterpretationContext(), source).scanTokens().map { it.type }
    )
  }
}