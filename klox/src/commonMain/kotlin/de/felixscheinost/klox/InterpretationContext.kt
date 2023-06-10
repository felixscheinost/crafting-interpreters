package de.felixscheinost.klox

class InterpretationContext(
  private val errors: MutableList<LoxError> = mutableListOf()
) {
  fun toResult() = InterpretationResult(
    errors = errors.toList()
  )

  fun error(line: Int, message: String) {
    errors.add(LoxError(line = line, message = message))
  }
}