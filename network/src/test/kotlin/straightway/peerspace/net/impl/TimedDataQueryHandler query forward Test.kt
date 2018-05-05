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
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given

class `TimedDataQueryHandler query forward Test` {

    companion object {
        val peerId = Id("peerId")
        val queryingPeerId = Id("queryingPeerId")
        val queriedChunkId = Id("queriedChunkId")
        val timedQueriedChunk = Chunk(Key(queriedChunkId, 1), byteArrayOf(1, 2, 3))
        val knownPeersIds = ids("1", "2", "3") + queryingPeerId
        val receivedTimedQueryRequest = QueryRequest(queryingPeerId, queriedChunkId, 0L..83L)
        val forwardedTimedQueryRequest = QueryRequest(peerId, queriedChunkId, 0L..83L)
        val forwardedPeers = 1..2
    }

    private val test get() = Given {
        PeerTestEnvironmentImpl(
                peerId,
                knownPeersIds = knownPeersIds,
                forwardStrategy = mock {
                    on { getQueryForwardPeerIdsFor(any()) }
                            .thenReturn(knownPeersIds.slice(forwardedPeers))
                },
                dataQueryHandler = TimedDataQueryHandler(peerId)).fixed()
    }

    @Test
    fun `query request is forwarded according to forward strategy`() =
            test when_ {
                dataQueryHandler.handle(receivedTimedQueryRequest)
            } then {
                verify(forwardStrategy).getQueryForwardPeerIdsFor(receivedTimedQueryRequest)
            }

    @Test
    fun `query request is forwarded to peers returned by forward strategy`() =
            test when_ {
                dataQueryHandler.handle(receivedTimedQueryRequest)
            } then {
                forwardedPeers.forEach {
                    verify(knownPeers[it]).query(forwardedTimedQueryRequest)
                }
            }

    @Test
    fun `query is forwarded even if local result found`() =
            test while_ {
                dataChunkStore.store(timedQueriedChunk)
            } when_ {
                dataQueryHandler.handle(receivedTimedQueryRequest)
            } then {
                forwardedPeers.forEach {
                    verify(knownPeers[it]).query(forwardedTimedQueryRequest)
                }
            }
}