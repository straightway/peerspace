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
import straightway.peerspace.data.DataQuery
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.PendingDataQueryTracker
import straightway.peerspace.net.Request
import straightway.testing.bdd.Given
import straightway.testing.flow.False
import straightway.testing.flow.True
import straightway.testing.flow.expect
import straightway.testing.flow.is_

class UntimedDataQueryHandlerTest : KoinLoggingDisabler() {

    private companion object {
        val chunkId = Id("chunkId")
        val otherChunkId = Id("otherChunkId")
        val chunk = DataChunk(Key(chunkId), byteArrayOf())
    }

    private val test get() =
        Given {
            object {
                val removePredicates = mutableListOf<Request<DataQuery>.() -> Boolean>()
                val environment = PeerTestEnvironment(
                        dataQueryHandlerFactory = { UntimedDataQueryHandler() },
                        pendingUntimedDataQueryTrackerFactory = {
                            mock { _ ->
                                on { removePendingQueriesIf(any()) }.thenAnswer {
                                    @Suppress("UNCHECKED_CAST")
                                    removePredicates.add(
                                            it.arguments[0] as Request<DataQuery>.() -> Boolean)
                                }
                            }
                        }
                )
                val sut get() =
                    environment.get<DataQueryHandler>("dataQueryHandler")
                            as UntimedDataQueryHandler
                val pendingQueryTracker get() =
                    environment.get<PendingDataQueryTracker>("pendingUntimedQueryTracker")
                val forwardTracker get() =
                        environment.get<ForwardStateTracker<DataQuery>>(
                                "queryForwardTracker")
                val predicate get() = removePredicates.single()
            }
        }

    @Test
    fun `local result prevents forwarding`() =
            test when_ { sut.isLocalResultPreventingForwarding } then {
                expect(it.result is_ True)
            }

    @Test
    fun `pending query is removed when matching chunk is forwarded`() =
            test when_ {
                sut.notifyChunkForwarded(chunk.key)
            } then {
                verify(pendingQueryTracker).removePendingQueriesIf(any())
            }

    @Test
    fun `predicate for removal of pending queries yields true for matching query`() =
            test when_ {
                sut.notifyChunkForwarded(chunk.key)
            } then {
                expect(Request(Id("originatorId"), DataQuery(chunkId)).predicate()
                               is_ True)
            }

    @Test
    fun `predicate for removal of pending queries yields false for not matching query`() =
            test when_ {
                sut.notifyChunkForwarded(chunk.key)
            } then {
                expect(Request(Id("originatorId"), DataQuery(otherChunkId)).predicate()
                        is_ False)
            }

    @Test
    fun `splitToEpochs returns list with argument as single element`() {
        val query = Request(Id("originatorId"), DataQuery(Id("ChunkId")))
        test when_ {
            sut.handle(query)
        } then {
            verify(forwardTracker).forward(query)
        }
    }
}