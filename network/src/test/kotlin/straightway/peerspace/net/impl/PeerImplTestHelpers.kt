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
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.TransmissionResultListener
import straightway.peerspace.net.isMatching
import straightway.random.Chooser

@Suppress("LongParameterList")
fun createPeerMock(
        id: Id,
        pushCallback: (PushRequest, TransmissionResultListener) -> Unit = { _, _ -> },
        queryCallback: (QueryRequest, TransmissionResultListener) -> Unit = { _, _ -> }
) =
        mock<Peer> {
            on { this.id }.thenReturn(id)
            on { push(any(), any()) }.thenAnswer {
                pushCallback(
                        it.arguments[0] as PushRequest,
                        it.arguments[1] as TransmissionResultListener)
            }
            on { query(any(), any()) }.thenAnswer {
                queryCallback(
                        it.arguments[0] as QueryRequest,
                        it.arguments[1] as TransmissionResultListener)
            }
        }

fun ids(vararg ids: String) = ids.map { Id(it) }

fun createChunkDataStore(initialChunks: () -> List<Chunk> = { listOf() }): DataChunkStore {
    val chunks: MutableList<Chunk> = mutableListOf(*initialChunks().toTypedArray())
    return mock {
        on { store(any()) }.thenAnswer { chunks.add(it.arguments[0] as Chunk) }
        on { query(any()) }.thenAnswer {
            val query = it.arguments[0] as QueryRequest
            chunks.filter { query.isMatching(it.key) }
        }
    }
}

fun createNetworkMock(peers: () -> Collection<Peer> = { listOf() }) = mock<Network> {
    on { getQuerySource(any()) }.thenAnswer { args ->
        peers().find { it.id == args.arguments[0] }!!
    }
    on { getPushTarget(any()) }.thenAnswer { args ->
        peers().find { it.id == args.arguments[0] }!!
    }
}

fun createPeerDirectory(peers: () -> Collection<Peer> = { listOf() }) = mock<PeerDirectory> {
    on { allKnownPeersIds }.thenAnswer {
        peers().map { it.id }
    }
}

fun createChooser(chosenIds: () -> List<Id> = { listOf() }) = mock<Chooser> {
    on { chooseFrom(any<List<Id>>(), any()) }.thenAnswer {
        chosenIds().take(it.arguments[1] as Int)
    }
}