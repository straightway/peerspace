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
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Network
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.PushTarget
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.isMatching
import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.toDuration
import straightway.utils.TimeProvider
import java.time.LocalDateTime

// TODO
// * Remove pending queries for unreachable peers

/**
 * Base class for DataQueryHandler implementations.
 */
abstract class SpecializedDataQueryHandlerBase
    : DataQueryHandler, KoinModuleComponent by KoinModuleComponent() {

    protected val peerId: Id by property("peerId") { Id(it) }
    protected abstract val tooOldThreshold: LocalDateTime
    protected abstract val Key.resultReceiverIdsForChunk: Iterable<Id>
    protected abstract fun QueryRequest.forward(hasLocalResult: Boolean)

    final override fun handle(query: QueryRequest) =
            if (query.isPending) Unit else handleNewQueryRequest(query)

    final override fun getForwardPeerIdsFor(chunkKey: Key) =
            chunkKey.resultReceiverIdsForChunk.toList()

    protected val QueryRequest.result get() = dataChunkStore.query(this)

    data class PendingQuery(
            val query: QueryRequest,
            val receiveTime: LocalDateTime
    ) {
        var forwardedChunks = mutableSetOf<Key>()
    }

    protected fun removeQueriesIf(predicate: QueryRequest.() -> Boolean) {
        _pendingQueries.removeIf { it.query.predicate() }
    }

    protected fun QueryRequest.forward() = forwardCopy.let {
        forwardPeerIds.forEach { peerId -> network.getQuerySource(peerId).query(it) }
    }

    protected val Key.pendingQueriesForThisPush
        get() = pendingQueries.filter { it.query.isMatching(this) }

    protected val pendingQueries: MutableList<PendingQuery> get() {
        removeOldPendingQueries()
        return _pendingQueries
    }

    protected val network: Network by inject()
    protected val timeProvider: TimeProvider by inject()

    private val dataChunkStore: DataChunkStore by inject()
    private val forwardStrategy: ForwardStrategy by inject()

    private val QueryRequest.isPending
        get() = pendingQueries.any { it.query == this }

    private fun handleNewQueryRequest(query: QueryRequest) {
        query.setPending()
        val hasLocalResult = returnLocalResult(query)
        query.copy(originatorId = peerId).forward(hasLocalResult)
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

    private val QueryRequest.forwardCopy
        get() = copy(originatorId = peerId)

    private val QueryRequest.forwardPeerIds
        get() = forwardStrategy.getQueryForwardPeerIdsFor(this, ForwardState())

    private fun QueryRequest.setPending() {
        pendingQueries += PendingQuery(this, timeProvider.currentTime)
    }

    private fun removeOldPendingQueries() = _pendingQueries.removeIf { it.isTooOld }

    private val PendingQuery.isTooOld get() = receiveTime < tooOldThreshold

    private val _pendingQueries = mutableListOf<PendingQuery>()
}

fun TimeProvider.nowPlus(duration: UnitNumber<Time>) =
        (currentTime + duration.toDuration())!!
