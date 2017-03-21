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
package straightway.testing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CallSequenceTest {
    @Test
    fun test_verifyActionExecutionOrder() {
        val sut = CallSequence(0, 2, 1)
        sut.actions[0]()
        sut.actions[2]()
        sut.actions[1]()
        sut.assertCompleted()
    }

    @Test
    fun test_getActionsInProperOrder() {
        val sut = CallSequence(0, 2, 1)
        for (action in sut.orderedActions) {
            action()
        }
        sut.assertCompleted()
    }

    @Test
    fun test_toString() {
        val sut = CallSequence(0, 2, 1)
        assertEquals("CallSequence(0, 2, 1)", sut.toString())
    }
}