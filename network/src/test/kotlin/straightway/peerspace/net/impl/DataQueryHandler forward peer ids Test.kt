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
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.get
import straightway.units.plus
import straightway.units.second
import straightway.units.toDuration
import straightway.units.year
import java.time.LocalDateTime

class `DataQueryHandler forward peer ids Test` {

    private companion object {
        val peerId = Id("peerId")
        val timedQueryingPeerId = Id("timedQueryingPeerId")
        val untimedQueryingPeerId = Id("untimedQueryingPeerId")
        val queriedChunkId = Id("queriedChunkId")
        val knownPeersIds = ids("1") + listOf(timedQueryingPeerId, untimedQueryingPeerId)
        val untimedQueryRequest = QueryRequest(untimedQueryingPeerId, queriedChunkId)
        val untimedQueryResult = Chunk(Key(queriedChunkId), byteArrayOf(1, 2, 3))
        val untimedResultPushRequest = PushRequest(peerId, untimedQueryResult)
        val timedQueryRequest = QueryRequest(timedQueryingPeerId, queriedChunkId, 1L..2L)
        val timedQueryResult = Chunk(Key(queriedChunkId, 1L), byteArrayOf(1, 2, 3))
        val timedResultPushRequest = PushRequest(peerId, timedQueryResult)
        val otherTimedQueryResult = Chunk(Key(queriedChunkId, 2L), byteArrayOf(1, 2, 3))
        val forwardedPeers = 0..0
    }

    private val test get() = Given {
        val result = object : PeerTestEnvironment by PeerTestEnvironmentImpl(
                peerId,
                knownPeersIds = knownPeersIds,
                forwardStrategy = mock {
                    on { getQueryForwardPeerIdsFor(any()) }
                            .thenReturn(knownPeersIds.slice(forwardedPeers))
                },
                configuration = Configuration(
                        untimedDataQueryTimeout = 10[second],
                        timedDataQueryTimeout = 10[second]),
                dataQueryHandler = DataQueryHandlerImpl(peerId)
        ) {
            var currTime = LocalDateTime.of(2001, 1, 1, 14, 30)
            init {
                timeProvider = mock {
                    on { currentTime }.thenAnswer { currTime }
                }
            }
        }

        result.fixed()
        result
    }

    @Test
    fun `not matching chunk is not forwarded`() =
            test while_ {
                dataQueryHandler.handle(untimedQueryRequest)
            } when_ {
                dataQueryHandler.getForwardPeerIdsFor(
                        PushRequest(peerId, Chunk(Key(Id("otherId")), byteArrayOf())))
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `untimed result being pushed back immediately is forwarded`() =
            test while_ {
                dataQueryHandler.handle(untimedQueryRequest)
            } when_ {
                dataQueryHandler.getForwardPeerIdsFor(untimedResultPushRequest)
            } then {
                expect(it.result is_ Equal to_ Values(untimedQueryingPeerId))
            }

    @Test
    fun `untimed result being received twice is forwarded on only once`() =
            test while_ {
                dataQueryHandler.handle(untimedQueryRequest)
            } when_ {
                dataQueryHandler.getForwardPeerIdsFor(untimedResultPushRequest) +
                dataQueryHandler.getForwardPeerIdsFor(untimedResultPushRequest)
            } then {
                expect(it.result is_ Equal to_ Values(untimedQueryingPeerId))
            }

    @Test
    fun `untimed result being received after another result is forwarded`() =
            test while_ {
                dataQueryHandler.handle(untimedQueryRequest)
            } when_ {
                dataQueryHandler.getForwardPeerIdsFor(timedResultPushRequest) +
                dataQueryHandler.getForwardPeerIdsFor(untimedResultPushRequest)
            } then {
                expect(it.result is_ Equal to_ Values(untimedQueryingPeerId))
            }

    @Test
    fun `untimed result not forwarded after timeout expired`() =
            test while_ {
                delayForwardingOfTimedQueries()
                dataQueryHandler.handle(untimedQueryRequest)
                currTime += (configuration.untimedDataQueryTimeout + 1[second]).toDuration()
            } when_ {
                dataQueryHandler.getForwardPeerIdsFor(untimedResultPushRequest)
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `timed result being received immediately is forwarded`() =
            test while_ {
                dataQueryHandler.handle(timedQueryRequest)
            } when_ {
                dataQueryHandler.getForwardPeerIdsFor(timedResultPushRequest)
            } then {
                expect(it.result is_ Equal to_ Values(timedQueryingPeerId))
            }

    @Test
    fun `timed result not forwarded after timeout expired`() =
            test while_ {
                delayForwardingOfUntimedQueries()
                dataQueryHandler.handle(timedQueryRequest)
                currTime += (configuration.timedDataQueryTimeout + 1[second]).toDuration()
            } when_ {
                dataQueryHandler.getForwardPeerIdsFor(timedResultPushRequest)
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `multiple timed result being received are all forwarded`() =
            test while_ {
                dataQueryHandler.handle(timedQueryRequest)
            } when_ {
                dataQueryHandler.getForwardPeerIdsFor(timedResultPushRequest) +
                dataQueryHandler.getForwardPeerIdsFor(PushRequest(peerId, otherTimedQueryResult))
            } then {
                expect(it.result is_ Equal to_ Values(timedQueryingPeerId, timedQueryingPeerId))
            }

    @Test
    fun `timed result being received twice is forwarded on only once`() =
            test while_ {
                dataQueryHandler.handle(timedQueryRequest)
            } when_ {
                dataQueryHandler.getForwardPeerIdsFor(timedResultPushRequest) +
                dataQueryHandler.getForwardPeerIdsFor(timedResultPushRequest)
            } then {
                expect(it.result is_ Equal to_ Values(timedQueryingPeerId))
            }

    private fun PeerTestEnvironment.delayForwardingOfTimedQueries() {
        configuration = configuration.copy(timedDataQueryTimeout = 1[year])
    }

    private fun PeerTestEnvironment.delayForwardingOfUntimedQueries() {
        configuration = configuration.copy(untimedDataQueryTimeout = 1[year])
    }
}