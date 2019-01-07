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
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.error.Panic
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Id
import straightway.peerspace.networksimulator.profile.dsl.DeviceUsageProfile
import straightway.peerspace.networksimulator.profile.dsl.UserProfile
import straightway.peerspace.networksimulator.profile.dsl.Weekly
import straightway.peerspace.networksimulator.profile.pc
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Throw
import straightway.testing.flow.Values
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.get
import straightway.units.hour

class UserActivitySchedulerImplTest : KoinLoggingDisabler() {

    // region Setup

    private val test get() =
        Given {
            object {
                val deviceActivitySchedules = mutableMapOf<Id, DeviceActivitySchedule>()
                val deviceOnlineTimeSchedules = mutableMapOf<Id, DeviceOnlineTimeSchedule>()
                val environment = UserActivitySchedulerTestEnvironment {
                    userActivitySchedulerFactory = { UserActivitySchedulerImpl() }
                    deviceActivityScheduleFactory = { device ->
                        mock<DeviceActivitySchedule>().apply {
                            deviceActivitySchedules[device.id] = this
                        }
                    }
                    deviceOnlineTimeScheduleFactory = { device ->
                        mock<DeviceOnlineTimeSchedule>().apply {
                            deviceOnlineTimeSchedules[device.id] = this
                        }
                    }
                    profile.usedDevices {
                        +DeviceUsageProfile {
                            onlineTimes { +Weekly.eachDay { 8.0[hour]..16.0[hour] } }
                            device { pc }
                            usages { }
                        }
                        +DeviceUsageProfile {
                            onlineTimes { +Weekly.eachDay { 18.0[hour]..22.0[hour] } }
                            device { pc }
                            usages { }
                        }
                    }
                }
            }
        }

    // endregion

    @Test
    fun `schedules itself for next day`() =
            test when_ {
                environment.userActivityScheduler.scheduleDay(environment.day)
            } then {
                with(environment) {
                    day.checkAt(0.0[hour]) {
                        expect(simulator.eventQueue.single().time is_ Equal to_ day.at(24.0[hour]))
                    }
                }
            }

    @Test
    fun `schedules all devices`() =
            test when_ {
                environment.userActivityScheduler.scheduleDay(environment.day)
            } then {
                with(environment) {
                    val devices = user.environment.devices
                    devices.forEach {
                        verify(deviceOnlineTimeSchedules[it.id]!!).scheduleOnlineTimes(day)
                        verify(deviceActivitySchedules[it.id]!!).scheduleActivities(day)
                    }
                }
            }

     @Test
     fun `blocks inactive times`() =
             test while_ {
                 environment.profile = UserProfile {
                     usedDevices { }
                     activityTimes { +Weekly.eachDay { 8.0[hour]..16.0[hour] } }
                 }
             } when_ {
                 environment.userActivityScheduler.scheduleDay(environment.day)
             } then {
                expect(environment.blockedUserTimes is_ Equal to_
                        Values(0.0[hour]..8.0[hour], 16.0[hour]..24.0[hour]))
             }

    @Test
    fun `blocks inactive times of all weekly periods`() =
            test while_ {
                environment.profile = UserProfile {
                    usedDevices { }
                    activityTimes {
                        +Weekly.eachDay { 8.0[hour]..16.0[hour] }
                        +Weekly.eachDay { 18.0[hour]..22.0[hour] }
                    }
                }
            } when_ {
                environment.userActivityScheduler.scheduleDay(environment.day)
            } then {
                expect(environment.blockedUserTimes is_ Equal to_
                        Values(0.0[hour]..8.0[hour], 16.0[hour]..18.0[hour],
                                22.0[hour]..24.0[hour]))
            }

    @Test
    fun `does not block inactive times for other weekdays`() =
            test while_ {
                environment.profile = UserProfile {
                    usedDevices { }
                    activityTimes {
                        +Weekly.eachDay { 8.0[hour]..16.0[hour] }
                        +Weekly("never") { isApplicableTo { { false } } }
                                .invoke { 18.0[hour]..22.0[hour] }
                    }
                }
            } when_ {
                environment.userActivityScheduler.scheduleDay(environment.day)
            } then {
                expect(environment.blockedUserTimes is_ Equal to_
                        Values(0.0[hour]..8.0[hour], 16.0[hour]..24.0[hour]))
            }

    @Test
    fun `simulation event is scheduled with description`() =
            test when_ {
                environment.userActivityScheduler.scheduleDay(environment.day)
            } then {
                val expectedDescription =
                        "scheduling ${environment.day.plusDays(1)} for user ${environment.user.id}"
                verify(environment.simScheduler).schedule(any(), eq(expectedDescription), any())
            }

    @Test
    fun `scheduling past day panics`() =
            test when_ {
                val yesterday = environment.simulator.now.toLocalDate().minusDays(1)
                environment.userActivityScheduler.scheduleDay(yesterday)
            } then {
                expect({ it.result } does Throw.type<Panic>())
            }
}