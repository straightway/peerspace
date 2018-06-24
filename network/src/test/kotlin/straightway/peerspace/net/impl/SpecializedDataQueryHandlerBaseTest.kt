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
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.koinutils.Bean.inject
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Network
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.TransmissionResultListener
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.True
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.get
import straightway.units.minute
import straightway.units.plus
import straightway.units.second
import straightway.units.toDuration
import java.time.LocalDateTime

class SpecializedDataQueryHandlerBaseTest : KoinTestBase() {

    private companion object {
        val peerId = Id("peerId")
        val anyPeerId = Id("anyPeerId")
        val originatorId = Id("originatorId")
        val forwardPeerId = Id("forwardPeerId")
        val chunk = Chunk(Key(Id("ChunkId")), byteArrayOf())
        val queryRequest = QueryRequest(originatorId, Id("queriedId"), 1L..2L)
        val pendingTimeout = 5[minute]
    }

    private var currentTime = LocalDateTime.of(2000, 1, 1, 0, 0)

    private fun test(isLocalResultPreventingForwarding: Boolean) =
        Given {
            PeerTestEnvironment(
                    peerId = peerId,
                    knownPeersIds = listOf(originatorId, forwardPeerId, anyPeerId),
                    configurationFactory = {
                        Configuration(timedDataQueryTimeout = pendingTimeout)
                    },
                    dataChunkStoreFactory = {
                        mock {
                            on { query(any()) }.thenAnswer { localChunks }
                        }
                    },
                    dataQueryHandlerFactory = {
                        DerivedSut(isLocalResultPreventingForwarding)
                    },
                    timeProviderFactory = {
                        mock {
                            on { currentTime }.thenAnswer { currentTime }
                        }
                    },
                    forwardStrategyFactory = {
                        mock {
                            on { getQueryForwardPeerIdsFor(any(), any()) }
                                    .thenReturn(listOf(forwardPeerId))
                        }
                    },
                    queryForwarderFactory = {
                        val queryForwarder = QueryForwarder()
                        mock {
                            on { forwardTo(any(), any(), any()) }.thenAnswer {
                                val request = it.arguments[1] as QueryRequest
                                queryForwarder.forwardTo(
                                        it.arguments[0] as Id,
                                        request,
                                        it.arguments[2] as TransmissionResultListener)
                                sut.forwardedQueries.add(request)
                            }
                            on { getKeyFor(any()) }.thenAnswer {
                                queryForwarder.getKeyFor(
                                        it.arguments[0] as QueryRequest)
                            }
                            on { getForwardPeerIdsFor(any(), any()) }.thenAnswer {
                                queryForwarder.getForwardPeerIdsFor(
                                        it.arguments[0] as QueryRequest,
                                        it.arguments[1] as ForwardState)
                            }
                        }
                    },
                    pendingTimedQueryTrackerFactory = {
                        PendingQueryTrackerImpl({ timedDataQueryTimeout })
                    },
                    queryForwardTrackerFactory = {
                        ForwardStateTrackerImpl(get("queryForwarder"))
                    })
        }

    private val PeerTestEnvironment.sut get() =
            get<DataQueryHandler>() as DerivedSut
    private val PeerTestEnvironment.originator get() =
        getPeer(originatorId)
    private val PeerTestEnvironment.forwardPeer get() =
        getPeer(forwardPeerId)
    private infix fun PeerTestEnvironment.isPending(query: QueryRequest) =
            sut.pendingQueries.any { it.query === query }

    private class DerivedSut(isLocalResultPreventingForwarding: Boolean) :
            SpecializedDataQueryHandlerBase(
                    isLocalResultPreventingForwarding = isLocalResultPreventingForwarding) {
        override fun resultReceiverIdsForChunk(chunkKey: Key) = resultReceiverIds

        val resultReceiverIds = mutableListOf<Id>()
        val forwardedQueries = mutableListOf<QueryRequest>()
        val pendingQueries get() = pendingQueryTracker.pendingQueries

        override fun notifyChunkForwarded(key: Key) {
            handledPushRequests += key
        }

        override val pendingQueryTracker: PendingQueryTracker by inject("pendingTimedQueryTracker")

        val handledPushRequests = mutableListOf<Key>()

        fun protectedRemoveQueriesIf(predicate: QueryRequest.() -> Boolean) =
                pendingQueryTracker.removePendingQueriesIf(predicate)

        fun getPendingQueriesForChunk(chunkKey: Key) =
                pendingQueryTracker.getPendingQueriesForChunk(chunkKey)
    }

    @Test
    fun `new handled query is set pending`() =
            test(isLocalResultPreventingForwarding = false) when_ {
                sut.handle(queryRequest)
            } then {
                expect(isPending(queryRequest) is_ True)
            }

    @Test
    fun `pending queries get current timestamp`() =
            test(isLocalResultPreventingForwarding = false) when_ {
                sut.handle(queryRequest)
            } then {
                val pendingQuery = sut.pendingQueries.single()
                expect(pendingQuery.receiveTime is_ Equal to_ currentTime)
            }

    @Test
    fun `local results are queried`() =
            test(isLocalResultPreventingForwarding = false) when_ {
                sut.handle(queryRequest)
            } then {
                verify(get<DataChunkStore>()).query(queryRequest)
            }

    @Test
    fun `local results are forwarded immediately`() =
            test(isLocalResultPreventingForwarding = false) andGiven {
                it.copy(localChunks = it.localChunks
                        + Chunk(Key(Id("chunkId1")), byteArrayOf())
                        + Chunk(Key(Id("chunkId2")), byteArrayOf()))
            } when_ {
                sut.handle(queryRequest)
            } then {
                localChunks.forEach {
                    verify(originator).push(PushRequest(peerId, it))
                }
            }

    @Test
    fun `query is forwarded without local result`() =
            test(isLocalResultPreventingForwarding = true) when_ {
                sut.handle(queryRequest)
            } then {
                expect(sut.forwardedQueries is_ Equal to_ Values(queryRequest))
            }

    @Test
    fun `query is forwarded with local result`() =
            test(isLocalResultPreventingForwarding = false) andGiven {
                it.copy(localChunks = it.localChunks
                        + Chunk(Key(Id("chunkId1")), byteArrayOf()))
            } when_ {
                sut.handle(queryRequest)
            } then {
                expect(sut.forwardedQueries is_ Equal to_ Values(queryRequest))
            }

    @Test
    fun `getForwardPeerIdsFor returns no ids without result receivers`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.resultReceiverIds.clear()
            } when_ {
                sut.getForwardPeerIdsFor(chunk.key)
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `getForwardPeerIdsFor returns ids of result receivers`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.resultReceiverIds += anyPeerId
            } when_ {
                sut.getForwardPeerIdsFor(chunk.key)
            } then {
                expect(it.result is_ Equal to_ Values(anyPeerId))
            }

    @Test
    fun `getForwardPeerIdsFor does not mark push as handled`() =
            test(isLocalResultPreventingForwarding = false) when_ {
                sut.getForwardPeerIdsFor(chunk.key)
            } then {
                expect(sut.handledPushRequests is_ Empty)
            }

    @Test
    fun `getForwardPeerIdsFor marks push as handled after result is returned`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.handle(queryRequest)
                sut.resultReceiverIds.clear()
                sut.resultReceiverIds.add(queryRequest.originatorId)
            } when_ {
                sut.getForwardPeerIdsFor(queryRequest.matchingChunk.key)
            } then {
                expect(it.result is_ Equal to_ Values(queryRequest.originatorId))
            }

    @Test
    fun `removeQueriesIf removes all queries if predicate yields true`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.handle(queryRequest)
            } when_ {
                sut.protectedRemoveQueriesIf { true }
            } then {
                expect(sut.pendingQueries is_ Empty)
            }

    @Test
    fun `removeQueriesIf removes no query if predicate yields false`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.handle(queryRequest)
            } when_ {
                sut.protectedRemoveQueriesIf { false }
            } then {
                expect(sut.pendingQueries.map { it.query }
                               is_ Equal to_ Values(queryRequest))
            }

    @Test
    fun `removeQueriesIf removes ALL queries matching the predicate`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.handle(queryRequest)
                sut.handle(queryRequest.copy(id = Id("otherId1")))
                sut.handle(queryRequest.copy(id = Id("otherId2")))
            } when_ {
                sut.protectedRemoveQueriesIf { id != queryRequest.id }
            } then {
                expect(sut.pendingQueries.map { it.query }
                               is_ Equal to_ Values(queryRequest))
            }

    @Test
    fun `query request forward peer ids are retrieved from forward strategy`() =
            test(isLocalResultPreventingForwarding = false) when_ {
                sut.handle(queryRequest)
            } then {
                verify(get<ForwardStrategy>())
                        .getQueryForwardPeerIdsFor(queryRequest, ForwardState())
            }

    @Test
    fun `query request forward peers are retrieved from network`() =
            test(isLocalResultPreventingForwarding = false) when_ {
                sut.handle(queryRequest)
            } then {
                verify(get<Network>()).getQuerySource(forwardPeerId)
            }

    @Test
    fun `query request with receiver as originator is forwarded`() =
            test(isLocalResultPreventingForwarding = false) when_ {
                sut.handle(queryRequest)
            } then {
                verify(forwardPeer).query(eq(queryRequest.copy(originatorId = peerId)), any())
            }

    @Test
    fun `forwarded query marks forward peer as pending`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.handle(queryRequest)
            } when_ {
                getForwardStateFor(queryRequest)
            } then {
                expect(it.result is_ Equal
                               to_ ForwardState(pending = setOf(forwardPeerId)))
            }

    @Test
    fun `forwarded query request is marked accordingly if it fails`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.handle(queryRequest)
            } when_ {
                queryTransmissionResultListeners.values.single().notifyFailure()
            } then {
                expect(sut.pendingQueries.single() is_ Equal to_
                               PendingQuery(queryRequest, currentTime))
                // The query is re-forwarded to the same peer after failure,
                // so it looks like nothing happened
                expect(getForwardStateFor(queryRequest) is_ Equal to_
                               ForwardState(pending=setOf(forwardPeerId)))
            }

    @Test
    fun `pendingQueriesForThisPush is empty without pending queries`() =
            test(isLocalResultPreventingForwarding = false) when_ {
                sut.getPendingQueriesForChunk(chunk.key)
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `pendingQueriesForThisPush yields matching pending query`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.handle(queryRequest)
            } when_ {
                sut.getPendingQueriesForChunk(queryRequest.matchingChunk.key)
            } then {
                expect(it.result.map { it.query } is_ Equal to_ Values(queryRequest))
            }

    @Test
    fun `pendingQueriesForThisPush yields empty result for non-matching pending query`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.handle(queryRequest)
            } when_ {
                sut.getPendingQueriesForChunk(chunk.key)
            } then {
                expect(it.result.map { it.query } is_ Empty)
            }

    @Test
    fun `too old pending queries are removed`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.handle(queryRequest)
                currentTime += (pendingTimeout + 1[second]).toDuration()
            } when_ {
                sut.pendingQueries
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `handling the same query a second time is ignored`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                sut.handle(queryRequest)
            } when_ {
                sut.handle(queryRequest)
            } then {
                verify(get<DataChunkStore>()).query(queryRequest)
            }

    private val QueryRequest.matchingChunk get() = Chunk(Key(id, timestamps.first), byteArrayOf())

    private fun PeerTestEnvironment.getForwardStateFor(queryRequest: QueryRequest): ForwardState {
        val queryForwardTracker =
                get<ForwardStateTracker<QueryRequest, QueryRequest>>("queryForwardTracker")
        return queryForwardTracker.getStateFor(queryRequest)
    }
}