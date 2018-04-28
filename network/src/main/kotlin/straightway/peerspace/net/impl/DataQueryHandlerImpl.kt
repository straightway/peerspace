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
@file:Suppress("ForbiddenComment")

package straightway.peerspace.net.impl

import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.Infrastructure
import straightway.peerspace.net.InfrastructureProvider
import straightway.peerspace.net.InfrastructureReceiver
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.PushTarget
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.isMatching
import straightway.peerspace.net.isUntimed
import straightway.peerspace.net.untimedData
import straightway.units.Time
import straightway.units.toDuration
import straightway.units.UnitNumber
import java.time.LocalDateTime

/**
 * Handle timed and untimed data queries.
 */
class DataQueryHandlerImpl(private val peerId: Id)
    : DataQueryHandler, InfrastructureReceiver, InfrastructureProvider {

    override lateinit var infrastructure: Infrastructure

    override fun handle(request: QueryRequest) {
        request.setPending()
        pushBackDataQueryResult(request) // TODO: Not pending when untimed and with immediate result
        request.forward() // TODO: Untimed request already satisfied: No need to forward
    }

    override fun getForwardPeerIdsFor(request: PushRequest): Iterable<Id> =
            request.resultReceiverIds.toList().apply { request.markAsHandled() }

    val QueryRequest.forwardCopy get() =
            copy(originatorId = peerId)

    val QueryRequest.forwardPeerIds get() =
            forwardStrategy.getQueryForwardPeerIdsFor(this)

    fun QueryRequest.forward() = forwardCopy.let {
        forwardPeerIds.forEach { peerId -> getQuerySourceFor(peerId).query(it) }
    }

    private fun QueryRequest.setPending() {
        _pendingQueries += PendingQuery(this, timeProvider.currentTime)
    }

    private fun PushRequest.markAsHandled() {
        removeUntimedMatchingQueries()
        addToForwardedChunksOfMatchingQueries()
    }

    private val PushRequest.resultReceiverIds get() =
            pendingQueries
                .filter { !it.forwardedChunks.contains(chunk.key) }
                .map { it.query.originatorId }

    private val PushRequest.pendingQueries get() =
            this@DataQueryHandlerImpl.pendingQueries.filter { it.query.isMatching(chunk.key) }

    private fun PushRequest.removeUntimedMatchingQueries() =
            isUntimed && _pendingQueries.removeIf { it.query.isMatching(chunk.key) }

    private val PushRequest.isUntimed get() =
            chunk.key.timestamp in untimedData

    private fun PushRequest.addToForwardedChunksOfMatchingQueries() =
            pendingQueries.forEach { it.forwardedChunks.add(chunk.key) }

    private fun pushBackDataQueryResult(request: QueryRequest) =
            request.result forwardTo request.pushTarget

    private val QueryRequest.result get() =
            dataChunkStore.query(this)

    private infix fun Iterable<Chunk>.forwardTo(target: PushTarget) =
            forEach { chunk -> chunk forwardTo target }

    private infix fun Chunk.forwardTo(target: PushTarget) =
            target.push(PushRequest(peerId, this))

    private val QueryRequest.pushTarget get() = getPushTargetFor(originatorId)

    private fun getQuerySourceFor(id: Id) = network.getQuerySource(id)

    private data class PendingQuery(
            val query: QueryRequest,
            val receiveTime: LocalDateTime
    ) {
        var forwardedChunks = mutableSetOf<Key>()
    }

    private val pendingQueries: List<PendingQuery> get() {
        removeOldPendingQueries()
        return _pendingQueries
    }

    private fun removeOldPendingQueries() = _pendingQueries.removeAll { it.isTooOld }

    private val PendingQuery.isTooOld get() = when {
        query.isUntimed -> receiveTime < tooOldTimeForUntimedQueries
        else -> receiveTime < tooOldTimeForTimedQueries
    }

    private val tooOldTimeForUntimedQueries get() = getTimeBorder { untimedDataQueryTimeout }

    private val tooOldTimeForTimedQueries get() = getTimeBorder { timedDataQueryTimeout }

    private fun getTimeBorder(duration: Configuration.() -> UnitNumber<Time>) =
            timeProvider.currentTime - configuration.duration().toDuration()

    private val _pendingQueries = mutableListOf<PendingQuery>()
}