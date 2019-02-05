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
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.net.EpochAnalyzer
import straightway.peerspace.net.PendingDataQuery
import straightway.peerspace.net.Request
import straightway.peerspace.net.dataQueryHandler
import straightway.peerspace.net.network
import straightway.peerspace.net.pendingTimedDataQueryTracker
import straightway.peerspace.net.queryForwarder
import straightway.testing.bdd.Given
import straightway.testing.flow.False
import straightway.testing.flow.True
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import java.time.LocalDateTime

private typealias QueryRequestPredicate = Request<DataChunkQuery>.() -> Boolean

class TimedDataChunkQueryHandlerTest : KoinLoggingDisabler() {

    private companion object {
        val chunkId = Id("chunkId")
        val otherChunkId = Id("otherChunkId")
        val chunk1 = DataChunk(Key(chunkId, 1), byteArrayOf())
        val queryOriginatorId = Id("remotePeerId")
        val matchingQuery = Request(queryOriginatorId, DataChunkQuery(chunkId, 1L..1L))
        val otherMatchingQuery = Request(queryOriginatorId, DataChunkQuery(chunkId, 1L..2L))
        val notMatchingQuery = Request(queryOriginatorId, DataChunkQuery(otherChunkId))
    }

    private val test get() =
        Given {
            object {
                var epochs = listOf(0)
                var chunkStoreQueryResult = listOf<DataChunk>()
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
                    environment.dataQueryHandler as TimedDataQueryHandler
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
                    verify(environment.pendingTimedDataQueryTracker)
                            .addForwardedChunk(it, chunk1.key)
                }
            }

    @Test
    fun `notifyChunkForwarded does not add chunk id to forwarded chunk for other query`() =
            test while_ {
                pendingQueries = setOf(notMatchingQuery.pending)
            } when_ {
                sut.notifyChunkForwarded(chunk1.key)
            } then {
                verify(environment.pendingTimedDataQueryTracker, never())
                        .addForwardedChunk(notMatchingQuery.pending, chunk1.key)
            }

    @Test
    fun `pending query is removed if matching chunk is received and originator is unreachable`() =
            test while_ {
                chunkStoreQueryResult = listOf(chunk1)
                pendingQueries = setOf(PendingDataQuery(matchingQuery, LocalDateTime.MIN))
                sut.notifyChunkForwarded(chunk1.key)
                environment.network.executePendingRequests()
            } when_ {
                environment.transmissionResultListeners.single().listener.notifyFailure()
            } then {
                val predicate = pendingQueryRemoveDelegates.single()
                expect(predicate(matchingQuery) is_ True)
                expect(predicate(Request(Id("otherQueryer"), matchingQuery.content)) is_ False)
            }

    @Test
    fun `query is forwarded`() =
            test when_ {
                sut.handle(matchingQuery)
            } then {
                verify(environment.queryForwarder)
                        .forward(Request(matchingQuery.remotePeerId,
                                         matchingQuery.content))
            }

    private val Request<DataChunkQuery>.pending get() = PendingDataQuery(this, LocalDateTime.MIN)
}