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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StateExprTest {

    @Test fun delegates_arity() {
        val wrapped = ExprMock()
        val sut: Expr = wrapped.inState<State>()
        assertEquals(2, sut.arity)
        assertEquals(1, wrapped.arityCalls)
    }

    @Test fun delegates_invoke() {
        val wrapped = ExprMock()
        val sut: Expr = wrapped.inState<State>()
        val invokeParams = listOf(5, 7)
        assertEquals(83, sut(*invokeParams.toTypedArray()))
        assertEquals(1, wrapped.invokeCalls)
        assertEquals(invokeParams, wrapped.lastInvokeArgs)
    }

    @Test fun toString_yieldsWrappedToString()
    = assertEquals("Hello", Value("Hello").inState<State>().toString())

    private object State

    private class ExprMock : Expr {
        var arityCalls = 0
        override val arity: Int get() { arityCalls++; return 2 }

        var invokeCalls = 0
        var lastInvokeArgs = listOf<Any>()
        override fun invoke(vararg params: Any): Any {
            invokeCalls++
            lastInvokeArgs = params.toList()
            return 83
        }
    }
}