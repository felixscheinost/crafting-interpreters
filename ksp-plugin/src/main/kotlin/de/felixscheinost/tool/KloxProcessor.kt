package de.felixscheinost.tool

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated

class KloxProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger
) : SymbolProcessor {

  companion object {
    private val EXPRESSIONS = listOf(
      "Assign" to listOf("name: Token", "value: Expr"),
      "Binary" to listOf("left: Expr", "operator: Token", "right: Expr"),
      "Grouping" to listOf("expr: Expr"),
      "Literal" to listOf("value: Any?"),
      "Unary" to listOf("operator: Token", "right: Expr"),
      "Ternary" to listOf("condition: Expr", "left: Expr", "right: Expr"),
      "Variable" to listOf("name: Token"),
    )
    private val STATEMENTS = listOf(
      "Block" to listOf("statements: List<Stmt>"),
      "Expression" to listOf("expression: Expr"),
      "Print" to listOf("expression: Expr"),
      "Var" to listOf("name: Token", "initializer: Expr?"),
    )
  }

  override fun process(resolver: Resolver): List<KSAnnotated> {
    // TODO: Is there a better way than this `if`?
    //       - KSP runs multiple times: It runs recursively for the files created by our processor
    //         => When having multiple processors files can also be "deferred"
    //       - When we try to do a `createNewFile` multiple times we get an exception
    //       - When using `resolver.getSymbolsWithAnnotation` this problem isn't present as `getSymbolsWithAnnotation` only returns symbols from new files
    //         => Already handled correctly in KSP. We need to do the same.
    fun defineAst(baseName: String, types: List<Pair<String, List<String>>>) {
      if (resolver.getAllFiles().none { it.fileName == "$baseName.kt" }) {
        codeGenerator.createNewFile(Dependencies(false), "de.felixscheinost.klox", baseName).bufferedWriter().use { bufferedWriter ->
          bufferedWriter.appendLine("package de.felixscheinost.klox")
          bufferedWriter.appendLine()
          bufferedWriter.appendLine("sealed class $baseName {")
          bufferedWriter.appendLine()
          bufferedWriter.appendLine("  abstract fun <T> accept(visitor: Visitor<T>): T")
          bufferedWriter.appendLine()
          types.forEach { (name, args) ->
            bufferedWriter.appendLine("  class $name(${args.joinToString(", ") { "val $it" }}) : $baseName() {")
            bufferedWriter.appendLine("    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit${name}$baseName(${args.joinToString(", ") { it.split(": ")[0] }})")
            bufferedWriter.appendLine("  }")
            bufferedWriter.appendLine()
          }
          bufferedWriter.appendLine("  interface Visitor<T> {")
          types.forEach { (name, args) ->
            bufferedWriter.appendLine("    fun visit${name}$baseName(${args.joinToString(", ")}): T")
          }
          bufferedWriter.appendLine("  }")
          bufferedWriter.appendLine("}")
        }
      }
    }
    defineAst("Expr", EXPRESSIONS)
    defineAst("Stmt", STATEMENTS)
    return listOf()
  }
}