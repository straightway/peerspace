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
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataQuery
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.PendingDataQuery
import straightway.peerspace.net.PendingDataQueryTracker
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.Network
import straightway.peerspace.net.Transmission
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Same
import straightway.testing.flow.Values
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import java.time.LocalDateTime

class SpecializedDataQueryHandlerBaseTest : KoinLoggingDisabler() {

    private companion object {
        val queryOriginatorId = Id("originatorId")
        val queriedChunkId = Id("chunkID")
        val untimedQueryRequest =
                DataQueryRequest(queryOriginatorId, DataQuery(queriedChunkId))
        val timedQueryRequest =
                DataQueryRequest(queryOriginatorId, DataQuery(queriedChunkId, 2L..7L))
        val matchingChunk = DataChunk(Key(queriedChunkId), byteArrayOf())
        val otherChunk = DataChunk(Key(Id("otherChunkId")), byteArrayOf())
    }

    private class DerivedSut(isLocalResultPreventingForwarding: Boolean) :
            SpecializedDataQueryHandlerBase(isLocalResultPreventingForwarding) {

        var notifiedChunkKeys = listOf<Key>()
        var pendingQueries = setOf<PendingDataQuery>()
        var chunkForwardFailure: Pair<Key, Id>? = null
        var splitRequests: List<DataQueryRequest>? = null
        var splitSource: DataQueryRequest? = null

        public override val pendingDataQueryTracker by lazy {
            mock<PendingDataQueryTracker> { _ ->
                on { pendingDataQueries }.thenAnswer { pendingQueries }
            }
        }

        override fun onChunkForwarding(key: Key) {
            notifiedChunkKeys += key
        }

        override fun onChunkForwardFailed(chunkKey: Key, targetId: Id) {
            chunkForwardFailure = Pair(chunkKey, targetId)
        }

        override fun splitToEpochs(request: DataQueryRequest): List<DataQueryRequest> {
            splitSource = request
            return splitRequests ?: listOf(request)
        }
    }

    private fun test(isLocalResultPreventingForwarding: Boolean = false) =
        Given {
            object {
                var chunkStoreQueryResult = listOf<DataChunk>()
                val environment = PeerTestEnvironment(
                        knownPeersIds = listOf(queryOriginatorId),
                        dataQueryHandlerFactory = {
                            DerivedSut(isLocalResultPreventingForwarding)
                        },
                        dataChunkStoreFactory = {
                            mock { _ ->
                                on { query(any()) }.thenAnswer { chunkStoreQueryResult }
                            }
                        })
                val sut get() = environment.get<DataQueryHandler>("dataQueryHandler")
                        as DerivedSut
                val forwardTracker get() = environment
                        .get<ForwardStateTracker<DataQueryRequest>>("queryForwardTracker")
            }
        }

    @Test
    fun `new handled query is set pending`() =
            test() when_ {
                sut.handle(untimedQueryRequest)
            } then {
                verify(sut.pendingDataQueryTracker).setPending(untimedQueryRequest)
            }

    @Test
    fun `new handled query is forwarded`() =
            test() when_ {
                sut.handle(untimedQueryRequest)
            } then {
                verify(forwardTracker).forward(untimedQueryRequest)
            }

    @Test
    fun `new handled query is not forwarded if local result exists and according flag is set`() =
            test(isLocalResultPreventingForwarding = true) while_ {
                chunkStoreQueryResult = listOf(matchingChunk)
            } when_ {
                sut.handle(untimedQueryRequest)
            } then {
                verify(forwardTracker, never()).forward(untimedQueryRequest)
            }

    @Test
    fun `new handled query is forwarded even if local result exists and according flag is set`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                chunkStoreQueryResult = listOf(matchingChunk)
            } when_ {
                sut.handle(untimedQueryRequest)
            } then {
                verify(forwardTracker).forward(untimedQueryRequest)
            }

    @Test
    fun `local result is forwarded to query issuer`() =
            test() while_ {
                chunkStoreQueryResult = listOf(matchingChunk, otherChunk)
            } when_ {
                sut.handle(untimedQueryRequest)
            } then { _ ->
                chunkStoreQueryResult.forEach {
                    verify(environment.get<Network>()).scheduleTransmission(
                            eq(Transmission(
                                    untimedQueryRequest.originatorId,
                                    DataPushRequest(environment.peerId, it))),
                            any())
                }
            }

    @Test
    fun `already pending query is not forwarded again`() =
            test() while_ {
                sut.pendingQueries = setOf(PendingDataQuery(untimedQueryRequest, LocalDateTime.MIN))
            } when_ {
                sut.handle(untimedQueryRequest)
            } then {
                verify(forwardTracker, never()).forward(untimedQueryRequest)
            }

    @Test
    fun `notifyChunkForwarded pushes chunk to querying peer`() =
            test() while_ {
                chunkStoreQueryResult = listOf(matchingChunk)
                sut.pendingQueries = setOf(PendingDataQuery(untimedQueryRequest, LocalDateTime.MIN))
            } when_ {
                sut.notifyChunkForwarded(matchingChunk.key)
                environment.get<Network>().executePendingRequests()
            } then {
                val pushRequest = DataPushRequest(environment.peerId, matchingChunk)
                val queryOriginator = environment.getPeer(queryOriginatorId)
                verify(queryOriginator).push(eq(pushRequest))
            }

    @Test
    fun `notifyChunkForwarded does not push not matching chunk`() =
            test() while_ {
                chunkStoreQueryResult = listOf(otherChunk)
                sut.pendingQueries = setOf(PendingDataQuery(untimedQueryRequest, LocalDateTime.MIN))
            } when_ {
                sut.notifyChunkForwarded(otherChunk.key)
            } then {
                val queryOriginator = environment.getPeer(queryOriginatorId)
                verify(queryOriginator, never()).push(any<DataPushRequest>())
            }

    @Test
    fun `notifyChunkForwarded calls onChunkForwarded`() =
            test() when_ {
                sut.notifyChunkForwarded(matchingChunk.key)
            } then {
                expect(sut.notifiedChunkKeys is_ Equal to_ Values(matchingChunk.key))
            }

    @Test
    fun `failed chunk forward is signaled`() =
            test() while_ {
                chunkStoreQueryResult = listOf(matchingChunk)
                sut.pendingQueries = setOf(PendingDataQuery(untimedQueryRequest, LocalDateTime.MIN))
                sut.notifyChunkForwarded(matchingChunk.key)
            } when_ {
                environment.transmissionResultListeners.single().listener.notifyFailure()
            } then {
                expect(sut.chunkForwardFailure is_ Equal to_
                               Pair(matchingChunk.key, queryOriginatorId))
            }

    @Test
    fun `timed query is split`() =
            test() when_ {
                sut.handle(timedQueryRequest)
            } then {
                expect(sut.splitSource is_ Same as_ timedQueryRequest)
            }

    @Test
    fun `split epoch results of timed query are forwarded`() =
            test() while_ {
                sut.splitRequests = listOf(
                        timedQueryRequest.copy(query = timedQueryRequest.query.copy(epoch = 1)),
                        timedQueryRequest.copy(query = timedQueryRequest.query.copy(epoch = 2)))
            } when_ {
                sut.handle(timedQueryRequest)
            } then { _ ->
                inOrder(forwardTracker) {
                    sut.splitRequests!!.forEach {
                        verify(forwardTracker).forward(it)
                    }
                }
            }
}