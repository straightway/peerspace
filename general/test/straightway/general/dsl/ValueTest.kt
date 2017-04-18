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

class ValueTest {

    @Test fun invocation_returnsValue() {
        val sut = Value(testValue)
        assertSame(testValue, sut())
    }

    @Test fun invocation_withParametersThrows() {
        val sut = Value(testValue)
        assertThrows<AssertionError>(AssertionError::class.java) { sut(1) }
    }

    @Test fun toString_returnsWrappedValueStringRepresentation()
    {
        val sut = Value(83)
        assertEquals("83", sut.toString())
    }

    @Test fun toString_returnsArrayElements() {
        val sut = Value(arrayOf(1, 2, 3))
        assertEquals("[1, 2, 3]", sut.toString())
    }

    @Test fun toString_returnsPlainString() {
        val sut = Value("123")
        assertEquals("123", sut.toString())
    }

    @Test fun toString_returnsSequenceElements() {
        val sut = Value("123".asSequence())
        assertEquals("[1, 2, 3]", sut.toString())
    }

    @Test fun toString_returnsCharSequenceElements() {
        val sut = Value(sequenceOf('1', '2', '3'))
        assertEquals("[1, 2, 3]", sut.toString())
    }

    @Test fun hasArity0() {
        val sut = Value(83)
        assertEquals(0, sut.arity)
    }

    @Test fun isDirectlyVisited() {
        val sut = Value(83)
        val visitor = StackExprVisitor()
        sut.accept { visitor.visit(it) }
        assertEquals(listOf(sut), visitor.stack)
    }

    private object testValue
}