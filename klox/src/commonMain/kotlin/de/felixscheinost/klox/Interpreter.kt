package de.felixscheinost.klox

object Interpreter : Expr.Visitor<Any?> {

  fun run(source: String): InterpretationResult {
    val context = InterpretationContext()
    val scanner = Scanner(context, source)
    val tokens: List<Token> = scanner.scanTokens()
    val expression = Parser(context, tokens).parse()
    if (context.hasSyntaxError) {
      return context.toResult()
    }
    if (expression != null) {
      try {
        val value: Any? = expression.accept(Interpreter)
        return context.toResult(value)
      } catch (error: LoxRuntimeError) {
        context.runtimeError(error)
      }
    }
    return context.toResult()
  }

  fun stringify(obj: Any?): String {
    if (obj == null) {
      return "nil"
    }
    return if (obj is Double) {
      val text = obj.toString()
      if (text.endsWith(".0")) {
        text.dropLast(2)
      } else {
        text
      }
    } else {
      obj.toString()
    }
  }

  override fun visitBinaryExpr(left: Expr, operator: Token, right: Expr): Any? {
    val leftValue = left.accept(this)
    val rightValue = right.accept(this)
    return when (operator.type) {
      TokenType.MINUS ->
        checkNumberOperands(operator, leftValue, rightValue).let { (a, b) ->
          a - b
        }

      TokenType.PLUS ->
        if (leftValue is Double && rightValue is Double) {
          leftValue + rightValue
        } else if (leftValue is String || rightValue is String) {
          stringify(leftValue) + stringify(rightValue)
        } else {
          throw LoxRuntimeError(operator, "Operands must be two numbers or at least one string.")
        }

      TokenType.SLASH ->
        checkNumberOperands(operator, leftValue, rightValue).let { (a, b) ->
          if (b == 0.0) {
            throw LoxRuntimeError(operator, "Division by zero.")
          }
          a / b
        }

      TokenType.STAR ->
        checkNumberOperands(operator, leftValue, rightValue).let { (a, b) ->
          a * b
        }

      TokenType.GREATER ->
        checkNumberOperands(operator, leftValue, rightValue).let { (a, b) ->
          a > b
        }

      TokenType.GREATER_EQUAL ->
        checkNumberOperands(operator, leftValue, rightValue).let { (a, b) ->
          a >= b
        }

      TokenType.LESS ->
        checkNumberOperands(operator, leftValue, rightValue).let { (a, b) ->
          a < b
        }

      TokenType.LESS_EQUAL ->
        checkNumberOperands(operator, leftValue, rightValue).let { (a, b) ->
          a <= b
        }

      TokenType.BANG_EQUAL ->
        !isEqual(leftValue, rightValue)

      TokenType.EQUAL_EQUAL ->
        isEqual(leftValue, rightValue)

      TokenType.COMMA ->
        rightValue

      else ->
        error("Unhandled operator ${operator.type}")
    }
  }

  override fun visitGroupingExpr(expr: Expr): Any? {
    return expr.accept(this)
  }

  override fun visitLiteralExpr(value: Any?): Any? {
    return value
  }

  override fun visitUnaryExpr(operator: Token, right: Expr): Any {
    val rightValue = right.accept(this)
    return when (operator.type) {
      TokenType.MINUS ->
        -checkNumberOperand(operator, rightValue)

      TokenType.BANG ->
        return !isTruthy(right)

      else ->
        error("Unhandled operator ${operator.type}")
    }
  }

  override fun visitTernaryExpr(condition: Expr, left: Expr, right: Expr): Any? {
    return if (isTruthy(condition.accept(this))) {
      left.accept(this)
    } else {
      right.accept(this)
    }
  }

  private fun isTruthy(value: Any?) = value != null && value != false

  // Kotlin `==` behaves as `isEqual` is defined in the book
  private fun isEqual(a: Any?, b: Any?) = a == b

  @Suppress("NOTHING_TO_INLINE")
  private inline fun checkNumberOperand(operator: Token, operand: Any?) = operand as? Double ?: throw LoxRuntimeError(operator, "Operand must be a number.")

  @Suppress("NOTHING_TO_INLINE")
  private inline fun checkNumberOperands(operator: Token, left: Any?, right: Any?) = if (left is Double && right is Double) left to right else throw LoxRuntimeError(operator, "Operands must be numbers.")
}