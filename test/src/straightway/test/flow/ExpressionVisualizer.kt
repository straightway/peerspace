/****************************************************************************
Copyright 2016 github.com/straightway

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ****************************************************************************/
package straightway.test.flow

import straightway.general.dsl.BoundExpr
import straightway.general.dsl.Expr
import straightway.general.dsl.StackExprVisitor
import straightway.general.dsl.Value

/**
 * Create a user-friendly string representation of the given expression.
 */
class ExpressionVisualizer(expression: Expr) {
    val string: String by lazy {
        while (reduceStack()) {}
        stack.single().toString()
    }

    private fun reduceStack() : Boolean {
        for ((index, expr) in stack.withIndex()) {
            val potentialArgs = getPotentialArgsForStackIndex(index)
            val reducedExpr = getReducedExpression(expr, potentialArgs) ?: continue
            reduceStackAt(index, reducedExpr)
            return true
        }

        return false
    }

    private fun getReducedExpression(expr: Expr, potentialArgs: List<Expr>) =
        when {
            expr.arity == 0 -> null
            expr.arity == 1 && 0 < potentialArgs.single().arity -> BoundExpr(expr, potentialArgs.single())
            potentialArgs.all { it.arity == 0 } -> Value(getStringRepresentation(expr, potentialArgs))
            else -> null
        }

    private fun getStringRepresentation(expr: Expr, args: List<Expr>) =
        when (expr.arity) {
            2 -> "${args[0]} $expr ${args[1]}"
            else -> "$expr(${args.joinToString()})"
        }

    private fun getPotentialArgsForStackIndex(index: Int) = stack.drop(index + 1).take(stack[index].arity)

    private fun reduceStackAt(index: Int, reducedExpr: Expr) {
        val exprArityAtIndex = stack[index].arity
        stack = stack.take(index) + reducedExpr + stack.takeLast(stack.size - index - exprArityAtIndex - 1)
    }

    private var stack : List<Expr>

    init {
        val stackExprVisitor = StackExprVisitor()
        expression.accept { stackExprVisitor.visit(it) }
        stack = stackExprVisitor.stack
    }
}

/*
class ExpressionVisualizer(private val expression: Expr) {
    val string: String by lazy {
        expression.accept { push(it); tryReduceOpStack() }
        fillUnboundArguments()
        vals.joinToString()
    }

    private fun fillUnboundArguments() {
        while (ops.any()) {
            vals += Value("_")
            tryReduceOpStack()
        }
    }

    private fun tryReduceOpStack() {
        if (!isLastOpReducible) return
        val arity = lastOp!!.arity
        val opChain: String = popLastChainedOperators()
        val params = popVals(arity)
        push(Value(getOpWithParamsString(opChain, params)))
    }

    private fun getOpWithParamsString(opString: String, params: List<Expr>) =
        when (params.size) {
            2 -> "${params[0]} $opString ${params[1]}"
            else -> "$lastOp(${params.joinToString()})"
        }

    private fun popLastChainedOperators(): String {
        var opChainString: String = ""
        var link = ""
        do {
            val op = popOp() ?: break
            opChainString = "$op$link$opChainString"
            link = "-"
        } while (lastOp?.arity == 1)

        return opChainString
    }

    private fun popOp() : Expr? {
        val result = lastOp
        if (result != null) ops = ops.dropLast(1)
        return result
    }

    private fun popVals(n: Int) : List<Expr> {
        if (vals.isEmpty()) return listOf()
        val result = vals.takeLast(n)
        vals = vals.dropLast(n)
        return result
    }

    private fun push(it: Expr) {
        when (it.arity) {
            0 -> vals += it
            else -> ops += it
        }
    }

    private var ops = listOf<Expr>()
    private var vals = listOf<Expr>()
    private val lastOp get() = ops.lastOrNull()
    private val isLastOpReducible : Boolean get() {
        val lastOp = this.lastOp
        return lastOp != null && lastOp.arity <= vals.size
    }
}*/