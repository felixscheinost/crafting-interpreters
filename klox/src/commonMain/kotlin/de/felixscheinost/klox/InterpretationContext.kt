package de.felixscheinost.klox

class InterpretationContext(
  private val syntaxErrors: MutableList<LoxSyntaxError> = mutableListOf(),
  private val runtimeErrors: MutableList<LoxRuntimeError> = mutableListOf()
) {
  val hasSyntaxError: Boolean
    get() = syntaxErrors.isNotEmpty()

  val hasRuntimeError: Boolean
    get() = runtimeErrors.isNotEmpty()

  fun toResult(result: Any? = null) = InterpretationResult(
    result = result,
    syntaxErrors = syntaxErrors.toList(),
    runtimeErrors = runtimeErrors.toList()
  )

  fun syntaxError(line: Int, message: String, where: String = "") {
    syntaxErrors.add(LoxSyntaxError(line = line, message = message, where = where))
  }

  fun runtimeError(error: LoxRuntimeError) {
    runtimeErrors.add(error)
  }
}