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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExprTest {

    private class TestExpr(override val arity: Int) : Expr {
        override fun invoke(vararg params: Any): Any {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    @Test fun accept_defaultImplementation_visitsExpression() {
        val sut = TestExpr(0)
        var calls = 0
        sut.accept {
            assertTrue(it === sut)
            calls++
        }

        assertEquals(1, calls, "Unexpected number of calls")
    }
}