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
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.PushRequest
import straightway.testing.bdd.Given

class DataPushForwarderImplTest {

    private companion object {
        val peerId = Id("peerId")
        val queryingPeerId = Id("queryingPeerId")
        val knownPeersIds = ids("1", "2", "3") + queryingPeerId
        val pushingPeerId = knownPeersIds.first()
        val chunk = Chunk(Key(Id("chunkId")), byteArrayOf(1, 2, 3))
        val incomingRequest = PushRequest(pushingPeerId, chunk)
        val forwardedRequest = PushRequest(peerId, chunk)
    }

    private val test get() = Given {
        val result = object : PeerTestEnvironment by PeerTestEnvironmentImpl(
                peerId,
                knownPeersIds = knownPeersIds,
                dataPushForwarder = DataPushForwarderImpl(peerId)
        ) {
            var forwardedPeers = 1..2
            var queryForwardPeerIds = ids()
            init {
                forwardStrategy = mock {
                    on {
                        getPushForwardPeerIdsFor(any(), any())
                    }.thenAnswer {
                        knownPeersIds.slice(forwardedPeers)
                    }
                }
                dataQueryHandler = mock {
                    on {
                        getForwardPeerIdsFor(any())
                    }.thenAnswer {
                        queryForwardPeerIds
                    }
                }
            }
        }

        result.fixed()
        result
    }

    @Test
    fun `push request is forwarded according to forward strategy`() =
            test when_ {
                dataPushForwarder.forward(incomingRequest)
            } then {
                verify(forwardStrategy).getPushForwardPeerIdsFor(chunk.key, ForwardState())
            }

    @Test
    fun `push request is forwarded to peers returned by forward strategy`() =
            test when_ {
                dataPushForwarder.forward(incomingRequest)
            } then {
                forwardedPeers.forEach {
                    verify(knownPeers[it]).push(forwardedRequest)
                }
            }

    @Test
    fun `push request is forwarded to peers which queried the pushed data`() =
            test while_ {
                forwardedPeers = IntRange.EMPTY
                queryForwardPeerIds = listOf(queryingPeerId)
            } when_ {
                dataPushForwarder.forward(incomingRequest)
            } then {
                verify(knownPeers.single { it.id == queryingPeerId }).push(forwardedRequest)
            }

    @Test
    fun `request is forwarded once if same in query and forward strategy `() =
            test while_ {
                forwardedPeers = 1..1
                queryForwardPeerIds = knownPeersIds.slice(forwardedPeers)
            } when_ {
                dataPushForwarder.forward(incomingRequest)
            } then {
                knownPeers
                        .filter { it.id in knownPeersIds.slice(forwardedPeers) }
                        .forEach { verify(it).push(forwardedRequest) }
            }

    @Test
    fun `don't push back to the originator`() =
            test while_ {
                forwardedPeers = 0..0
            } when_ {
                dataPushForwarder.forward(incomingRequest)
            } then {
                verify(getPeer(pushingPeerId), never()).push(any(), any())
            }

    @Test
    fun `DataQueryHandler is notified of forward push`() =
            test when_ {
                dataPushForwarder.forward(incomingRequest)
            } then {
                verify(dataQueryHandler).notifyChunkForwarded(incomingRequest.chunk.key)
            }

    @Test
    fun `DataQueryHandler is notified of incoming data`() {
        val pushRequest = PushRequest(Id("originatorId"), Chunk(Key(Id("pushId")), byteArrayOf()))
        test when_ {
            dataPushForwarder.forward(pushRequest)
        } then {
            verify(dataQueryHandler).getForwardPeerIdsFor(pushRequest.chunk.key)
        }
    }
}
