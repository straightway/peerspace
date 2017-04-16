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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import straightway.general.Panic

class DistributedExprTest {

    @Test fun arity() {
        val sut = DistributedExpr("collector", odd, big) { _ -> 83 }
        assertEquals(1, sut.arity)
    }

    @Test fun callsFunctor() {
        val sut = DistributedExpr("collector", odd, big) { _ -> 83 } - 1
        assertEquals(83, sut())
    }

    @Test fun construction_withDifferentArityLeftAndRight_throws() =
        assertThrows<Panic>(Panic::class.java) {
            DistributedExpr("invalid", Value("arity0"), odd) { _ -> 83 }
        }

    @Test fun hasAccessToDistributionTargets() {
        val sut = DistributedExpr("collector", odd, big) { _ ->
            assertEquals(odd, left)
            assertEquals(big, right)
            83 }
        assertEquals(83, sut())
    }

    @Test fun accept_doesNotDescend() {
        val sut = big and odd
        val visitor = StackExprVisitor()
        sut.accept { visitor.visit(it) }
        assertEquals(listOf(sut), visitor.stack)
    }

    @Test fun toString_yieldsInfixNotations() =
        assertEquals("big and odd", (big and odd).toString())

    @Test fun toString_withThreeDistributedExpression_yieldsSuffixNotations() =
            assertEquals("big and odd and notTooBig", (big and odd and notTooBig).toString())

    @Test fun useCase_logicalAnd() {
        assertFalse(((big and odd)-1)() as Boolean)
        assertFalse(((big and odd)-2)() as Boolean)
        assertFalse(((big and odd)-12)() as Boolean)
        assertTrue(((big and odd)-13)() as Boolean)
    }
}

private operator fun Expr.minus(e: Expr) = BoundExpr(this, e)
private operator fun Any.minus(e: Expr) = BoundExpr(e, Value(this))
private operator fun Expr.minus(v: Any) = BoundExpr(this, Value(v))

private infix fun Expr.and(other: Expr) =
    DistributedExpr("and", this, other) { args -> left(*args) as Boolean && right(*args) as Boolean }
private val odd = FunExpr("odd", untyped<Int, Boolean> { a -> a % 2 != 0 })
private val big = FunExpr("big", untyped<Int, Boolean> { a -> a > 10 })
private val notTooBig = FunExpr("notTooBig", untyped<Int, Boolean> { a -> 100 < a })
