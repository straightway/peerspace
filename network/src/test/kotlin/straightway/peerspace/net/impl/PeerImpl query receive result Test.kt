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
import com.nhaarman.mockito_kotlin.calls
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given
import straightway.units.get
import straightway.units.plus
import straightway.units.second
import straightway.units.toDuration
import straightway.units.year
import java.time.LocalDateTime

class `PeerImpl query receive result Test` {

    companion object {
        val peerId = Id("peerId")
        val queryingPeerId = Id("queryingPeerId")
        val queriedChunkId = Id("queriedChunkId")
        val knownPeersIds = ids("1") + listOf(queryingPeerId)
        val untimedQueryRequest = QueryRequest(queryingPeerId, queriedChunkId)
        val untimedQueryResult = Chunk(Key(queriedChunkId), byteArrayOf(1, 2, 3))
        val timedQueryRequest = QueryRequest(queryingPeerId, queriedChunkId, 1L..1L)
        val timedQueryResult = Chunk(Key(queriedChunkId, 1L), byteArrayOf(1, 2, 3))
        val forwardedPeers = 0..0
    }

    private val test get() = Given {
        object : PeerTestEnvironment by PeerTestEnvironmentImpl(
            peerId,
            knownPeersIds = knownPeersIds,
            forwardStrategy = mock {
                on { getQueryForwardPeerIdsFor(any()) }
                        .thenReturn(knownPeersIds.slice(forwardedPeers))
            },
            configuration = Configuration(
                    untimedDataQueryTimeout = 10[second],
                    timedDataQueryTimeout = 10[second])
        ) {
            var currTime = LocalDateTime.of(2001, 1, 1, 14, 30)
            init {
                timeProvider = mock {
                    on { currentTime }.thenAnswer { currTime }
                }
            }
        }
    }

    @Test
    fun `not matching chunk is not pushed back as query result`() =
            test while_ {
                sut.query(untimedQueryRequest)
            } when_ {
                sut.push(PushRequest(Chunk(Key(Id("otherId")), byteArrayOf())))
            } then {
                verify(queryingPeer, never()).push(any())
            }

    @Test
    fun `untimed result being pushed back immediately is pushed back on`() =
            test while_ {
                sut.query(untimedQueryRequest)
            } when_ {
                sut.push(PushRequest(untimedQueryResult))
            } then {
                verify(queryingPeer).push(PushRequest(untimedQueryResult))
            }

    @Test
    fun `untimed result being received twice is pushed back on only once`() =
            test while_ {
                sut.query(untimedQueryRequest)
            } when_ {
                sut.push(PushRequest(untimedQueryResult))
                sut.push(PushRequest(untimedQueryResult))
            } then {
                verify(queryingPeer).push(PushRequest(untimedQueryResult))
            }

    @Test
    fun `untimed result being received after another result is pushed back on`() =
            test while_ {
                sut.query(untimedQueryRequest)
            } when_ {
                sut.push(PushRequest(timedQueryResult))
                sut.push(PushRequest(untimedQueryResult))
            } then {
                verify(queryingPeer).push(PushRequest(untimedQueryResult))
            }

    @Test
    fun `untimed result not pushed back after timeout expired`() =
            test while_ {
                delayForwardingOfTimedQueries()
                sut.query(untimedQueryRequest)
                currTime += (configuration.untimedDataQueryTimeout + 1[second]).toDuration()
            } when_ {
                sut.push(PushRequest(untimedQueryResult))
            } then {
                verify(queryingPeer, never()).push(any())
            }

    @Test
    fun `timed result being pushed back immediately is pushed back on`() =
            test while_ {
                sut.query(timedQueryRequest)
            } when_ {
                sut.push(PushRequest(timedQueryResult))
            } then {
                verify(queryingPeer).push(PushRequest(timedQueryResult))
            }

    @Test
    fun `timed result not pushed back after timeout expired`() =
            test while_ {
                delayForwardingOfUntimedQueries()
                sut.query(timedQueryRequest)
                currTime += (configuration.timedDataQueryTimeout + 1[second]).toDuration()
            } when_ {
                sut.push(PushRequest(timedQueryResult))
            } then {
                verify(queryingPeer, never()).push(any())
            }

    @Test
    fun `timed result being received twice is pushed back on twice`() =
            test while_ {
                sut.query(timedQueryRequest)
            } when_ {
                sut.push(PushRequest(timedQueryResult))
                sut.push(PushRequest(timedQueryResult))
            } then {
                inOrder(queryingPeer) {
                    verify(queryingPeer, calls(2)).push(PushRequest(timedQueryResult))
                }
            }

    private val PeerTestEnvironment.queryingPeer get() =
        knownPeers.single { it.id == queryingPeerId }

    private fun PeerTestEnvironment.delayForwardingOfTimedQueries() {
        configuration = configuration.copy(timedDataQueryTimeout = 1[year])
    }

    private fun PeerTestEnvironment.delayForwardingOfUntimedQueries() {
        configuration = configuration.copy(untimedDataQueryTimeout = 1[year])
    }
}