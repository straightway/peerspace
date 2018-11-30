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

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.koinutils.Bean.get
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.networksimulator.profile.dsl.Activity
import straightway.peerspace.networksimulator.profile.dsl.UsageProfile
import straightway.peerspace.networksimulator.profile.dsl.Weekly
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.byte
import straightway.units.get
import straightway.units.hour
import straightway.units.mi
import straightway.units.minute

class DeviceActivityScheduleImplTest : KoinLoggingDisabler() {

    // region Setup

    private data class ActivityTimingParameters
    (
            val ranges: List<TimeRange>,
            val duration: UnitNumber<Time>,
            val resultRange: TimeRange) {

        val timing = mock<ActivityTiming> {
            on { timeRange }.thenAnswer { resultRange }
        }
    }

    private fun test(targetRange: TimeRange) = Given {
        object {
            var deviceActivityCalls = 0
            val activityTiminigs = mutableListOf<ActivityTimingParameters>()
            val environment = UserActivitySchedulerTestEnvironment {
                deviceActivityScheduleFactory = { DeviceActivityScheduleImpl(it) }
                activityTiminigFactory = { ranges, duration ->
                    ActivityTimingParameters(ranges, duration, targetRange).let {
                        activityTiminigs.add(it)
                        it.timing
                    }
                }
                profile.usedDevices.values.single().usages {
                    +UsageProfile {
                        numberOfTimes { 1 }
                        activity { Activity("testActivity") { deviceActivityCalls++ } }
                        duration { 1[minute] }
                        time { Weekly.eachDay { 8[hour]..17[hour] } }
                        dataVolume { 1[mi(byte)] }
                    }
                }
            }

            val device get() =
                environment.user.environment.devices.single()

            val sut by lazy {
                environment.context.get<DeviceActivitySchedule> { mapOf("device" to device) }
            }

            val deviceUsageProfile get() =
                environment.profile.usedDevices.values.single()
        }
    }

    // endregion

    @Test
    fun `activityTiming is initially called with range defined in usage profile`() =
        test(10[hour]..11[hour]) when_ {
            sut.scheduleActivities(environment.day)
        } then {
            expect(activityTiminigs.single().ranges is_
                    Equal to_ Values(8[hour]..17[hour]))
        }

    @Test
    fun `second call to activityTiminig is called with split range due to first activity`() =
            test(10[hour]..11[hour]) while_ {
                environment.profile.usedDevices.values.single()
                        .usages.values.single().numberOfTimes { 2 }
            } when_ {
                sut.scheduleActivities(environment.day)
            } then {
                expect(activityTiminigs.size is_ Equal to_ 2)
                expect(activityTiminigs.first().ranges is_
                        Equal to_ Values(8[hour]..17[hour]))
                expect(activityTiminigs.last().ranges is_
                        Equal to_ Values(8[hour]..10[hour], 11[hour]..17[hour]))
            }

    @Test
    fun `activityTiming is initially called with duration defined in usage profile`() =
        test(10[hour]..11[hour]) when_ {
            sut.scheduleActivities(environment.day)
        } then {
            expect(activityTiminigs.single().duration is_ Equal to_ 1[minute])
        }
}