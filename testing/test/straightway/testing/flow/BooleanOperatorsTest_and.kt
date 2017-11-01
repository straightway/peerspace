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

import org.junit.jupiter.api.Test
import straightway.dsl.FunExpr
import straightway.dsl.Value

class BooleanOperatorsTest_and {

    @Test fun isTrueIfBothArgumentsAreTrue() =
            expect((Value(true) and Value(true))() _is _true)

    @Test fun isFalseIfFirstArgumentIsFalse() =
            expect((Value(false) and Value(true))() _is _false)

    @Test fun isFalseIfSecondArgumentIsFalse() =
            expect((Value(true) and Value(false))() _is _false)

    @Test fun isFalseIfBothArgumentsAreFalse() =
            expect((Value(false) and Value(false))() _is _false)

    @Test fun passesArgumentsToBothSubExpressions() {
        val left = FunExpr("left") { a -> expect(a _is equal to 83); true }
        val right = FunExpr("right") { a -> expect(a _is equal to 83); true }
        expect((left and right)(83) _is _true)
    }

    @Test fun toString_yieldsProperString() =
        expect((Value(true) and Value(false)).toString() _is equal to "true and false")
}