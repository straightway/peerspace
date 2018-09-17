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
package straightway.peerspace.net.impl

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Id
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.configuration
import straightway.peerspace.net.peerDirectory
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.plus
import java.time.LocalDateTime

class TransientPeerDirectoryTest : KoinLoggingDisabler() {

    private val test get() = Given {
        object {
            var currentTime = LocalDateTime.of(2001, 12, 18, 14, 53, 33)
            val environment = PeerTestEnvironment(
                    configurationFactory = { Configuration(maxKnownPeers = 3) },
                    peerDirectoryFactory = { TransientPeerDirectory() },
                    timeProviderFactory = {
                        mock { _ ->
                            on { now }.thenAnswer { currentTime }
                        }
                    }
            )
            val sut = environment.peerDirectory as TransientPeerDirectory
            val knownPeerId = Id("id")
        }
    }

    @Test
    fun `initially the peer directory is empty`() =
            test when_ { sut.allKnownPeersIds } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `adding to the empty peer directory yields a one element directory`() =
            test when_ { sut.add(knownPeerId) } then {
                expect(sut.allKnownPeersIds is_ Equal to_ Values(knownPeerId))
            }

    @Test
    fun `adding the same id twice ignores the second add`() =
            test while_ {
                sut.add(knownPeerId)
            } when_ {
                sut.add(knownPeerId)
            } then {
                expect(sut.allKnownPeersIds is_ Equal to_ Values(knownPeerId))
            }

    @Test
    fun `setting a peer unreachable removes it from the list of known peers`() =
            test while_ {
                sut.add(knownPeerId)
            } when_ {
                sut.setUnreachable(knownPeerId)
            } then {
                expect(sut.allKnownPeersIds is_ Empty)
            }

    @Test
    fun `an unreachable peer is automatically known again after configured time`() =
            test while_ {
                sut.add(knownPeerId)
            } when_ {
                sut.setUnreachable(knownPeerId)
                currentTime += environment.configuration.unreachablePeerSuspendTime
            } then {
                expect(sut.allKnownPeersIds is_ Equal to_ Values(knownPeerId))
            }

    @Test
    fun `if too many known peers, remove peer not contacted for the longest time`() =
            test while_ {
                environment.fullKnownPeers.forEach { sut.add(it) }
            } when_ {
                sut.add(knownPeerId)
            } then {
                expect(sut.allKnownPeersIds is_ Equal to_
                               environment.fullKnownPeers - Id("1") + knownPeerId)
            }

    @Test
    fun `adding a peer makes sure it is removed as the last one`() =
            test while_ {
                environment.fullKnownPeers.forEach { sut.add(it) }
            } when_ {
                sut.add(Id("1"))
                sut.add(knownPeerId)
            } then {
                expect(sut.allKnownPeersIds is_ Equal to_
                               environment.fullKnownPeers - Id("2") + knownPeerId)
            }

    @Test
    fun `unreachable peers are removed first`() =
            test while_ {
                environment.fullKnownPeers.forEach { sut.add(it) }
            } when_ {
                sut.setUnreachable(Id("2"))
                sut.add(knownPeerId)
            } then {
                expect(sut.allKnownPeersIds is_ Equal to_
                               environment.fullKnownPeers - Id("2") + knownPeerId)
            }

    @Test
    fun `the peer which became unreachable first is removed first`() =
            test while_ {
                environment.fullKnownPeers.forEach { sut.add(it) }
            } when_ {
                sut.setUnreachable(Id("1"))
                sut.setUnreachable(Id("2"))
                sut.add(knownPeerId)
                currentTime += environment.configuration.unreachablePeerSuspendTime
            } then {
                expect(sut.allKnownPeersIds is_ Equal to_
                               environment.fullKnownPeers - Id("1") + knownPeerId)
            }

    private val PeerTestEnvironment.fullKnownPeers get() =
        (1..configuration.maxKnownPeers).map { Id(it.toString()) }.toSet()
}