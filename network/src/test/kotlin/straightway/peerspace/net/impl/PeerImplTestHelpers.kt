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
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.isMatching
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.Request
import straightway.random.Chooser

@Suppress("LongParameterList")
fun createPeerMock(
        id: Id,
        pushCallback: (Request<DataChunk>) -> Unit = { _ -> },
        queryCallback: (Request<DataQuery>) -> Unit = { _ -> }
) =
        mock<Peer> { _ ->
            on { this.id }.thenReturn(id)
            on { pushDataChunk(any()) }.thenAnswer {
                @Suppress("UNCHECKED_CAST")
                pushCallback(it.arguments[0] as Request<DataChunk>)
            }
            on { queryData(any()) }.thenAnswer {
                @Suppress("UNCHECKED_CAST")
                queryCallback(it.arguments[0] as Request<DataQuery>)
            }
        }

fun ids(vararg ids: String) = ids.map { Id(it) }

fun createChunkDataStore(initialChunks: () -> List<DataChunk> = { listOf() }): DataChunkStore {
    val chunks: MutableList<DataChunk> = mutableListOf(*initialChunks().toTypedArray())
    return mock { _ ->
        on { store(any()) }.thenAnswer { chunks.add(it.arguments[0] as DataChunk) }
        on { query(any()) }.thenAnswer { args ->
            @Suppress("UNCHECKED_CAST")
            val query = args.arguments[0] as DataQuery
            chunks.filter { query.isMatching(it.key) }
        }
    }
}

@Suppress("LongParameterList")
fun createNetworkMock(
        localPeerId: Id,
        transmissionResultListeners: MutableList<TransmissionRecord>,
        peers: () -> Collection<Peer> = { listOf() }
) = MockedNetworkImpl(localPeerId, transmissionResultListeners, peers).let { net ->
    fun MockedNetworkImpl.setSuspension(value: Boolean) { isSuspended = value }
    mock<MockedNetwork> { _ ->
        on { scheduleTransmission(any(), any()) }.thenAnswer {
            net.scheduleTransmission(it.getArgument(0), it.getArgument(1))
        }
        on { executePendingRequests() }.thenAnswer {
            net.executePendingRequests()
        }
        on { isSuspended }.thenAnswer {
            net.isSuspended
        }
        on { isSuspended = any() }.thenAnswer {
            net.setSuspension(it.getArgument(0))
        }
        on { setUnreachable(any()) }.thenAnswer {
            net.setUnreachable(it.getArgument(0))
        }
    }
}

fun createPeerDirectory(peers: () -> Collection<Peer> = { listOf() }) = mock<PeerDirectory> { _ ->
    on { allKnownPeersIds }.thenAnswer { _ ->
        peers().map { it.id }.toSet()
    }
}

fun createChooser(chosenIds: () -> List<Id> = { listOf() }) = mock<Chooser> { _ ->
    on { chooseFrom(any<List<Id>>(), any()) }.thenAnswer {
        chosenIds().take(it.arguments[1] as Int)
    }
}