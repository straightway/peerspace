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

import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.koinutils.KoinModuleComponent
import straightway.peerspace.koinutils.Bean.inject
import straightway.peerspace.koinutils.Property.property
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.Network
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.PushTarget
import straightway.peerspace.net.QueryRequest

// TODO
// * Remove pending queries for unreachable peers

/**
 * Base class for DataQueryHandler implementations.
 */
abstract class SpecializedDataQueryHandlerBase(
        private val isLocalResultPreventingForwarding: Boolean) :
        DataQueryHandler,
        KoinModuleComponent by KoinModuleComponent() {

    final override fun handle(query: QueryRequest) =
            if (pendingQueryTracker.isPending(query)) Unit else handleNewQueryRequest(query)

    final override fun getForwardPeerIdsFor(chunkKey: Key) =
            resultReceiverIdsForChunk(chunkKey).toList()

    protected abstract fun resultReceiverIdsForChunk(chunkKey: Key): Iterable<Id>
    protected abstract val pendingQueryTracker: PendingQueryTracker

    private val peerId: Id by property("peerId") { Id(it) }
    private val network: Network by inject()
    private val dataChunkStore: DataChunkStore by inject()
    private val forwardTracker: ForwardStateTracker<QueryRequest, QueryRequest>
            by inject("queryForwardTracker")

    private val QueryRequest.result get() = dataChunkStore.query(this)

    private fun handleNewQueryRequest(query: QueryRequest) {
        pendingQueryTracker.setPending(query)
        val hasLocalResult = returnLocalResult(query)
        if (hasLocalResult && isLocalResultPreventingForwarding) return
        forwardTracker.forward(query)
    }

    private fun returnLocalResult(query: QueryRequest): Boolean {
        val localResult = query.result.toList()
        localResult forwardTo query.issuer
        return localResult.any()
    }

    private infix fun Iterable<Chunk>.forwardTo(target: PushTarget) =
            forEach { chunk -> chunk forwardTo target }

    private val QueryRequest.issuer
        get() = network.getPushTarget(originatorId)

    private infix fun Chunk.forwardTo(target: PushTarget) =
            target.push(PushRequest(peerId, this))
}
