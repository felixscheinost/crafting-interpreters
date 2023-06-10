package de.felixscheinost.klox

import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.system.exitProcess

object Lox {

  @JvmStatic
  fun main(args: Array<String>) {
    if (args.size > 1) {
      println("Usage: klox [script]")
      exitProcess(64)
    } else if (args.size == 1) {
      runFile(args[0])
    } else {
      runPrompt()
    }
  }

  private fun runFile(path: String) {
    val result = LoxInterpreter.run(Paths.get(path).readText())
    result.printErrors()
    if (result.hasError) {
      exitProcess(65)
    }
  }

  private fun runPrompt() {
    while (true) {
      print("> ")
      val line = readlnOrNull() ?: break
      LoxInterpreter.run(line).printErrors()
    }
  }
}