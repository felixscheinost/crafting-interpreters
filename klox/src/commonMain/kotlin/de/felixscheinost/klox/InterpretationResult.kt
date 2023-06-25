package de.felixscheinost.klox

class InterpretationResult(
  // Only filled when evaluating single expression
  val result: Any?,
  val syntaxErrors: List<LoxSyntaxError>,
  val runtimeErrors: List<LoxRuntimeError>
) {
  val hasSyntaxError: Boolean
    get() = syntaxErrors.isNotEmpty()

  val hasRuntimeError: Boolean
    get() = runtimeErrors.isNotEmpty()
}