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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Network
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.True
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import java.time.LocalDateTime

class SpecializedDataQueryHandlerBaseTest : KoinTestBase() {

    private companion object {
        val peerId = Id("peerId")
        val anyPeerId = Id("anyPeerId")
        val originatorId = Id("originatorId")
        val forwardPeerId = Id("forwardPeerId")
        val chunk = Chunk(Key(Id("ChunkId")), byteArrayOf())
        val queryRequest = QueryRequest(originatorId, Id("queriedId"), 1L..2L)
    }

    private var currentTime = LocalDateTime.of(2000, 1, 1, 0, 0)

    private val test get() =
        Given {
            PeerTestEnvironment(
                    peerId = peerId,
                    knownPeersIds = listOf(originatorId, forwardPeerId, anyPeerId),
                    dataChunkStoreFactory = {
                        mock {
                            on { query(any()) }.thenAnswer { localChunks }
                        }
                    },
                    dataQueryHandlerFactory = { DerivedSut() },
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
                    })
        }

    private val PeerTestEnvironment.sut get() =
            get<DataQueryHandler>() as DerivedSut
    private val PeerTestEnvironment.originator get() =
        getPeer(originatorId)
    private val PeerTestEnvironment.forwardPeer get() =
        getPeer(forwardPeerId)
    private infix fun PeerTestEnvironment.isPending(query: QueryRequest) =
            sut.protectedPendingQueries.any { it.query === query }

    private class DerivedSut : SpecializedDataQueryHandlerBase() {
        override var tooOldThreshold = LocalDateTime.of(2000, 1, 1, 0, 0)!!
        fun changeTooOldThreshold(new: LocalDateTime) { tooOldThreshold = new }

        override val Key.resultReceiverIdsForChunk: Iterable<Id>
            get() = this@DerivedSut.resultReceiverIds

        val resultReceiverIds = mutableListOf<Id>()

        override fun QueryRequest.forward(hasLocalResult: Boolean) {
            forwardedQueries += ForwardedQuery(this, hasLocalResult)
        }

        data class ForwardedQuery(val query: QueryRequest, val hasLocalResult: Boolean)
        val forwardedQueries = mutableListOf<ForwardedQuery>()

        override fun notifyChunkForwarded(key: Key) {
            handledPushRequests += key
        }

        val handledPushRequests = mutableListOf<Key>()

        fun protectedRemoveQueriesIf(predicate: QueryRequest.() -> Boolean) =
                removeQueriesIf(predicate)

        fun protectedForwardQueryRequest(query: QueryRequest) = query.forward()

        fun protectedPendingQueriesForThisPush(key: Key) =
                key.pendingQueriesForThisPush

        val protectedPendingQueries: List<PendingQuery> get() = pendingQueries
    }

    @Test
    fun `new handled query is set pending`() =
            test when_ {
                sut.handle(queryRequest)
            } then {
                expect(isPending(queryRequest) is_ True)
            }

    @Test
    fun `pending queries get current timestamp`() =
            test when_ {
                sut.handle(queryRequest)
            } then {
                val pendingQuery = sut.protectedPendingQueries.single()
                expect(pendingQuery.receiveTime is_ Equal to_ currentTime)
            }

    @Test
    fun `local results are queried`() =
            test when_ {
                sut.handle(queryRequest)
            } then {
                verify(get<DataChunkStore>()).query(queryRequest)
            }

    @Test
    fun `local results are forwarded immediately`() =
            test andGiven {
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
            test when_ {
                sut.handle(queryRequest)
            } then {
                expect(sut.forwardedQueries is_ Equal to_
                               Values(queryRequest.forwarded(hasLocalResult = false)))
            }

    @Test
    fun `query is forwarded with local result`() =
            test andGiven {
                it.copy(localChunks = it.localChunks
                        + Chunk(Key(Id("chunkId1")), byteArrayOf()))
            } when_ {
                sut.handle(queryRequest)
            } then {
                expect(sut.forwardedQueries is_ Equal to_
                               Values(queryRequest.forwarded(hasLocalResult = true)))
            }

    @Test
    fun `getForwardPeerIdsFor returns no ids without result receivers`() =
            test while_ {
                sut.resultReceiverIds.clear()
            } when_ {
                sut.getForwardPeerIdsFor(chunk.key)
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `getForwardPeerIdsFor returns ids of result receivers`() =
            test while_ {
                sut.resultReceiverIds += anyPeerId
            } when_ {
                sut.getForwardPeerIdsFor(chunk.key)
            } then {
                expect(it.result is_ Equal to_ Values(anyPeerId))
            }

    @Test
    fun `getForwardPeerIdsFor does not mark push as handled`() =
            test when_ {
                sut.getForwardPeerIdsFor(chunk.key)
            } then {
                expect(sut.handledPushRequests is_ Empty)
            }

    @Test
    fun `getForwardPeerIdsFor marks push as handled after result is returned`() =
            test while_ {
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
            test while_ {
                sut.handle(queryRequest)
            } when_ {
                sut.protectedRemoveQueriesIf { true }
            } then {
                expect(sut.protectedPendingQueries is_ Empty)
            }

    @Test
    fun `removeQueriesIf removes no query if predicate yields false`() =
            test while_ {
                sut.handle(queryRequest)
            } when_ {
                sut.protectedRemoveQueriesIf { false }
            } then {
                expect(sut.protectedPendingQueries.map { it.query }
                               is_ Equal to_ Values(queryRequest))
            }

    @Test
    fun `removeQueriesIf removes ALL queries matching the predicate`() =
            test while_ {
                sut.handle(queryRequest)
                sut.handle(queryRequest.copy(id = Id("otherId1")))
                sut.handle(queryRequest.copy(id = Id("otherId2")))
            } when_ {
                sut.protectedRemoveQueriesIf { id != queryRequest.id }
            } then {
                expect(sut.protectedPendingQueries.map { it.query }
                               is_ Equal to_ Values(queryRequest))
            }

    @Test
    fun `query request forward peer ids are retrieved from forward strategy`() =
            test when_ {
                sut.protectedForwardQueryRequest(queryRequest)
            } then {
                verify(get<ForwardStrategy>())
                        .getQueryForwardPeerIdsFor(queryRequest, ForwardState())
            }

    @Test
    fun `query request forward peers are retrieved from network`() =
            test when_ {
                sut.protectedForwardQueryRequest(queryRequest)
            } then {
                verify(get<Network>()).getQuerySource(forwardPeerId)
            }

    @Test
    fun `query request with receiver as originator is forwarded`() =
            test when_ {
                sut.protectedForwardQueryRequest(queryRequest)
            } then {
                verify(forwardPeer).query(queryRequest.copy(originatorId = peerId))
            }

    @Test
    fun `forwarded query marks forward peer as pending`() =
            test while_ {
                sut.handle(queryRequest)
                sut.protectedForwardQueryRequest(queryRequest)
            } when_ {
                sut.protectedPendingQueries.single().forwardState
            } then {
                expect(it.result is_ Equal
                               to_ ForwardState(pending = listOf(forwardPeerId)))
            }

    @Test
    @Disabled
    fun `forwarded query request is marked accordingly if it fails`() =
            test while_ {
                sut.handle(queryRequest)
                sut.protectedForwardQueryRequest(queryRequest)
            } when_ {
                queryTransmissionResultListeners.values.single().notifyFailure()
            } then {
                expect(sut.protectedPendingQueries is_ Empty)
            }

    @Test
    fun `pendingQueriesForThisPush is empty without pending queries`() =
            test when_ {
                sut.protectedPendingQueriesForThisPush(chunk.key)
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `pendingQueriesForThisPush yields matching pending query`() =
            test while_ {
                sut.handle(queryRequest)
            } when_ {
                sut.protectedPendingQueriesForThisPush(queryRequest.matchingChunk.key)
            } then {
                expect(it.result.map { it.query } is_ Equal to_ Values(queryRequest))
            }

    @Test
    fun `pendingQueriesForThisPush yields empty result for non-matching pending query`() =
            test while_ {
                sut.handle(queryRequest)
            } when_ {
                sut.protectedPendingQueriesForThisPush(chunk.key)
            } then {
                expect(it.result.map { it.query } is_ Empty)
            }

    @Test
    fun `too old pending queries are removed`() =
            test while_ {
                sut.handle(queryRequest)
                sut.changeTooOldThreshold(LocalDateTime.of(2001, 1, 1, 0, 0))
            } when_ {
                sut.protectedPendingQueries
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `handling the same query a second time is ignored`() =
            test while_ {
                sut.handle(queryRequest)
            } when_ {
                sut.handle(queryRequest)
            } then {
                verify(get<DataChunkStore>()).query(queryRequest)
            }

    private fun QueryRequest.forwarded(hasLocalResult: Boolean) =
            DerivedSut.ForwardedQuery(this, hasLocalResult)

    private val QueryRequest.matchingChunk get() = Chunk(Key(id, timestamps.first), byteArrayOf())
}