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
import com.nhaarman.mockito_kotlin.mock
import straightway.koinutils.Bean.get
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.withContext
import straightway.peerspace.net.PeerClient
import straightway.peerspace.networksimulator.SimNode
import straightway.peerspace.networksimulator.profile.dsl.DeviceProfile
import straightway.peerspace.networksimulator.profile.dsl.DeviceUsageProfile
import straightway.peerspace.networksimulator.profile.dsl.UserProfile
import straightway.sim.Scheduler
import straightway.sim.core.Simulator
import straightway.testing.flow.expect
import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.byte
import straightway.units.div
import straightway.units.get
import straightway.units.mi
import straightway.units.milli
import straightway.units.minus
import straightway.units.plus
import straightway.units.second
import straightway.utils.TimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class UserActivitySchedulerTestEnvironment(
        init: UserActivitySchedulerTestEnvironment.() -> Unit
) {

    val day = LocalDate.of(2013, 1, 1)!!

    val profile = UserProfile {
        usedDevices {
            +DeviceUsageProfile {
                onlineTimes { values() }
                device {
                    DeviceProfile {
                        uploadBandwidth { 2[mi(byte) / second] }
                        downloadBandwidth { 8[mi(byte) / second] }
                    }
                }
            }
        }
    }
    val simulator = Simulator()
    val simScheduler: Scheduler = mock {
        on { schedule(any(), any()) }.thenAnswer {
            simulator.schedule(it.getArgument(0), it.getArgument(1))
        }
    }
    val timeProvider: TimeProvider = simulator
    val sut by lazy { context.get<UserActivitySchedulerImpl>() }
    val user by lazy { context.get<User>() }
    fun LocalDate.at(time: UnitNumber<Time>) =
            LocalDateTime.of(this, LocalTime.MIDNIGHT) + time
    fun LocalDate.checkAt(time: UnitNumber<Time>, check: () -> Unit) {
        var isCheckExecuted = false
        simulator.schedule(at(time) - simulator.now + 1[milli(second)]) {
            isCheckExecuted = true
            simulator.pause()
            check()
        }
        simulator.run()
        expect(isCheckExecuted) { "The check has not been executed" }
    }
    private val context by lazy {
        withContext {
            bean("simNodes") { mutableMapOf<Any, SimNode>() }
            bean { _ -> profile }
            bean { _ -> timeProvider }
            bean { _ -> simScheduler }
            bean { _ -> User() }
            bean { UserActivitySchedulerImpl() }
            factory { args ->
                val peerClient = mock<PeerClient>()
                var isOnline = false
                fun setOnline(online: Boolean) { isOnline = online }
                mock<Device> {
                    on { this.id }.thenAnswer { args["id"] }
                    on { this.peerClient }.thenAnswer { peerClient }
                    on { this.isOnline }.thenAnswer { isOnline }
                    on { this.isOnline = any() }.thenAnswer {
                        setOnline(it.getArgument(0))
                    }
                }
            }
        } make {
            KoinModuleComponent()
        }
    }

    init {
        val now = LocalDateTime.of(day.minusDays(1), LocalTime.MIDNIGHT)
        simulator.schedule(now - simulator.now) {}
        simulator.run()
        init()
    }
}