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
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.EpochAnalyzer
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.PendingDataQuery
import straightway.peerspace.net.PendingDataQueryTracker
import straightway.peerspace.net.DataQueryRequest
import straightway.testing.bdd.Given
import straightway.testing.flow.False
import straightway.testing.flow.True
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import java.time.LocalDateTime

private typealias QueryRequestPredicate = DataQueryRequest.() -> Boolean

class TimedDataQueryHandlerTest : KoinLoggingDisabler() {

    private companion object {
        val chunkId = Id("chunkId")
        val otherChunkId = Id("otherChunkId")
        val chunk1 = Chunk(Key(chunkId, 1), byteArrayOf())
        val queryOriginatorId = Id("originatorId")
        val matchingQuery = DataQueryRequest(queryOriginatorId, chunkId, 1L..1L)
        val otherMatchingQuery = DataQueryRequest(queryOriginatorId, chunkId, 1L..2L)
        val notMatchingQuery = DataQueryRequest(queryOriginatorId, otherChunkId)
    }

    private val test get() =
        Given {
            object {
                var epochs = listOf(0)
                var chunkStoreQueryResult = listOf<Chunk>()
                var pendingQueries = setOf<PendingDataQuery>()
                val pendingQueryRemoveDelegates = mutableListOf<QueryRequestPredicate>()
                val epochAnalyzer: EpochAnalyzer = mock {
                    on { getEpochs(any()) }.thenAnswer { epochs }
                }
                val environment = PeerTestEnvironment(
                        knownPeersIds = listOf(queryOriginatorId),
                        dataQueryHandlerFactory = { TimedDataQueryHandler() },
                        pendingTimedDataQueryTrackerFactory = {
                            mock {
                                on { pendingDataQueries }.thenAnswer { pendingQueries }
                                on { removePendingQueriesIf(any()) }.thenAnswer {
                                    @Suppress("UNCHECKED_CAST")
                                    val predicate = (it.arguments[0] as QueryRequestPredicate)
                                    pendingQueryRemoveDelegates.add(predicate)
                                }
                            }
                        },
                        dataChunkStoreFactory = {
                            mock {
                                on { query(any()) }.thenAnswer { chunkStoreQueryResult }
                            }
                        }
                ) {
                    bean { epochAnalyzer }
                }
                val sut get() =
                    environment.get<DataQueryHandler>("dataQueryHandler")
                            as TimedDataQueryHandler
                val pendingQueryTracker get() =
                    environment.get<PendingDataQueryTracker>("pendingTimedQueryTracker")
                val forwardTracker get() =
                    environment.get<ForwardStateTracker<DataQueryRequest, DataQueryRequest>>(
                            "queryForwardTracker")
            }
        }

    @Test
    fun `local result does not prevent forwarding`() =
            test when_ { sut.isLocalResultPreventingForwarding } then {
                expect(it.result is_ False)
            }

    @Test
    fun `notifyChunkForwarded adds chunk id to forwarded chunk for pending query`() =
            test while_ {
                pendingQueries = setOf(matchingQuery.pending, otherMatchingQuery.pending)
            } when_ {
                sut.notifyChunkForwarded(chunk1.key)
            } then {
                pendingQueries.forEach {
                    verify(pendingQueryTracker).addForwardedChunk(it, chunk1.key)
                }
            }

    @Test
    fun `notifyChunkForwarded does not add chunk id to forwarded chunk for other query`() =
            test while_ {
                pendingQueries = setOf(notMatchingQuery.pending)
            } when_ {
                sut.notifyChunkForwarded(chunk1.key)
            } then {
                verify(pendingQueryTracker, never())
                        .addForwardedChunk(notMatchingQuery.pending, chunk1.key)
            }

    @Test
    fun `pending query is removed if matching chunk is received and originator is unreachable`() =
            test while_ {
                chunkStoreQueryResult = listOf(chunk1)
                pendingQueries = setOf(PendingDataQuery(matchingQuery, LocalDateTime.MIN))
                sut.notifyChunkForwarded(chunk1.key)
            } when_ {
                val listenerKey = Pair(queryOriginatorId, chunk1.key)
                environment.pushTransmissionResultListeners[listenerKey]!!.notifyFailure()
            } then {
                val predicate = pendingQueryRemoveDelegates.single()
                expect(predicate(matchingQuery) is_ True)
                expect(predicate(matchingQuery.copy(originatorId = Id("otherId"))) is_ False)
            }

    @Test
    fun `query is split by epochs`() =
            test while_ {
                epochs = listOf(0, 1)
            } when_ {
                sut.handle(matchingQuery)
            } then {
                verify(epochAnalyzer).getEpochs(matchingQuery.timestamps)
                inOrder(forwardTracker) {
                    verify(forwardTracker).forward(matchingQuery.withEpoch(0))
                    verify(forwardTracker).forward(matchingQuery.withEpoch(1))
                }
            }

    private val DataQueryRequest.pending get() = PendingDataQuery(this, LocalDateTime.MIN)
}