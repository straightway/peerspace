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
import straightway.peerspace.data.Id
import straightway.peerspace.data.isMatching
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.Transmission
import straightway.peerspace.net.TransmissionResultListener
import straightway.random.Chooser

@Suppress("LongParameterList")
fun createPeerMock(
        id: Id,
        pushCallback: (DataPushRequest) -> Unit = { _ -> },
        queryCallback: (DataQueryRequest) -> Unit = { _ -> }
) =
        mock<Peer> { _ ->
            on { this.id }.thenReturn(id)
            on { pushDataChunk(any<DataPushRequest>()) }.thenAnswer {
                pushCallback(it.arguments[0] as DataPushRequest)
            }
            on { queryData(any<DataQueryRequest>()) }.thenAnswer {
                queryCallback(it.arguments[0] as DataQueryRequest)
            }
        }

fun ids(vararg ids: String) = ids.map { Id(it) }

fun createChunkDataStore(initialChunks: () -> List<DataChunk> = { listOf() }): DataChunkStore {
    val chunks: MutableList<DataChunk> = mutableListOf(*initialChunks().toTypedArray())
    return mock { _ ->
        on { store(any()) }.thenAnswer { chunks.add(it.arguments[0] as DataChunk) }
        on { query(any()) }.thenAnswer { args ->
            val query = args.arguments[0] as DataQueryRequest
            chunks.filter { query.query.isMatching(it.key) }
        }
    }
}

fun createNetworkMock(
        transmissionResultListeners: MutableList<TransmissionRecord>,
        peers: () -> Collection<Peer> = { listOf() }
) = mock<Network> { _ ->
    val pendingTransmissions = mutableListOf<() -> Unit>()
    on { scheduleTransmission(any(), any()) }.thenAnswer { args ->
        val transmission = args.arguments[0] as Transmission
        val listener = args.arguments[1] as TransmissionResultListener
        val request = transmission.content
        transmissionResultListeners.add(TransmissionRecord(request, listener))
        val peer = peers().find { it.id == transmission.receiverId }!!
        peer.handle(request)
        listener.notifySuccess()
    }
    on { executePendingRequests() }.thenAnswer { _ ->
        pendingTransmissions.forEach { it() }
        pendingTransmissions.clear()
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