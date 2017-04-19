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

/**
 * Provides a series of actions which are expected to be called in the given order
 * when assertCompleted is called.
 */
class CallSequence(vararg val expectedActionOrder: Int) {
    val actions: Array<() -> Unit>
    var orderedActions: Array<() -> Unit>

    fun assertCompleted() = assertEquals(expectedActionOrder.size, _nextExpectedCallIndex)
    override fun toString(): String = "CallSequence(" + expectedActionOrder.joinToString(", ") + ")"

    init {
        val maxAction = expectedActionOrder.max() ?: 0
        actions = Array<() -> Unit>(maxAction + 1) {
            {
                assertEquals(expectedActionOrder[_nextExpectedCallIndex], it)
                ++_nextExpectedCallIndex
            }
        }
        orderedActions = Array(expectedActionOrder.size) { actions[expectedActionOrder[it]] }
    }

    private var _nextExpectedCallIndex = 0
}