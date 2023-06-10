package de.felixscheinost.klox

object LoxInterpreter {
  fun run(source: String): InterpretationResult {
    val context = InterpretationContext()
    val scanner = Scanner(context, source)
    val tokens: List<Token> = scanner.scanTokens()
    for (token in tokens) {
      println(token)
    }
    return context.toResult()
  }
}