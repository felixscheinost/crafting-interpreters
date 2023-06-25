package de.felixscheinost.klox

fun InterpretationResult.printErrors() {
  syntaxErrors.forEach {
    console.error("[line ${it.line}] Error${it.where}: ${it.message}")
  }
  runtimeErrors.forEach {
    console.error("${it.message}\n[line " + it.token.line + "]")
  }
}