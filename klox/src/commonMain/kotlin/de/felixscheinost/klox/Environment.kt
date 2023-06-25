package de.felixscheinost.klox

class Environment(
  private val enclosing: Environment? = null
) {

  private val values: MutableMap<String, Any?> = mutableMapOf()

  fun define(name: String, value: Any?) = values.put(name, value)

  fun get(name: Token): Any? {
    if (values.containsKey(name.lexeme)) {
      return values[name.lexeme]
    }
    if (enclosing != null) {
      return enclosing.get(name)
    }
    throw LoxRuntimeError(name, "Undefined variable '${name.lexeme}'.")
  }

  fun assign(name: Token, value: Any?) {
    if (values.containsKey(name.lexeme)) {
      values[name.lexeme] = value
      return
    }
    if (enclosing != null) {
      return enclosing.assign(name, value)
    }
    throw LoxRuntimeError(name, "Undefined variable '" + name.lexeme + "'.")
  }
}