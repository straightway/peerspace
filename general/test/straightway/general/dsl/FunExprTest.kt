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

class FunExprTest {

    @BeforeEach fun setup() {
        calls = 0
        sut = defaultSut
    }

    @Test fun arity() = assertEquals(1, sut.arity)
    @Test fun name() = assertEquals("name", sut.name)
    @Test fun functor() = assertEquals(-1, testInvoke(1))
    @Test fun functor_withTooManyArguments_throws()
    { assertThrows<AssertionError>(AssertionError::class.java) { sut(1, 2) } }
    @Test fun construction_withPrimaryConstructor()
        = testConstructionFromFunction(FunExpr(1, "name") { testFunctor(it[0]) }, 2, 2)
    @Test fun construction_fromFunction0()
        = testConstructionFromFunction(DerivedFun0("name", { testFunctor(83) }), 83)
    @Test fun construction_fromTypedFunction0()
        = testConstructionFromFunction(DerivedFun0("name", { testFunctor(83) as Int }), 83)
    @Test fun construction_fromUnypedFunction1()
        = testConstructionFromFunction(DerivedFun1("name") { a: Any -> testFunctor(a) }, 2, 2)
    @Test fun construction_fromTypedFunction1()
        = testConstructionFromTypedFunction(FunExpr("name") { a: Int -> testFunctor(a) }, 2, 2)
    @Test fun construction_fromTypedFunction2()
        = testConstructionFromTypedFunction(FunExpr("name") { a: Int, _: Int -> testFunctor(a + 2) }, 3, 1, 2)
    @Test fun construction_fromUnypedFunction2()
        = testConstructionFromFunction(DerivedFun2("name") { a: Any, _: Any -> testFunctor(a as Int + 2) }, 3, 1, 2)
    @Test fun construction_withArityLessThan0Throws()
    { assertThrows<AssertionError>(AssertionError::class.java) { FunExpr(-1, "name") { it } } }

    @Test fun toString_yieldsName() = assertEquals(sut.name, sut.toString())

    private class DerivedFun0(name: String, functor: () -> Any) : FunExpr(name, functor)
    private class DerivedFun1(name: String, functor: (Any) -> Any) : FunExpr(name, functor)
    private class DerivedFun2(name: String, functor: (Any, Any) -> Any) : FunExpr(name, functor)

    private fun testConstructionFromTypedFunction(toTest: FunExpr, expectedResult: Any, vararg invokeParams: Any) {
        testConstructionFromFunction(toTest, expectedResult, *invokeParams)
        for (paramIndex in invokeParams.indices) {
            assertThrows<ClassCastException>(ClassCastException::class.java)
            {
                testInvoke(*(Array(invokeParams.size) {
                    if (it == paramIndex) invokeParams[it].toString() else invokeParams[paramIndex]
                }))
            }
        }
    }

    private fun testConstructionFromFunction(toTest: FunExpr, expectedResult: Any, vararg invokeParams: Any) {
        sut = toTest
        assertEquals(invokeParams.size, sut.arity)
        assertEquals("name", sut.name)
        assertEquals(expectedResult, testInvoke(*invokeParams))
    }

    private fun testInvoke(vararg params: Any) : Any {
        calls = 0
        val result = sut(*params)
        assertEquals(1, calls, "Invalid number of functor calls")
        return result
    }

    private fun testFunctor(result: Any) : Any {
        calls++
        return result
    }

    private val defaultSut = FunExpr(1, "name") { testFunctor(-(it[0] as Int)) }
    private var calls = 0
    private var sut = defaultSut
}
