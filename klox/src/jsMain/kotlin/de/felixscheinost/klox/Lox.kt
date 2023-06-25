package de.felixscheinost.klox

import kotlin.math.nextTowards
import kotlin.math.nextUp
import kotlin.math.sign

external val process: dynamic

fun main() {
  val args = process.argv.slice(2) as Array<String>
  if (args.size > 1) {
    println("Usage: klox [script]")
    process.exit(64)
  } else if (args.size == 1) {
    runFile(args[0])
  }
}

private fun runFile(path: String) {
  val fileText = js("require('fs').readFileSync(path, 'utf8')") as String
  val result = Interpreter().run(fileText)
  result.printErrors()
  if (result.hasSyntaxError) {
    process.exit(65)
  } else if (result.hasRuntimeError) {
    process.exit(70)
  }
}
