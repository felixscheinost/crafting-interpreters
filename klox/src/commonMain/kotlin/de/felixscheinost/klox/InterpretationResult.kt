package de.felixscheinost.klox

class InterpretationResult(
  val errors: List<LoxError>
) {
  val hasError: Boolean
    get() = errors.isNotEmpty()
}