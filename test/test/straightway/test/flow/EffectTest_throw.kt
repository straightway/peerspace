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
package straightway.test.flow

import org.junit.jupiter.api.Test
import straightway.general.Panic
import straightway.general.dsl.minus
import straightway.test.assertDoesNotThrow
import straightway.test.assertFails

class EffectTest_throw {
    @Test fun passes() =
            assertDoesNotThrow { expect(::panic does _throw-exception) }
    @Test fun passes_withSpecificException() =
            assertDoesNotThrow { expect(::panic does _throw-Panic::class) }
    @Test fun negation_passes() =
            assertDoesNotThrow { expect(::throwNothing does not-_throw-exception) }
    @Test fun negation_passes_withSpecificException() =
            assertDoesNotThrow { expect(::panic does not-_throw-NullPointerException::class) }
    @Test fun fails() =
            assertFails { expect(::throwNothing does _throw-exception) }
    @Test fun fails_withSpecificException() =
            assertFails { expect(::panic does _throw-NullPointerException::class) }
    @Test fun negation_fails() =
            assertFails { expect(::panic does not-_throw-exception) }
    @Test fun fails_withMeaningfulMessage() =
            assertFails("Expectation <class kotlin.Throwable thrown by fun throwNothing(): kotlin.Unit> failed")
            { expect(::throwNothing does _throw-exception) }
    @Test fun negated_fails_withMeaningfulMessage() =
            assertFails("Expectation <class kotlin.Throwable not-thrown by fun panic(): kotlin.Unit> failed")
            { expect(::panic does not-_throw-exception) }
}

private fun throwNothing() {}
private fun panic(): Unit = throw Panic("panic!")