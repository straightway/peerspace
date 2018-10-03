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
import straightway.expr.minus
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.withContext
import straightway.peerspace.net.chunkSizeGetter
import straightway.peerspace.networksimulator.SimNode
import straightway.peerspace.networksimulator.profile.dsl.Activity
import straightway.peerspace.networksimulator.profile.dsl.DeviceUsageProfile
import straightway.peerspace.networksimulator.profile.dsl.UsageProfile
import straightway.peerspace.networksimulator.profile.dsl.UserProfile
import straightway.peerspace.networksimulator.profile.dsl.Weekly
import straightway.peerspace.networksimulator.profile.pc
import straightway.random.RandomDistribution
import straightway.sim.net.TransmissionRequestHandler
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Not
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.byte
import straightway.units.get
import straightway.units.hour
import straightway.units.ki
import straightway.utils.TimeProvider

class UserTest : KoinLoggingDisabler() {

    private interface ActivityHandler {
        fun handleActivity(env: UserEnvironment, profile: UsageProfile)
    }

    private val test get() =
        Given {
            object {
                val activityHandler: ActivityHandler = mock()
                val profile by lazy {
                    UserProfile {
                        usedDevices {
                            +device
                        }
                    }
                }
                val device by lazy {
                    DeviceUsageProfile {
                        device { pc }
                        onlineTimes { +Weekly.mondays { 8[hour]..12[hour] } }
                        usages { +testUsage }
                    }
                }
                val testUsage by lazy {
                    UsageProfile {
                        activity { mockedActivity }
                    }
                }

                val mockedActivity get() =
                    Activity("activityName") { profile: UsageProfile ->
                        activityHandler.handleActivity(this, profile)
                    }

                val sut by lazy { createUser(profile) }
            }
        }

    @Test
    fun `construction creates different ids for different users`() =
            test when_ {
                createUser(profile).id
            } then {
                expect(it.result is_ Not - Equal to_ sut.id)
            }

    @Test
    fun `construction creates node with own node id`() =
        test when_ {
            sut.environment.devices.single().id
        } then {
            expect(it.result is_ Not - Equal to_ sut.id)
        }

    @Test
    fun `construction creates one node per device`() =
            test while_ {
                profile.usedDevices {
                    +device
                    +device
                }
            } when_ {
                sut.environment.devices
            } then {
                expect(it.result.size is_ Equal to_ 2)
            }

    @Test
    fun `each created node has a different id`() =
            test while_ {
                profile.usedDevices {
                    +device
                    +device
                }
            } when_ {
                sut.environment.devices
            } then {
                expect(it.result[0].id is_ Not - Equal to_ it.result[1].id)
            }

    @Test
    fun `user devices are offline initially`() =
            test when_ {
                sut.environment.devices.first().isOnline
            } then {
                expect(it.result is_ False)
            }

    private fun createUser(profile: UserProfile) =
            withContext {
                bean("simNodes") { mutableMapOf<Any, SimNode>() }
                bean { _ -> profile }
                bean("randomSource") { _ -> mock<RandomDistribution<Byte>>() }
                bean { _ -> mock<TimeProvider>() }
                bean { _ -> chunkSizeGetter { 64[ki(byte)] } }
                bean { _ -> mock<TransmissionRequestHandler>() }
                bean { _ -> mock<UserActivityScheduler>() }
                factory { args -> mock<Device> { on { id }.thenAnswer { args["id"] } } }
            } make {
                User()
            }
}