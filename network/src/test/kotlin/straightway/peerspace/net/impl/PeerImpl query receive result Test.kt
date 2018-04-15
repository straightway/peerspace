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
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given
import straightway.units.get
import straightway.units.plus
import straightway.units.second
import straightway.units.toDuration
import java.time.LocalDateTime

class `PeerImpl query receive result Test` {

    companion object {
        val peerId = Id("peerId")
        val queryingPeerId = Id("queryingPeerId")
        val queriedChunkId = Id("queriedChunkId")
        val knownPeersIds = ids("1") + listOf(queryingPeerId)
        val receivedQueryRequest = QueryRequest(queryingPeerId, queriedChunkId)
        val queriedChunk = Chunk(Key(queriedChunkId), byteArrayOf(1, 2, 3))
        val forwardedPeers = 0..0
        val sendTime = LocalDateTime.of(2000, 1, 1, 14, 30)
    }

    private val test get() = Given {
        object : PeerTestEnvironment by PeerTestEnvironmentImpl(
            peerId,
            knownPeersIds = knownPeersIds,
            forwardStrategy = mock {
                on { getQueryForwardPeerIdsFor(any()) }
                        .thenReturn(knownPeersIds.slice(forwardedPeers))
            }
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
    fun `untimed result being pushed back immediately is pushed back on`() =
            test while_ {
                sut.query(receivedQueryRequest)
            } when_ {
                sut.push(PushRequest(queriedChunk))
            } then {
                verify(knownPeers.single { it.id == queryingPeerId })
                        .push(PushRequest(queriedChunk))
            }

    @Test
    fun `untimed result not pushed back after timeout expired`() =
            test while_ {
                sut.query(receivedQueryRequest)
                currTime += (configuration.untimedDataQueryTimeout + 1[second]).toDuration()
            } when_ {
                sut.push(PushRequest(queriedChunk))
            } then {
                verify(knownPeers.single { it.id == queryingPeerId }, never())
                        .push(PushRequest(queriedChunk))
            }
}