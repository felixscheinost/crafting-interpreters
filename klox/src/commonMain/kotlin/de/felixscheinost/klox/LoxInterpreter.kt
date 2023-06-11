package de.felixscheinost.klox

object LoxInterpreter {
  fun run(source: String): InterpretationResult {
    val context = InterpretationContext()
    val scanner = Scanner(context, source)
    val tokens: List<Token> = scanner.scanTokens()
    val expression = Parser(context, tokens).parse()
    val result = context.toResult()
    if (!result.hasError && expression != null) {
      println(expression.accept(AstPrinter))
    }
    return result
  }
}