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
 * An expression associated with a type used as a state. The type itself is not
 * instantiated, it is used to be able to control binding of functions at compile t.
 */
@Suppress("unused")
interface StateExpr<TState> : Expr

fun <T> Expr.inState() : StateExpr<T> = StateExprImpl<T>(this)

private class StateExprImpl<TState>(private val wrapped: Expr) : StateExpr<TState>, Expr by wrapped {
    override fun toString() = wrapped.toString()
}