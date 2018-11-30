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
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.networksimulator.profile.dsl.DeviceUsageProfile
import straightway.peerspace.networksimulator.profile.dsl.Weekly
import straightway.peerspace.networksimulator.profile.pc
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.get
import straightway.units.hour

class UserActivitySchedulerImplTest { // : KoinLoggingDisabler() {

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
                            onlineTimes { +Weekly.eachDay { 8[hour]..16[hour] } }
                            device { pc }
                            usages { }
                        }
                        +DeviceUsageProfile {
                            onlineTimes { +Weekly.eachDay { 18[hour]..22[hour] } }
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
                    day.checkAt(0[hour]) {
                        expect(simulator.eventQueue.single().time is_ Equal to_ day.at(24[hour]))
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
}