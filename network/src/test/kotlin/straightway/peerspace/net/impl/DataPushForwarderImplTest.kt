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
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.DataPushForwarder
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.PushRequest
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataPushForwarderImplTest : KoinTestBase() {

    private companion object {
        val peerId = Id("peerId")
        val queryingPeerId = Id("queryingPeerId")
        val keepAlivePeerId = Id("keepAlivePeerId")
        val knownPeersIds = ids("1", "2", "3") + queryingPeerId + keepAlivePeerId
        val keepAlivePeerIndex = knownPeersIds.indexOf(keepAlivePeerId)
        val keepAliveRange = keepAlivePeerIndex..keepAlivePeerIndex
        val pushingPeerId = knownPeersIds.first()
        val chunk = Chunk(Key(Id("chunkId")), byteArrayOf(1, 2, 3))
        val incomingRequest = PushRequest(pushingPeerId, chunk)
        val forwardedRequest = PushRequest(peerId, chunk)
        val pushRequest = PushRequest(
                Id("pushOriginatorId"),
                Chunk(Key(Id("pushedChunkId")), byteArrayOf()))

        fun keepingAlive(ids: List<Id> = listOf()) = listOf(keepAlivePeerId) + ids
    }

    private val test get() = Given {
        var localQueryForwardIds = ids()
        var localForwardedPeerIndices = 1..2
        object {
            val environment = PeerTestEnvironment(
                    peerId,
                    knownPeersIds = knownPeersIds,
                    dataPushForwarderFactory = { DataPushForwarderImpl() },
                    dataQueryHandlerFactory = {
                        mock {
                            on {
                                getForwardPeerIdsFor(any())
                            }.thenAnswer {
                                localQueryForwardIds
                            }
                        }
                    },
                    forwardStrategyFactory = {
                        mock {
                            on {
                                getPushForwardPeerIdsFor(any(), any())
                            }.thenAnswer {
                                knownPeersIds.slice(localForwardedPeerIndices)
                            }
                        }
                    }
            )
            var forwardedPeerIndices
                get() = localForwardedPeerIndices
                set(new) { localForwardedPeerIndices = new }
            val forwardedPeerIds get() = knownPeersIds.slice(forwardedPeerIndices)
            var queryForwardPeerIds
                get() = localQueryForwardIds
                set(new) { localQueryForwardIds = new }
            val dataPushForwarder get() = environment.get<DataPushForwarder>()
            val forwardStrategy get() = environment.get<ForwardStrategy>()
            val dataQueryHandler get() = environment.get<DataQueryHandler>()
            val pushTransmissionResultListeners get() = environment.pushTransmissionResultListeners
            val knownPeers get() = environment.knownPeers
            val sut get() = dataPushForwarder as DataPushForwarderImpl
            fun getPeer(id: Id) = environment.getPeer(id)
            fun keepAlive(push: PushRequest) {
                forwardedPeerIndices = keepAliveRange
                dataPushForwarder.forward(push)
            }
            fun <R> suspendForwarding(action: () -> R ): R {
                val oldForwardPeerIndices = forwardedPeerIndices
                forwardedPeerIndices = IntRange.EMPTY
                try {
                    return action()
                } finally {
                    forwardedPeerIndices = oldForwardPeerIndices
                }
            }
        }
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
                forwardedPeerIndices.forEach {
                    verify(knownPeers[it]).push(eq(forwardedRequest), any())
                }
            }

    @Test
    fun `push request is forwarded to peers which queried the pushed data`() =
            test while_ {
                forwardedPeerIndices = IntRange.EMPTY
                queryForwardPeerIds = listOf(queryingPeerId)
            } when_ {
                dataPushForwarder.forward(incomingRequest)
            } then {
                verify(knownPeers.single { it.id == queryingPeerId })
                        .push(eq(forwardedRequest), any())
            }

    @Test
    fun `request is forwarded once if same in query and forward strategy `() =
            test while_ {
                forwardedPeerIndices = 1..1
                queryForwardPeerIds = knownPeersIds.slice(forwardedPeerIndices)
            } when_ {
                dataPushForwarder.forward(incomingRequest)
            } then {
                knownPeers
                        .filter { it.id in knownPeersIds.slice(forwardedPeerIndices) }
                        .forEach { verify(it).push(eq(forwardedRequest), any()) }
            }

    @Test
    fun `don't push back to the originator`() =
            test while_ {
                forwardedPeerIndices = 0..0
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
    fun `DataQueryHandler is asked for peer ids for forwarding a chunk`() =
         test when_ {
            dataPushForwarder.forward(pushRequest)
        } then {
            verify(dataQueryHandler).getForwardPeerIdsFor(pushRequest.chunk.key)
        }

    @Test
    fun `forwarding to one peer leads to a pending state for this peer`() =
            test while_ {
                forwardedPeerIndices = 0..0
            } when_ {
                dataPushForwarder.forward(pushRequest)
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(pending = listOf(knownPeersIds[0])))))
            }

    @Test
    fun `forwarding to two peers leads to a pending state for these peers`() =
            test while_ {
                forwardedPeerIndices = 0..1
            } when_ {
                dataPushForwarder.forward(pushRequest)
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(pending = forwardedPeerIds))))
            }

    @Test
    fun `forwarding again after successful forward keeps success state`() =
            test while_ {
                keepAlive(pushRequest)
                forwardedPeerIndices = 0..0
                dataPushForwarder.forward(pushRequest)
                pushTransmissionResultListeners.values.last().notifySuccess()
                forwardedPeerIndices = 1..1
            } when_ {
                dataPushForwarder.forward(pushRequest)
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(
                                        successful = listOf(knownPeersIds[0]),
                                        pending = keepingAlive(forwardedPeerIds)))))
            }

    @Test
    fun `forwarding again after failed forward keeps failed state`() =
            test while_ {
                keepAlive(pushRequest)
                forwardedPeerIndices = 0..0
                dataPushForwarder.forward(pushRequest)
                suspendForwarding {
                    pushTransmissionResultListeners.values.last().notifyFailure()
                }
                forwardedPeerIndices = 1..1
            } when_ {
                dataPushForwarder.forward(pushRequest)
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(
                                        failed = listOf(knownPeersIds[0]),
                                        pending = keepingAlive(forwardedPeerIds)))))
            }

    @Test
    fun `succeeded forwarding to one peer leads to a succeeded state for this peer`() =
            test while_ {
                keepAlive(pushRequest)
                forwardedPeerIndices = 0..0
                dataPushForwarder.forward(pushRequest)
            } when_ {
                pushTransmissionResultListeners.values.last().notifySuccess()
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(
                                        successful = listOf(knownPeersIds[0]),
                                        pending = keepingAlive()))))
            }

    @Test
    fun `succeeded forwarding to two peers leads to a succeeded state for these peers`() =
            test while_ {
                keepAlive(pushRequest)
                forwardedPeerIndices = 0..1
                dataPushForwarder.forward(pushRequest)
            } when_ {
                suspendForwarding {
                    pushTransmissionResultListeners
                            .filter { it.key.first != keepAlivePeerId }
                            .forEach { it.value.notifySuccess() }
                }
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(
                                        successful = forwardedPeerIds,
                                        pending = keepingAlive()))))
            }

    @Test
    fun `failed forwarding to one peer leads to a failed state for this peer`() =
            test while_ {
                keepAlive(pushRequest)
                forwardedPeerIndices = 0..0
                dataPushForwarder.forward(pushRequest)
            } when_ {
                suspendForwarding {
                    pushTransmissionResultListeners.values.last().notifyFailure()
                }
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(
                                        failed = forwardedPeerIds,
                                        pending = keepingAlive()))))
            }

    @Test
    fun `failed forwarding to two peers leads to a failed state for these peers`() =
            test while_ {
                keepAlive(pushRequest)
                forwardedPeerIndices = 0..1
                dataPushForwarder.forward(pushRequest)
            } when_ {
                suspendForwarding {
                    pushTransmissionResultListeners
                            .filter { it.key.first != keepAlivePeerId }
                            .forEach { it.value.notifyFailure() }
                }
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(
                                        failed = forwardedPeerIds,
                                        pending = keepingAlive()))))
            }

    @Test
    fun `failed forwarding to a peers keeps success state for another peer`() =
            test while_ {
                keepAlive(pushRequest)
                forwardedPeerIndices = 0..1
                dataPushForwarder.forward(pushRequest)
                pushTransmissionResultListeners.values.drop(1).first().notifySuccess()
            } when_ {
                suspendForwarding {
                    pushTransmissionResultListeners.values.last().notifyFailure()
                }
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(
                                        successful = forwardedPeerIds.slice(0..0),
                                        failed = forwardedPeerIds.slice(1..1),
                                        pending = keepingAlive()))))
            }

    @Test
    fun `succeeded forwarding to a peers keeps failed state for another peer`() =
            test while_ {
                keepAlive(pushRequest)
                forwardedPeerIndices = 0..1
                dataPushForwarder.forward(pushRequest)
                suspendForwarding {
                    pushTransmissionResultListeners.values.last().notifyFailure()
                }
            } when_ {
                pushTransmissionResultListeners.values.drop(1).first().notifySuccess()
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(
                                        successful = forwardedPeerIds.slice(0..0),
                                        failed = forwardedPeerIds.slice(1..1),
                                        pending = keepingAlive()))))
            }

    @Test
    fun `if forwarding to a forward strategy result peer failed, ask strategy again`() =
            test while_ {
                forwardedPeerIndices = 0..0
                dataPushForwarder.forward(pushRequest)
            } when_ {
                pushTransmissionResultListeners.values.single().notifyFailure()
            } then {
                inOrder(forwardStrategy) {
                    verify(forwardStrategy).getPushForwardPeerIdsFor(
                            pushRequest.chunk.key,
                            ForwardState())
                    verify(forwardStrategy).getPushForwardPeerIdsFor(
                            pushRequest.chunk.key,
                            ForwardState(failed = listOf(forwardedPeerIds.first())))
                }
            }

    @Test
    fun `forward tp peers provided after re-query of forward strategy`() =
            test while_ {
                forwardedPeerIndices = 0..0
                dataPushForwarder.forward(pushRequest)
                forwardedPeerIndices = 1..1
            } when_ {
                pushTransmissionResultListeners.values.single().notifyFailure()
            } then {
                val reForwardPeer = getPeer(forwardedPeerIds.single())
                verify(reForwardPeer).push(eq(PushRequest(peerId, pushRequest.chunk)), any())
            }

    @Test
    fun `re-transmitted chunk is pending`() =
            test while_ {
                forwardedPeerIndices = 0..0
                dataPushForwarder.forward(pushRequest)
            } when_ {
                pushTransmissionResultListeners.values.single().notifyFailure()
            } then {
                expect(sut.forwardStates is_ Equal to_ Values(
                        Pair(
                                pushRequest.chunk.key,
                                ForwardState(pending = forwardedPeerIds.slice(0..0)))))
            }
}
