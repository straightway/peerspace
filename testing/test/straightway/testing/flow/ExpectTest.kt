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
import straightway.general.dsl.Value
import straightway.general.dsl.minus
import straightway.testing.assertDoesNotThrow
import straightway.testing.assertFails

class ExpectTest {

    @Test fun booleanExpression_false_isFailure() =
        assertFails { expect(Value(false)) }

    @Test fun nonBooleanExpression_isFailure() =
            assertFails("Expectation <1> failed (java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.Boolean)")
        { expect(Value(1)) }

    @Test fun booleanExpression_true_isSuccess() =
        assertDoesNotThrow{ expect(Value(true)) }

    @Test fun failure_singleDyadicOp_withMeaningfulExplanation() =
            assertFails("Expectation <1 greater 2> failed", { expect(1 _is greater than 2) })

    @Test fun failure_monadicWithDyadicOp_withMeaningfulExplanation() =
            assertFails("Expectation <1 not-equal 1> failed") { expect(1 _is not - equal to 1) }

    @Test fun failure_notFullyBoundExpression_withMeaningfulExplanation() =
            assertFails("Expectation <1 greater ?> failed (Invalid number of parameters. Expected: 2, got: 1)")
            { expect(1 _is greater) }

    @Test fun success_directlyUsingBoolean() =
            assertDoesNotThrow { expect(true) }

    @Test fun failure_directlyUsingBoolean() =
            assertFails { expect(false) }

    @Test fun success_directlyUsingBoolean_withExplanation() =
            assertDoesNotThrow { expect(true) { "Explanation" } }

    @Test fun failure_directlyUsingBoolean_withExplanation() =
            assertFails("Explanation") { expect(false) { "Explanation" } }
}