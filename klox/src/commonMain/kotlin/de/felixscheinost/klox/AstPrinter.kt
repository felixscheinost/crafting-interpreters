package de.felixscheinost.klox

object AstPrinter : Expr.Visitor<String> {
  override fun visitAssignExpr(name: Token, value: Expr) = "${name.lexeme} = ${value.accept(this)}"

  override fun visitBinaryExpr(left: Expr, operator: Token, right: Expr): String = parenthesize(operator.lexeme, left, right)

  override fun visitGroupingExpr(expr: Expr): String = parenthesize("group", expr)

  override fun visitLiteralExpr(value: Any?): String = value?.toString() ?: "nil"

  override fun visitUnaryExpr(operator: Token, right: Expr): String = parenthesize(operator.lexeme, right)

  override fun visitTernaryExpr(condition: Expr, left: Expr, right: Expr) = parenthesize("?:", condition, left, right)

  override fun visitVariableExpr(name: Token) = "var ${name.lexeme}"

  private fun parenthesize(name: String, vararg exprs: Expr) = buildString {
    append("(")
    append(name)
    for (expr in exprs) {
      append(" ")
      append(expr.accept(this@AstPrinter))
    }
    append(")")
  }
}