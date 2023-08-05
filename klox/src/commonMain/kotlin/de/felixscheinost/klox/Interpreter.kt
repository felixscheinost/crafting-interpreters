package de.felixscheinost.klox

class Interpreter(
  private val allowParseLastLineAsExpression: Boolean = false,
) : Expr.Visitor<Any?>, Stmt.Visitor<Any?> {

  private var environment = Environment()

  fun run(source: String): InterpretationResult {
    val context = InterpretationContext()
    val scanner = Scanner(context, source)
    val tokens: List<Token> = scanner.scanTokens()
    val statements = Parser(context, tokens, allowParseLastLineAsExpression = allowParseLastLineAsExpression).parse()
    if (context.hasSyntaxError) {
      return context.toResult()
    }
    try {
      var lastResult: Any? = null
      statements.forEach { statement ->
        lastResult = statement.accept(this)
      }
      return context.toResult(lastResult)
    } catch (error: LoxRuntimeError) {
      context.runtimeError(error)
    }
    return context.toResult()
  }

  fun stringify(obj: Any?): String {
    if (obj == null) {
      return "nil"
    }
    return if (obj is Double) {
      // Workaround for JS: "-0.0".toString() returns "0"
      if (obj == 0.0 && 1 / obj != Double.POSITIVE_INFINITY) {
        return "-0"
      }
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

  override fun visitBlockStmt(statements: List<Stmt>): Any? {
    return executeBlock(statements, Environment(environment))
  }

  override fun visitAssignExpr(name: Token, value: Expr): Any? {
    val evaluatedValue = value.accept(this)
    environment.assign(name, evaluatedValue)
    return evaluatedValue
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
        } else if (leftValue is String && rightValue is String) {
          stringify(leftValue) + stringify(rightValue)
        } else {
          throw LoxRuntimeError(operator, "Operands must be two numbers or two strings.")
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

  override fun visitLogicalExpr(left: Expr, operator: Token, right: Expr): Any? {
    val leftValue = left.accept(this)
    if (operator.type == TokenType.OR) {
      if (isTruthy(leftValue)) {
        return leftValue
      }
    } else {
      if (!isTruthy(leftValue)) {
        return leftValue
      }
    }
    return right.accept(this)
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
        !isTruthy(rightValue)

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

  override fun visitVariableExpr(name: Token): Any? {
    return environment.get(name)
  }

  override fun visitExpressionStmt(expression: Expr): Any? {
    return expression.accept(this)
  }

  override fun visitPrintStmt(expression: Expr) {
    println(stringify(expression.accept(this)))
  }

  override fun visitVarStmt(name: Token, initializer: Expr?) {
    environment.define(name.lexeme, initializer?.accept(this))
  }

  override fun visitWhileStmt(condition: Expr, body: Stmt) {
    while (isTruthy(condition.accept(this))) {
      body.accept(this)
    }
  }

  override fun visitIfStmt(condition: Expr, thenBranch: Stmt, elseBranch: Stmt?) {
    if (isTruthy(condition.accept(this))) {
      thenBranch.accept(this)
    } else {
      elseBranch?.accept(this)
    }
  }

  private fun isTruthy(value: Any?) = value != null && value != false

  // Kotlin `==` behaves as `isEqual` is defined in the book
  private fun isEqual(a: Any?, b: Any?) = a == b

  private fun executeBlock(statements: List<Stmt>, environment: Environment): Any? {
    val previous = this.environment
    try {
      this.environment = environment
      var last: Any? = null
      statements.forEach {
        last = it.accept(this)
      }
      return last
    } finally {
      this.environment = previous
    }
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun checkNumberOperand(operator: Token, operand: Any?) = operand as? Double ?: throw LoxRuntimeError(operator, "Operand must be a number.")

  @Suppress("NOTHING_TO_INLINE")
  private inline fun checkNumberOperands(operator: Token, left: Any?, right: Any?) = if (left is Double && right is Double) left to right else throw LoxRuntimeError(operator, "Operands must be numbers.")
}