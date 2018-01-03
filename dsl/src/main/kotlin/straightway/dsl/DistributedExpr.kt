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

import straightway.error.Panic

/**
 * An expression which calls a functor and is able to access two sub expressions of
 * the same arity (named left and right). The arity of the distributed expression is
 * equal to the arity of the sub expression.
 */
class DistributedExpr(
    private val name: String,
    val left: Expr,
    val right: Expr,
    private val functor: DistributedExpr.(Array<out Any>) -> Any) : Expr
{
    override val arity: Int get() = left.arity
    override fun invoke(vararg params: Any): Any = functor(params)
    override fun toString() = "$left $name $right"

    init {
        if (left.arity != right.arity)
            throw Panic(
                    "Different arity for left (${left.arity}) and " +
                    "right (${right.arity}) distributed expression")
    }
}
