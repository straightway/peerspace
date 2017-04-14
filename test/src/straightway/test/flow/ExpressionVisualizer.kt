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

    private fun reduceStack() : Boolean =
        stack.indices.any(this::reduceStackAt)

    private fun reduceStackAt(index: Int): Boolean {
        val potentialArgs = getPotentialArgsForStackIndex(index)
        val reducedExpr = getReducedExpression(stack[index], potentialArgs) ?: return false
        reduceStackAt(index, reducedExpr)
        return true
    }

    private fun getPotentialArgsForStackIndex(index: Int) =
        stack.drop(index + 1).take(stack[index].arity)

    private fun reduceStackAt(index: Int, reducedExpr: Expr) {
        val exprArityAtIndex = stack[index].arity
        stack = stack.take(index) + reducedExpr + stack.drop(index + exprArityAtIndex + 1)
    }

    private var stack : List<Expr>

    init {
        val stackExprVisitor = StackExprVisitor()
        expression.accept { stackExprVisitor.visit(it) }
        stack = stackExprVisitor.stack
    }
}

private fun getReducedExpression(expr: Expr, potentialArgs: List<Expr>) =
    when {
        expr.isPlainValue() -> null
        potentialArgs.isChainedOperation(expr) -> BoundExpr(expr, potentialArgs.single())
        potentialArgs.all(Expr::isPlainValue) -> Value(getStringRepresentation(expr, potentialArgs))
        else -> null
    }

private fun Expr.isPlainValue() =
    arity == 0

private fun List<Expr>.isChainedOperation(expr: Expr) =
    expr.arity == 1 && 0 < single().arity

private fun getStringRepresentation(expr: Expr, args: List<Expr>) =
    getFilledArgsForExpression(args, expr).let {
        when (expr.arity) {
            2 -> "${it[0]} $expr ${it[1]}"
            else -> "$expr(${it.joinToString()})"
        }
    }

private fun getFilledArgsForExpression(args: List<Expr>, expr: Expr) =
    args + List<Expr>(expr.arity - args.size) { _ -> Value("?") }
