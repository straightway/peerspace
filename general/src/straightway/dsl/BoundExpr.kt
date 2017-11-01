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
package straightway.dsl

/**
 * Expression consisting of two sub expressions bound together, i.e. the binding
 * expression is using the result of the bound expression as first argument.
 * The resulting expression takes the arguments of the bound expression and
 * the remaining arguments of the binding expression.
 */
class BoundExpr(private val binding: Expr, private val bound: Expr) : Expr
{
    override fun invoke(vararg params: Any): Any {
        val boundFunParams = params.take(bound.arity).toTypedArray()
        val boundValue = bound(*boundFunParams)
        val bindingFunParams = (listOf(boundValue) + params.drop(bound.arity)).toTypedArray()
        return binding(*bindingFunParams)
    }

    override val arity: Int by lazy { binding.arity + bound.arity - 1 }
    override fun accept(visitor: (Expr) -> Unit) {
        binding.accept(visitor)
        bound.accept(visitor)
    }

    override fun toString() = "$binding-$bound"

    init {
        assert(1 <= binding.arity) { "Binding expression must have parameters (arity: $arity)" }
    }
}
