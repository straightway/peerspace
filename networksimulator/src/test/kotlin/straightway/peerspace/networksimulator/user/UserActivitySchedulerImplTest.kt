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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.withContext
import straightway.peerspace.networksimulator.Device
import straightway.peerspace.networksimulator.profile.dsl.DeviceUsageProfile
import straightway.peerspace.networksimulator.profile.dsl.UserProfile
import straightway.peerspace.networksimulator.profile.dsl.Weekly
import straightway.sim.Scheduler
import straightway.testing.bdd.Given
import straightway.units.get
import straightway.units.hour
import straightway.utils.TimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class UserActivitySchedulerImplTest : KoinLoggingDisabler() {

    private companion object {
        val DAY = LocalDate.of(2013, 1, 1)!!
    }

    private val test get() =
        Given {
            object {
                var profile = UserProfile {}
                val now = LocalDateTime.of(DAY.minusDays(1), LocalTime.MIDNIGHT)
                val simScheduler: Scheduler = mock { _ ->
                    on { schedule(any(), any()) }.thenAnswer {
                        it.getArgument<() -> Unit>(1)()
                    }
                }
                val timeProvider: TimeProvider = mock { _ ->
                    on { this.now }.thenAnswer { now }
                }
                val devices = mutableListOf<Device>()
                val sut by lazy {
                    withContext {
                        bean { _ -> profile }
                        bean { _ -> timeProvider }
                        bean { _ -> simScheduler }
                        bean { _ -> User() }
                    } make {
                        UserActivitySchedulerImpl()
                    }
                }
            }
        }

    @Test @Disabled
    fun `scheduleDay switches on devices at defined times`() =
            test while_ {
                profile = UserProfile {
                    usedDevices {
                        +DeviceUsageProfile {
                            onlineTimes { +Weekly.eachDay { 8[hour]..17[hour] } }
                        }
                    }
                }
                devices.add(mock())
            } when_ {
                sut.scheduleDay(DAY)
            } then {
                verify(simScheduler).schedule(eq(32[hour]), any())
                verify(devices.single()).isOnline = true
            }
}