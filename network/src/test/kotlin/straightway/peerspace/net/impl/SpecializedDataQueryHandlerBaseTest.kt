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
import straightway.peerspace.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.PendingQuery
import straightway.peerspace.net.PendingQueryTracker
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given
import java.time.LocalDateTime

class SpecializedDataQueryHandlerBaseTest : KoinLoggingDisabler() {

    private companion object {
        val queryOriginatorId = Id("originatorId")
        val queryRequest = QueryRequest(queryOriginatorId, Id("chunkID"))
        val chunk = Chunk(Key(Id("chunkId")), byteArrayOf())
        val otherChunk = Chunk(Key(Id("otherChunkId")), byteArrayOf())
    }

    private class DerivedSut(isLocalResultPreventingForwarding: Boolean) :
            SpecializedDataQueryHandlerBase(isLocalResultPreventingForwarding) {

        var notifiedChunkKeys = listOf<Key>()
        var pendingQueries = setOf<PendingQuery>()

        public override val pendingQueryTracker by lazy {
            mock<PendingQueryTracker> {
                on { pendingQueries }.thenAnswer { pendingQueries }
            }
        }

        override fun notifyChunkForwarded(key: Key) {
            notifiedChunkKeys += key
        }
    }

    private fun test(isLocalResultPreventingForwarding: Boolean = false) =
        Given {
            object {
                var queryResult = listOf<Chunk>()
                val environment = PeerTestEnvironment(
                        knownPeersIds = listOf(queryOriginatorId),
                        dataQueryHandlerFactory = {
                            DerivedSut(isLocalResultPreventingForwarding)
                        },
                        dataChunkStoreFactory = {
                            mock {
                                on { query(any()) }.thenAnswer { queryResult }
                            }
                        })
                val sut get() = environment.get<DataQueryHandler>() as DerivedSut
                val forwardTracker get() = environment
                        .get<ForwardStateTracker<QueryRequest, QueryRequest>>("queryForwardTracker")
            }
        }

    @Test
    fun `new handled query is set pending`() =
            test() when_ {
                sut.handle(queryRequest)
            } then {
                verify(sut.pendingQueryTracker).setPending(queryRequest)
            }

    @Test
    fun `new handled query is forwarded`() =
            test() when_ {
                sut.handle(queryRequest)
            } then {
                verify(forwardTracker).forward(queryRequest)
            }

    @Test
    fun `new handled query is not forwarded if local result exists and according flag is set`() =
            test(isLocalResultPreventingForwarding = true) while_ {
                queryResult = listOf(chunk)
            } when_ {
                sut.handle(queryRequest)
            } then {
                verify(forwardTracker, never()).forward(queryRequest)
            }

    @Test
    fun `new handled query is forwarded even if local result exists and according flag is set`() =
            test(isLocalResultPreventingForwarding = false) while_ {
                queryResult = listOf(chunk)
            } when_ {
                sut.handle(queryRequest)
            } then {
                verify(forwardTracker).forward(queryRequest)
            }

    @Test
    fun `local result is forwarded to query issuer`() =
            test() while_ {
                queryResult = listOf(chunk, otherChunk)
            } when_ {
                sut.handle(queryRequest)
            } then {
                queryResult.forEach {
                    verify(environment.getPeer(queryRequest.originatorId))
                            .push(PushRequest(environment.peerId, it))
                }
            }

    @Test
    fun `already pending query is not forwarded again`() =
            test() while_ {
                sut.pendingQueries = setOf(PendingQuery(queryRequest, LocalDateTime.MIN))
            } when_ {
                sut.handle(queryRequest)
            } then {
                verify(forwardTracker, never()).forward(queryRequest)
            }
}