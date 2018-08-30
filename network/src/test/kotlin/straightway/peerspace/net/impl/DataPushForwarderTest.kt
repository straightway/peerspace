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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.Request
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataPushForwarderTest : KoinLoggingDisabler() {

    private companion object {
        val chunkId = Id("chunkId")
        val chunk = DataChunk(Key(chunkId), byteArrayOf())
        val originatorId = Id("remotePeerId")
        val pushRequest = Request(originatorId, chunk)
    }

    private val test get() =
        Given {
            object {
                var forwardPeerIds = listOf<Id>()
                var queryForwardPeerIds = listOf<Id>()
                val environment = PeerTestEnvironment(
                        knownPeersIds = ids("peer0", "peer1", "peer2"),
                        pushForwarderFactory = { DataPushForwarder() },
                        forwardStrategyFactory = {
                            mock { _ ->
                                on { getForwardPeerIdsFor(any(), any()) }.thenAnswer {
                                    forwardPeerIds.toSet()
                                }
                            }
                        },
                        dataQueryHandlerFactory = {
                            mock { _ ->
                                on { getForwardPeerIdsFor(any()) }.thenAnswer {
                                    queryForwardPeerIds
                                }
                            }
                        }
                )
                val sut get() = environment.get<Forwarder<DataChunk>>("pushForwarder")
                val forwardStrategy get() = environment.get<ForwardStrategy>()
                val dataQueryHandler get() =
                    environment.get<DataQueryHandler>("dataQueryHandler")
            }
        }

    @Test
    fun `getForwardPeerIdsFor gets forward peer ids from forward strategy`() =
            test while_ {
                forwardPeerIds = environment.knownPeersIds.slice(0..1)
            } when_ {
                sut.getForwardPeerIdsFor(pushRequest, ForwardState())
            } then {
                verify(forwardStrategy).getForwardPeerIdsFor(chunk.key, ForwardState())
                expect(it.result is_ Equal to_ forwardPeerIds.toSet())
            }

    @Test
    fun `getForwardPeerIdsFor ignores duplicates retrieved from forward strategy`() =
            test while_ {
                forwardPeerIds = listOf(
                        environment.knownPeersIds[0], environment.knownPeersIds[0])
            } when_ {
                sut.getForwardPeerIdsFor(pushRequest, ForwardState())
            } then {
                expect(it.result is_ Equal to_ setOf(environment.knownPeersIds[0]))
            }

    @Test
    fun `getForwardPeerIdsFor yields id of peers querying a chunk`() =
            test while_ {
                queryForwardPeerIds = environment.knownPeersIds.slice(0..1)
            } when_ {
                sut.getForwardPeerIdsFor(pushRequest, ForwardState())
            } then {
                verify(dataQueryHandler).getForwardPeerIdsFor(chunk.key)
                expect(it.result is_ Equal to_ queryForwardPeerIds.toSet())
            }

    @Test
    fun `getForwardPeerIdsFor ignores duplicates retrieved from dataQueryHandler`() =
            test while_ {
                queryForwardPeerIds = listOf(
                        environment.knownPeersIds[0], environment.knownPeersIds[0])
            } when_ {
                sut.getForwardPeerIdsFor(pushRequest, ForwardState())
            } then {
                expect(it.result is_ Equal to_ setOf(environment.knownPeersIds[0]))
            }

    @Test
    fun `getForwardPeerIdsFor combines peer ids from forward strategy and dataQueryHandler`() =
            test while_ {
                forwardPeerIds = environment.knownPeersIds.slice(0..0)
                queryForwardPeerIds = environment.knownPeersIds.slice(1..1)
            } when_ {
                sut.getForwardPeerIdsFor(pushRequest, ForwardState())
            } then {
                verify(forwardStrategy).getForwardPeerIdsFor(chunk.key, ForwardState())
                expect(it.result is_ Equal to_ environment.knownPeersIds.slice(0..1).toSet())
            }

    @Test
    fun `getForwardPeerIdsFor ignores duplicates in combined result`() =
            test while_ {
                forwardPeerIds = environment.knownPeersIds.slice(0..0)
                queryForwardPeerIds = environment.knownPeersIds.slice(0..0)
            } when_ {
                sut.getForwardPeerIdsFor(pushRequest, ForwardState())
            } then {
                verify(forwardStrategy).getForwardPeerIdsFor(chunk.key, ForwardState())
                expect(it.result is_ Equal to_ setOf(environment.knownPeersIds[0]))
            }

    @Test
    fun `getForwardPeerIdsFor does not return the originator as push target`() =
            test while_ {
                forwardPeerIds = listOf(originatorId)
                queryForwardPeerIds = listOf(originatorId)
            } when_ {
                sut.getForwardPeerIdsFor(pushRequest, ForwardState())
            } then {
                expect(it.result is_ Empty)
            }
}