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
package straightway.sim

import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import straightway.general.dsl.minus
import straightway.test.flow.*
import java.time.Duration

internal class SimulatorTest_pause : SimulatorTest() {

    @Test fun withoutEvent_hasNoEffect() = (sut as Controller).pause()

    @Test fun calledWhileRunning_stopsSimulatiom() {
        sut.schedule(Duration.ofMinutes(1)) { sut.pause() }
        sut.schedule(Duration.ofMinutes(2)) { fail("This event must not be called") }
        expect({ sut.run() } does not - _throw - exception)
        expect(sut.eventQueue has size of 1)
    }

    @Test fun callingRunAfterPause_resumesSimulation() {
        val numCalls = mutableListOf(0, 0)
        sut.schedule(Duration.ofMinutes(1)) { ++numCalls[0]; sut.pause() }
        sut.schedule(Duration.ofMinutes(2)) { ++numCalls[1] }
        expect(numCalls _is equal to listOf(0, 0))
        sut.run()
        expect(numCalls _is equal to listOf(1, 0))
        sut.run()
        expect(numCalls _is equal to listOf(1, 1))
    }
}