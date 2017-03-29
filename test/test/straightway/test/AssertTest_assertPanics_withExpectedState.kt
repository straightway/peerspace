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
package straightway.test

import org.junit.jupiter.api.Test
import straightway.general.Panic

class AssertTest_assertPanics_withExpectedState {

    @Test
    fun passes_ifPanicOccurs_withCorrectState() =
        assertDoesNotThrow { assertPanics(expectedState) { throw Panic(expectedState) } }

    @Test
    fun fails_ifNoPanicOccurs() =
        assertFails { assertPanics(expectedState) {} }

    @Test
    fun fails_ifPanicOccurs_withIncorrectState() =
        assertFails { assertPanics(expectedState) { throw Panic(unexpectedState) } }

    private companion object {
        val expectedState = Any()
        val unexpectedState = Any()
    }
}