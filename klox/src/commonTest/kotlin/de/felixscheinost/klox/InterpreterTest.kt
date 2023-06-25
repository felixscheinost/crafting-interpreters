package de.felixscheinost.klox

import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {
  @Test
  fun basicInterpreterTest() {
    assertInterpret(
      "1.1 * 2.5",
      1.1 * 2.5
    )
    assertInterpret(
      "1.1 * 2.5 * 5.1",
      1.1 * 2.5 * 5.1,
    )
    assertInterpret(
      "1.1 * 2.5 + 2.1 * 2.1",
      (1.1 * 2.5) + (2.1 * 2.1)
    )
    assertInterpret(
      "1.1 * 2.5 + 2.1 * 2.1, 1.1 + 2.1, \"foo\"",
      "foo"
    )
    assertInterpret(
      """
        "a" 
          ? "ba" ? "bb" : "bc"
          : "c"
      """.trimIndent(),
      "bb"
    )
    assertInterpret(
      """
        nil
          ? "ba" ? "bb" : "bc"
          : "c"
      """.trimIndent(),
      "c"
    )
    assertInterpret(
      """
        true
          ? false ? "bb" : "bc"
          : "c"
      """.trimIndent(),
      "bc"
    )
    assertInterpret(
      "1 + 2",
      3.0,
    )
    assertInterpret(
      """
        "1" + "2"
      """.trimIndent(),
      "12",
    )
    assertInterpret(
      """
        1 + "2"
      """.trimIndent(),
      "12",
    )
  }

  private fun assertInterpret(source: String, expectedResult: Any?) {
    val result = Interpreter.run(source)
    assertEquals(listOf(), result.syntaxErrors)
    assertEquals(listOf(), result.runtimeErrors)
    assertEquals(expectedResult, result.result)
  }
}