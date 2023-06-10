package de.felixscheinost.klox

fun InterpretationResult.printErrors() {
  errors.forEach {
    System.err.println("[line ${it.line}] Error: ${it.message}")
  }
}