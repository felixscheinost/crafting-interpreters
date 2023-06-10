package de.felixscheinost.klox

import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.system.exitProcess

object Lox {

  private var hadError: Boolean = false

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
    run(Paths.get(path).readText())
    // Indicate an error in the exit code.
    if (hadError) {
      exitProcess(65)
    }
  }

  private fun runPrompt() {
    while (true) {
      print("> ")
      val line = readlnOrNull() ?: break
      run(line)
      hadError = false
    }
  }

  private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens: List<Token> = scanner.scanTokens()
    for (token in tokens) {
      println(token)
    }
  }

  fun error(line: Int, message: String) {
    report(line, "", message)
  }

  private fun report(line: Int, where: String, message: String) {
    System.err.println("[line $line] Error$where: $message")
    hadError = true
  }
}