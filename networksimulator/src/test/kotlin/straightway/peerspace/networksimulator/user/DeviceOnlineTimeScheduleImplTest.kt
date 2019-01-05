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
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.Bean.get
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.networksimulator.profile.dsl.DeviceUsageProfile
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
import straightway.units.get
import straightway.units.hour
import straightway.units.second
import java.time.LocalDate

class DeviceOnlineTimeScheduleImplTest : KoinLoggingDisabler() {

    // region Setup

    private fun test(usage: DeviceUsageProfile) = Given {
        object {
            val environment = UserActivitySchedulerTestEnvironment {
                deviceActivityScheduleFactory = { DeviceActivityScheduleImpl(it) }
                deviceOnlineTimeScheduleFactory = { DeviceOnlineTimeScheduleImpl(it) }
                profile.usedDevices { +usage }
            }

            val device get() =
                environment.user.environment.devices.single()

            val sut by lazy {
                environment.context.get<DeviceOnlineTimeSchedule> { mapOf("device" to device) }
            }

            fun setNegativeOnlineTimeRange() {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 17.0[hour]..8.0[hour] }
                }
            }

            fun assertNextOnlinePeriod(day: LocalDate, range: TimeRange) = with(environment) {
                val checkedScenario =
                        "$device is online from ${range.start} to ${range.endInclusive}"
                day.checkAt(range.start - 1.0[second]) {
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

            val deviceUsageProfile get() =
                environment.profile.usedDevices.values.single()
        }
    }

    private val test get() = test(DeviceUsageProfile {
        onlineTimes {
            +Weekly.eachDay { 8.0[hour]..17.0[hour] }
        }
    })

    // endregion

    @Test
    fun `switches on and off devices at defined times`() =
            test when_ {
                sut.scheduleOnlineTimes(environment.day)
            } then {
                assertNextOnlinePeriod(environment.day, 8.0[hour]..17.0[hour])
            }

    @Test
    fun `does not directly schedule online time end`() =
            test when_ {
                sut.scheduleOnlineTimes(environment.day)
            } then {
                with(environment) {
                    expect(simulator.eventQueue.map { event -> event.time } is_ Equal
                            to_ Values(day.at(8.0[hour])))
                }
            }

    @Test
    fun `ignores negative time range`() =
            test while_ {
                setNegativeOnlineTimeRange()
            } when_ {
                sut.scheduleOnlineTimes(environment.day)
            } then {
                with(environment) {
                    day.checkAt(0.0[hour]) {
                        expect(simulator.eventQueue.filter { event ->
                            event.time.toLocalDate() == day
                        } is_ Empty)
                    }
                }
            }

    @Test
    fun `ignores past time range start`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { -25.0[hour]..8.0[hour] }
                }
            } when_ {
                sut.scheduleOnlineTimes(environment.day)
            } then {
                expect(environment.simulator.eventQueue is_ Empty)
            }

    @Test
    fun `ignores past day`() =
            test when_ {
                sut.scheduleOnlineTimes(environment.day.minusDays(2))
            } then {
                verify(environment.simScheduler, never()).schedule(any(), any(), any())
            }

    @Test
    fun `schedules multiple online times`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 6.0[hour]..8.0[hour] }
                    +Weekly.eachDay { 10.0[hour]..12.0[hour] }
                }
            } when_ {
                sut.scheduleOnlineTimes(environment.day)
            } then {
                assertNextOnlinePeriod(environment.day, 6.0[hour]..8.0[hour])
                assertNextOnlinePeriod(environment.day, 10.0[hour]..12.0[hour])
            }

    @Test
    fun `schedules orders online`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 10.0[hour]..12.0[hour] }
                    +Weekly.eachDay { 6.0[hour]..8.0[hour] }
                }
            } when_ {
                sut.scheduleOnlineTimes(environment.day)
            } then {
                assertNextOnlinePeriod(environment.day, 6.0[hour]..8.0[hour])
                assertNextOnlinePeriod(environment.day, 10.0[hour]..12.0[hour])
            }

    @Test
    fun `merges two overlapping online times`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 6.0[hour]..8.0[hour] }
                    +Weekly.eachDay { 7.0[hour]..9.0[hour] }
                }
            } when_ {
                sut.scheduleOnlineTimes(environment.day)
            } then {
                assertNextOnlinePeriod(environment.day, 6.0[hour]..9.0[hour])
            }

    @Test
    fun `start time after the given day is ignored`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 25.0[hour]..27.0[hour] }
                }
            } when_ {
                sut.scheduleOnlineTimes(environment.day)
            } then {
                with(environment) {
                    day.checkAt(26.0[hour]) { expect(device.isOnline is_ False) }
                }
            }

    @Test
    fun `online time is not scheduled if weekday does not match`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly("never") {
                        isApplicableTo { { false } }
                        hours { 1.0[hour]..6.0[hour] }
                    }
                }
                device.isOnline = false
            } when_ {
                sut.scheduleOnlineTimes(environment.day)
            } then {
                with(environment) {
                    day.checkAt(2.0[hour]) { expect(device.isOnline is_ False) }
                }
            }

    @Test
    fun `onlineTimes is only evaluated once`() {
        var onlineTimesHoursCalls = 0
        test while_ {
            deviceUsageProfile.onlineTimes {
                +Weekly.eachDay { onlineTimesHoursCalls++; 6.0[hour]..8.0[hour] }
            }
        } when_ {
            sut.scheduleOnlineTimes(environment.day)
        } then {
            expect(onlineTimesHoursCalls is_ Equal to_ 1)
        }
    }

    @Test
    fun `time range from last day is merged with first one if needed`() =
            test while_ {
                deviceUsageProfile.onlineTimes {
                    +Weekly.eachDay { 1.0[hour]..6.0[hour] }
                    +Weekly.eachDay { 22.0[hour]..26.0[hour] }
                }
            } when_ {
                with(environment) {
                    sut.scheduleOnlineTimes(day)
                    day.plusDays(1L).checkAt(0.0[hour]) {
                        sut.scheduleOnlineTimes(day.plusDays(1L))
                    }
                }
            } then {
                with(environment) {
                    day.checkAt(27.0[hour]) { expect(device.isOnline is_ True) }
                }
            }

    @Test
    fun `online event is scheduled with description`() =
            test when_ {
                sut.scheduleOnlineTimes(environment.day)
            } then {
                val expectedDescription = "device ${device.id} goes online"
                verify(environment.simScheduler).schedule(any(), eq(expectedDescription), any())
            }

    @Test
    fun `offline event is scheduled with description`() =
            test while_ {
                sut.scheduleOnlineTimes(environment.day)
            } when_ {
                environment.simulator.eventQueue.single().action()
            } then {
                val expectedDescription = "device ${device.id} goes offline"
                verify(environment.simScheduler).schedule(any(), eq(expectedDescription), any())
            }
}