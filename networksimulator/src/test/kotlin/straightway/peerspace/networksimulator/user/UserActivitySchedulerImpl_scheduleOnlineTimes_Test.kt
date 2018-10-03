/*
 * Copyright 2016 github.com/straightway
 *
 *  Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package straightway.peerspace.networksimulator.user

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.networksimulator.profile.dsl.Weekly
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.True
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.get
import straightway.units.hour
import straightway.units.minus
import straightway.units.second
import java.time.LocalDate

class UserActivitySchedulerImpl_scheduleOnlineTimes_Test : KoinLoggingDisabler() {

    private val test get() =
        Given {
            UserActivitySchedulerTestEnvironment {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 8[hour]..17[hour] }
                }
            }
        }

    @Test
    fun `switches on and off devices at defined times`() =
            test when_ {
                sut.scheduleDay(day)
            } then {
                assertNextOnlinePeriod(day, 8[hour]..17[hour])
            }

    @Test
    fun `does not directly schedule online time end`() =
            test when_ {
                sut.scheduleDay(day)
            } then {
                expect(simulator.eventQueue.map { event -> event.time } is_ Equal
                               to_ Values(day.at(8[hour]), day.at(24[hour])))
            }

    @Test
    fun `ignores negative time range`() =
            test while_ {
                setNegativeOnlineTimeRange()
            } when_ {
                sut.scheduleDay(day)
            } then {
                day.checkAt(0[hour]) {
                    expect(simulator.eventQueue.filter { event ->
                        event.time.toLocalDate() == day
                    } is_ Empty)
                }
            }

    @Test
    fun `ignores past time range start`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { -25[hour]..8[hour] }
                }
            } when_ {
                sut.scheduleDay(day)
            } then {
                expect(simulator.eventQueue.single().time is_ Equal to_ day.at(24[hour]))
            }

    @Test
    fun `ignores past day`() =
            test when_ {
                sut.scheduleDay(day.minusDays(2))
            } then {
                verify(simScheduler, never()).schedule(any(), any())
            }

    @Test
    fun `schedules itself for next day`() =
            test while_ {
                setNegativeOnlineTimeRange()
            } when_ {
                sut.scheduleDay(day)
            } then {
                day.checkAt(0[hour]) {
                    expect(simulator.eventQueue.single().time is_ Equal to_ day.at(24[hour]))
                }
            }

    @Test
    fun `schedules multiple online times`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 6[hour]..8[hour] }
                    +Weekly.eachDay { 10[hour]..12[hour] }
                }
            } when_ {
                sut.scheduleDay(day)
            } then {
                assertNextOnlinePeriod(day, 6[hour]..8[hour])
                assertNextOnlinePeriod(day, 10[hour]..12[hour])
            }

    @Test
    fun `schedules orders online`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 10[hour]..12[hour] }
                    +Weekly.eachDay { 6[hour]..8[hour] }
                }
            } when_ {
                sut.scheduleDay(day)
            } then {
                assertNextOnlinePeriod(day, 6[hour]..8[hour])
                assertNextOnlinePeriod(day, 10[hour]..12[hour])
            }

    @Test
    fun `merges two overlapping online times`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 6[hour]..8[hour] }
                    +Weekly.eachDay { 7[hour]..9[hour] }
                }
            } when_ {
                sut.scheduleDay(day)
            } then {
                assertNextOnlinePeriod(day, 6[hour]..9[hour])
            }

    @Test
    fun `start time after the given day is ignored`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 25[hour]..27[hour] }
                }
            } when_ {
                sut.scheduleDay(day)
            } then {
                day.checkAt(26[hour]) { expect(device.isOnline is_ False) }
            }

    @Test
    fun `time range from last day is merged with first one if needed`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 1[hour]..6[hour] }
                    +Weekly.eachDay { 22[hour]..26[hour] }
                }
            } when_ {
                sut.scheduleDay(day)
            } then {
                day.checkAt(27[hour]) { expect(device.isOnline is_ True) }
            }

    private val UserActivitySchedulerTestEnvironment.device get() =
        user.environment.devices.single()

    private fun UserActivitySchedulerTestEnvironment.setNegativeOnlineTimeRange() {
        deviceUsageProfile.onlineTimes {
            +Weekly.eachDay { 17[hour]..8[hour] }
        }
    }

    private val UserActivitySchedulerTestEnvironment.deviceUsageProfile get() =
            profile.usedDevices.values.single()

    private fun UserActivitySchedulerTestEnvironment.assertNextOnlinePeriod(
            day: LocalDate,
            range: ClosedRange<UnitNumber<Time>>
    ) {
        val checkedScenario = "$device is online from ${range.start} to ${range.endInclusive}"
        day.checkAt(range.start - 1[second]) {
            expect(!device.isOnline) { "$checkedScenario: offline before ${range.start}" }
        }
        day.checkAt(range.start) {
            expect(device.isOnline) { "$checkedScenario: is online at ${range.start}" }
            val nextEventTime = simulator.eventQueue.first().time
            expect(nextEventTime == day.at(range.endInclusive)) {
                "$checkedScenario: Next event is at $nextEventTime " +
                        "and not at ${day.at(range.endInclusive)}"
            }
        }
        day.checkAt(range.endInclusive) {
            expect(!device.isOnline) {
                "$checkedScenario: is offline at ${range.endInclusive}"
            }
        }
    }
}