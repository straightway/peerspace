/****************************************************************************
 * Copyright 2016 github.com/straightway

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package straightway.simulation

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import straightway.general.TimeProvider
import straightway.testing.CallCounter
import straightway.testing.CallSequence

import java.time.Duration
import java.time.LocalDateTime

internal class SchedulerTest {
    @BeforeEach
    fun setUp() {
        sut = Scheduler()
    }

    @AfterEach
    fun tearDown() {
        nullableSut = null
    }

    @Test
    fun test_currentTime_isInitiallyZero() = assertEquals(initialTime, sut.currentTime)

    @Test
    fun test_schedule_doesNotCallActionImmediately() = sut.schedule(defaultEventDuration) { doNotCall() }

    @Test
    fun test_schedule_addsEventToEventQueue() {
        sut.schedule(defaultEventDuration) { doNotCall() }
        assertEquals(1, sut.eventQueue.size)
    }

    @Test
    fun test_schedule_schedulesEventAtProperTime() {
        sut.schedule(defaultEventDuration) { doNotCall() }
        val targetTime = sut.currentTime.plus(defaultEventDuration)
        assertEquals(targetTime, sut.eventQueue.first().time)
    }

    @Test
    fun test_schedule_addsSpecifiedAction() {
        val callCounter = CallCounter()
        sut.schedule(defaultEventDuration) { callCounter.action() }
        val action = sut.eventQueue.first().action
        sut.action()
        assertEquals(1, callCounter.calls)
    }

    @Test
    fun test_run_executesEvent() {
        val callCounter = CallCounter()
        sut.schedule(defaultEventDuration) { callCounter.action() }
        sut.run()
        assertEquals(1, callCounter.calls)
    }

    @Test
    fun test_run_executesEventAtProperTime() {
        sut.schedule(defaultEventDuration) {
            assertEquals(initialTime.plus(defaultEventDuration), sut.currentTime)
        }
        sut.run()
    }

    @Test
    fun test_run_consumesEvent() {
        sut.schedule(defaultEventDuration) {}
        sut.run()
        assertEquals(0, sut.eventQueue.size)
    }

    @Test
    fun test_run_executesAllEvents() {
        val callSequence = CallSequence(0, 2, 1)
        for (i in 0..2) {
            val execTime = Duration.ofMinutes(callSequence.expectedActionOrder[i].toLong())
            val action = callSequence.actions[i]
            sut.schedule(execTime) { action() }
        }
        sut.run()
        callSequence.assertCompleted()
    }

    @Test
    fun test_schedule_whileExecutinEvent() {
        val callSequence = CallSequence(0, 1)
        sut.schedule(defaultEventDuration) {
            callSequence.actions[0]()
            schedule(defaultEventDuration) { callSequence.actions[1]() }
        }
        sut.run()
        callSequence.assertCompleted()
    }

    @Test
    fun test_scheduler_isTimeProvider() {
        assertTrue(sut is TimeProvider)
    }

    private var sut: Scheduler
        get() = nullableSut!!
        set(value) {
            nullableSut = value
        }

    private var nullableSut: Scheduler? = null
}

private val initialTime = LocalDateTime.of(0, 1, 1, 0, 0)

private val defaultEventDuration = Duration.ofMinutes(1)

@Suppress("unused")
private fun Scheduler.doNotCall() = fail("must not be called")
