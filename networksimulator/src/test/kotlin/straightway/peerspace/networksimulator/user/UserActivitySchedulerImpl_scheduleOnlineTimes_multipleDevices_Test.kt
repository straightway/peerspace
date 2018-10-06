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

import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.networksimulator.profile.dsl.DeviceUsageProfile
import straightway.peerspace.networksimulator.profile.dsl.Weekly
import straightway.peerspace.networksimulator.profile.pc
import straightway.testing.bdd.Given
import straightway.testing.flow.expect
import straightway.units.get
import straightway.units.hour

class UserActivitySchedulerImpl_scheduleOnlineTimes_multipleDevices_Test :
        KoinLoggingDisabler() {

    // region Setup

    private val test get() =
        Given {
            UserActivitySchedulerTestEnvironment {
                profile.usedDevices {
                    +DeviceUsageProfile {
                        onlineTimes { +Weekly.eachDay { 8[hour]..16[hour] } }
                        device { pc }
                    }
                    +DeviceUsageProfile {
                        onlineTimes { +Weekly.eachDay { 18[hour]..22[hour] } }
                        device { pc }
                    }
                }
            }
        }

    // endregion

    @Test
    fun `schedule both devices`() =
            test when_ {
                sut.scheduleDay(day)
            } then {
                val devices = user.environment.devices
                day.checkAt(7[hour]) {
                    expect(!devices[0].isOnline) { "device0 is offline at 7:00" }
                    expect(!devices[1].isOnline) { "device1 is offline at 7:00" }
                }
                day.checkAt(9[hour]) {
                    expect(devices[0].isOnline) { "device0 is online at 9:00" }
                    expect(!devices[1].isOnline) { "device1 is offline at 9:00" }
                }
                day.checkAt(17[hour]) {
                    expect(!devices[0].isOnline) { "device0 is offline at 17:00" }
                    expect(!devices[1].isOnline) { "device1 is offline at 17:00" }
                }
                day.checkAt(19[hour]) {
                    expect(!devices[0].isOnline) { "device0 is offline at 19:00" }
                    expect(devices[1].isOnline) { "device1 is online at 19:00" }
                }
            }
}