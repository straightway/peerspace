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
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.Request
import straightway.peerspace.net.TransmissionResultListener
import straightway.peerspace.net.handle
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

interface MockedNetwork : Network {
    var isSuspended: Boolean
    fun setUnreachable(peerId: Id)
}

@Suppress("LongParameterList")
fun createNetworkMock(
        localPeerId: Id,
        transmissionResultListeners: MutableList<TransmissionRecord>,
        peers: () -> Collection<Peer> = { listOf() }
): Network {
    val result = mock<MockedNetwork> { _ ->
        var unreachablePeers = mutableSetOf<Id>()
        var isSuspended = false
        val suspendedTransmissions = mutableListOf<() -> Unit>()
        val pendingTransmissions = mutableListOf<() -> Unit>()
        on { scheduleTransmission(any(), any()) }.thenAnswer { args ->
            pendingTransmissions.add {
                val transmission = args.arguments[0] as Request<*>
                val listener = args.arguments[1] as TransmissionResultListener
                if (transmission.remotePeerId in unreachablePeers)
                    listener.notifyFailure()
                else {
                    val request = transmission.content
                    transmissionResultListeners.add(TransmissionRecord(request, listener))
                    val peer = peers().find { it.id == transmission.remotePeerId }!!
                    peer.handle(Request.createDynamically(localPeerId, request))
                    listener.notifySuccess()
                }
            }
        }
        on { executePendingRequests() }.thenAnswer { _ ->
            if (isSuspended)
                suspendedTransmissions.addAll(pendingTransmissions)
            else pendingTransmissions.forEach { it() }
            pendingTransmissions.clear()
        }
        on { this.isSuspended }.thenAnswer { isSuspended }
        on { this.isSuspended = any() }.then {
            val isSuspending: Boolean = it.getArgument(0)
            if (isSuspending != isSuspended) {
                isSuspended = isSuspending
                if (!isSuspended) {
                    suspendedTransmissions.forEach { it() }
                    suspendedTransmissions.clear()
                }
            }
        }
        on { setUnreachable(any()) }.then { unreachablePeers.add(it.getArgument(0)) }
    }

    return result
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