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
package straightway.general.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BoundExprTest {

    @BeforeEach fun setup() { calls = 0 }

    @Test fun bindingValue_reducesArityByOne()
        = assertEquals(1, (BoundExpr(exprArity2, Value(2))).arity)
    @Test fun bindingUnaryExpression_preservesArity()
        = assertEquals(2, (BoundExpr(exprArity2, exprArity1)).arity)
    @Test fun bindingBinaryExpression_incrementsArity()
        = assertEquals(3, (BoundExpr(exprArity2, otherExprArity2)).arity)
    @Test fun bindingToValue_throws()
    { assertThrows<AssertionError>(AssertionError::class.java) { BoundExpr(Value(2), otherExprArity2) } }

    @Test fun invoke_withTooFewParametersThrows() {
        val binding = fun1 { -it }
        val bound = fun1 { -it }
        val sut = BoundExpr(binding, bound)
        assertThrows<AssertionError>(AssertionError::class.java) { sut() }
    }

    @Test fun invoke_withTooManyParametersThrows() {
        val binding = fun1 { -it }
        val bound = fun1 { -it }
        val sut = BoundExpr(binding, bound)
        assertThrows<AssertionError>(AssertionError::class.java) { sut(1, 2) }
    }

    @Test fun invoke_forBoundValue_yieldsCallResultOfHigherArityExpression() {
        var calls = 0
        val binding = FunExpr<Int>("fun") { calls++; -it }
        val bound = Value(3)
        val sut = BoundExpr(binding, bound)

        val result = sut()

        assertEquals(-3, result)
        assertEquals(1, calls)
    }

    @Test fun invoke_forIndirectlyBoundValue_yieldsCallResultOfBothExpression() {
        var calls = 0
        val binding = FunExpr<Int>("fun") { calls++; -it }
        val bound1 = FunExpr<Int>("bound") { calls++; it * 2 }
        val bound2 = Value(3)
        val sut = BoundExpr(binding, BoundExpr(bound1, bound2))

        val result = sut()

        assertEquals(-6, result)
        assertEquals(2, calls)
    }

    @Test fun invoke_distributesParameters() {
        var calls = 0
        val binding = FunExpr<Int, Int>("fun") { a, b -> calls++; a - b }
        val bound1 = FunExpr<Int>("bound") { calls++; it * 2 }
        val sut = BoundExpr(binding, bound1)

        val result = sut(3, 1)

        assertEquals(5, result)
        assertEquals(2, calls)
    }

    @Test fun accept_traversesBoundFirst() {
        val bound = Value(2)
        val sut = BoundExpr(exprArity1, bound)
        val visitor = StackExprVisitor()
        sut.accept { visitor.visit(it) }
        assertEquals(listOf(exprArity1, bound), visitor.stack)
    }

    @Test fun accept_visitsBoundExpressionDepthFirst() {
        val sub1 = FunExpr<Int, Int>("Sub1") { a, _ -> a }
        val sub1sub1 = Value("Sub1Sub1")
        val sub1sub2 = Value("Sub1Sub2")
        val sub1Bound = BoundExpr(BoundExpr(sub1, sub1sub1), sub1sub2)

        val sub2 = FunExpr<Int, Int>("Sub2") { a, _ -> a }
        val sub2sub1 = Value("Sub2Sub1")
        val sub2sub2 = Value("Sub2Sub2")
        val sub2Bound = BoundExpr(BoundExpr(sub2, sub2sub1), sub2sub2)

        val top = FunExpr<Int, Int>("Top") { a, _ -> a }
        val topBound = BoundExpr(BoundExpr(top, sub1Bound), sub2Bound)

        val visitor = StackExprVisitor()
        topBound.accept { visitor.visit(it) }

        assertEquals(listOf(top, sub1, sub1sub1, sub1sub2, sub2, sub2sub1, sub2sub2), visitor.stack)
    }

    @Test fun toString_yieldsExpectedResult()
        = assertEquals("fun2-fun1-3-2", (BoundExpr(BoundExpr(BoundExpr(exprArity2, exprArity1), Value(3)), Value(2))).toString())

    private fun fun1(name: String = "fun1", compute: (Int) -> Int = { it })
        = FunExpr<Int>(name) { calls++; compute(it) }
    private fun fun2(name: String = "fun2", compute: (Int, Int) -> Int = { a, b -> a - b })
        = FunExpr<Int, Int>(name) { a, b -> calls++; compute(a, b) }

    private var calls = 0
    private val exprArity1 = fun1()
    private val exprArity2 = fun2()
    private val otherExprArity2 = fun2("otherFun2")
}