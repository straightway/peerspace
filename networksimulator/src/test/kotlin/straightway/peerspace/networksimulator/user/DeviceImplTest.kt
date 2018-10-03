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
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.withContext
import straightway.peerspace.data.Id
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.chunkSizeGetter
import straightway.peerspace.net.peer
import straightway.peerspace.networksimulator.SimNode
import straightway.peerspace.networksimulator.profile.dsl.DeviceProfile
import straightway.random.RandomDistribution
import straightway.sim.net.TransmissionRequestHandler
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.True
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.bit
import straightway.units.byte
import straightway.units.div
import straightway.units.get
import straightway.units.gi
import straightway.units.ki
import straightway.units.mi
import straightway.units.second
import straightway.utils.TimeProvider

class DeviceImplTest : KoinLoggingDisabler() {

    // region Setup

    private companion object {
        val defaultId = Id("default")
        val defaultProfile = DeviceProfile {
            uploadBandwidth { 16[mi(bit) / second] }
            downloadBandwidth { 4[mi(bit) / second] }
            persistentStorageAvailable { 4[gi(byte)] }
        }
    }

    private fun test(id: Id = defaultId, profile: DeviceProfile = defaultProfile) =
            Given {
                object {
                    val environment = withContext {
                        bean { mock<TransmissionRequestHandler>() }
                        bean { chunkSizeGetter { 64[ki(byte)] } }
                        bean("simNodes") { mutableMapOf<Any, SimNode>() }
                        bean { mock<TimeProvider>() }
                        bean { mock<RandomDistribution<Byte>>() }
                        bean { DeviceImpl(id, profile) }
                    } make {
                        KoinModuleComponent()
                    }
                    val sut = environment.get<DeviceImpl>()
                    val simNodes get() = environment.get<MutableMap<Any, SimNode>>()
                    val simNode get() = simNodes.values.single()
                }
            }

    // endregion

    @Test
    fun `node is added to map of SimNodes`() =
            test() when_ {
                simNodes.keys
            } then {
                expect(it.result is_ Equal to_ Values(defaultId))
            }

    @Test
    fun `device is online if node is online`() =
            test() when_ {
                simNode.isOnline = true
            } then {
                expect(sut.isOnline is_ True)
            }

    @Test
    fun `device is offline if node is offline`() =
            test() when_ {
                simNode.isOnline = false
            } then {
                expect(sut.isOnline is_ False)
            }

    @Test
    fun `setting device online sets node online`() =
            test() while_ {
                simNode.isOnline = false
            } when_ {
                sut.isOnline = true
            } then {
                expect(simNode.isOnline is_ True)
            }

    @Test
    fun `setting device offline sets node offline`() =
            test() while_ {
                simNode.isOnline = true
            } when_ {
                sut.isOnline = false
            } then {
                expect(simNode.isOnline is_ False)
            }

    @Test
    fun `peerClient belongs to node`() =
            test() when_ {
                (sut.peerClient as PeerComponent).peer.id
            } then {
                expect(it.result is_ Equal to_ defaultId)
            }
}