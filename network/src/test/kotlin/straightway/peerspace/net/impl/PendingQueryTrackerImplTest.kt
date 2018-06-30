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

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.PendingQuery
import straightway.peerspace.net.PendingQueryTracker
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Same
import straightway.testing.flow.Values
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.get
import straightway.units.hour
import straightway.units.plus
import straightway.units.second
import java.time.LocalDateTime

class PendingQueryTrackerImplTest : KoinLoggingDisabler() {

    private companion object {
        val queryRequest1 = QueryRequest(Id("originatorId"), Id("chunkId"))
        val queryRequest2 = QueryRequest(Id("originatorId"), Id("otherChunkId"))
        val chunkKey1 = Key(Id("chunkKey1"))
        val chunkKey2 = Key(Id("chunkKey2"))
    }

    private val test get() = Given {
        object {
            val timeout = 1[hour]
            val environment = PeerTestEnvironment(
                    configurationFactory = {
                        Configuration(timedDataQueryTimeout = timeout)
                    },
                    pendingUntimedQueryTrackerFactory = {
                        PendingQueryTrackerImpl { timedDataQueryTimeout }
                    },
                    timeProviderFactory = {
                        mock {
                            on { currentTime }.thenAnswer { currentTime }
                        }
                    })
            val sut get() =
                    environment.get<PendingQueryTracker>("pendingUntimedQueryTracker")
            var currentTime: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0)
        }
    }

    @Test
    fun `pendingQueries is initially empty`() =
            test when_ {
                sut.pendingQueries
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `setPending adds query to collection of pending queries`() =
            test when_ {
                sut.setPending(queryRequest1)
            } then {
                expect(sut.pendingQueries.single().query is_ Same as_ queryRequest1)
            }

    @Test
    fun `setPending stores current time`() =
            test when_ {
                sut.setPending(queryRequest1)
            } then {
                expect(sut.pendingQueries.single().receiveTime is_ Equal to_ currentTime)
            }

    @Test
    fun `setPending called for an already pending request is ignored`() =
            test while_ {
                sut.setPending(queryRequest1)
            } when_ {
                sut.setPending(queryRequest1)
            } then {
                expect(sut.pendingQueries.size is_ Equal to_ 1)
            }

    @Test
    fun `removePendingQueriesIf with true predicate removes all queries`() =
            test while_ {
                sut.setPending(queryRequest1)
            } when_ {
                sut.removePendingQueriesIf { true }
            } then {
                expect(sut.pendingQueries is_ Empty)
            }

    @Test
    fun `removePendingQueriesIf with false predicate removes no queries`() =
            test while_ {
                sut.setPending(queryRequest1)
            } when_ {
                sut.removePendingQueriesIf { false }
            } then {
                expect(sut.pendingQueries.map { it.query } is_ Equal to_ Values(queryRequest1))
            }

    @Test
    fun `removePendingQueriesIf with condition removes matching queries`() =
            test while_ {
                sut.setPending(queryRequest1)
                sut.setPending(queryRequest1.copy(id = Id("otherChunk")))
            } when_ {
                sut.removePendingQueriesIf { id != queryRequest1.id }
            } then {
                expect(sut.pendingQueries.map { it.query } is_ Equal to_ Values(queryRequest1))
            }

    @Test
    fun `automatically remove too old queries`() =
            test while_ {
                sut.setPending(queryRequest1)
                currentTime += timeout + 1[second]
            } when_ {
                sut.pendingQueries
            } then {
                expect(sut.pendingQueries is_ Empty)
            }

    @Test
    fun `addForwardedChunk adds first chunk key to forwardedChunkKeys`() =
            test while_ {
                sut.setPending(queryRequest1)
            } when_ {
                sut.addForwardedChunk(sut.pendingQueries.single(), chunkKey1)
            } then {
                expect(sut.pendingQueries.single().forwardedChunkKeys
                               is_ Equal to_ setOf(chunkKey1))
            }

    @Test
    fun `addForwardedChunk adds new chunk key to forwardedChunkKeys`() =
            test while_ {
                sut.setPending(queryRequest1)
                sut.addForwardedChunk(sut.pendingQueries.single(), chunkKey1)
            } when_ {
                sut.addForwardedChunk(sut.pendingQueries.single(), chunkKey2)
            } then {
                expect(sut.pendingQueries.single().forwardedChunkKeys
                               is_ Equal to_ setOf(chunkKey1, chunkKey2))
            }

    @Test
    fun `addForwardedChunk modifies only specified pending query request`() =
            test while_ {
                sut.setPending(queryRequest1)
                sut.setPending(queryRequest2)
            } when_ {
                sut.addForwardedChunk(sut.pendingQueries.first(), chunkKey1)
            } then {
                expect(sut.pendingQueries is_ Equal to_ Values(
                        PendingQuery(queryRequest2, currentTime),
                        PendingQuery(queryRequest1, currentTime, setOf(chunkKey1))))
            }
}