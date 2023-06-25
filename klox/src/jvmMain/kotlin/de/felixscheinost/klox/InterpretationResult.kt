package de.felixscheinost.klox

fun InterpretationResult.printErrors() {
  syntaxErrors.forEach {
    System.err.println("[line ${it.line}] Error${it.where}: ${it.message}")
  }
  runtimeErrors.forEach {
    System.err.println("${it.message}\n[line " + it.token.line + "]")
  }
}