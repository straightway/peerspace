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
package straightway.testing.flow

import straightway.general.dsl.*
import kotlin.reflect.KClass

/**
 * An expression which tests the effect of a given lambda object.
 */
interface Effect : Expr

object _throw : StateExpr<Effect>, FunExpr("thrown by", untyped {
    exception: KClass<*>, action: () -> Unit ->
    try { action(); false } catch(e: Throwable) { exception.isInstance(e) } })

val exception = Throwable::class

operator fun StateExpr<Effect>.minus(type: KClass<*>) = BoundExpr(this, Value(type)).inState<Effect>()

infix fun <T: Effect> (() -> Unit).does(op: StateExpr<T>) = BoundExpr(op, Value(this)).inState<T>()

