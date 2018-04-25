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

import straightway.peerspace.data.Id
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.Infrastructure
import straightway.peerspace.net.InfrastructureProvider
import straightway.peerspace.net.InfrastructureReceiver
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.isMatching
import straightway.peerspace.net.isUntimed
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
        _pendingQueries +=
                PendingQuery(request, timeProvider.currentTime)

        pushBackDataQueryResult(request)

        val forwardedRequest = request.copy(originatorId = peerId)
        forwardStrategy.getQueryForwardPeerIdsFor(request).forEach {
            getQuerySourceFor(it).query(forwardedRequest)
        }
    }

    override fun getForwardPeerIdsFor(request: PushRequest): Iterable<Id> {
        val result = pendingQueries
                .filter { it.query.isMatching(request.chunk.key) }
                .map { it.query.originatorId }
                .toList()

        _pendingQueries.removeIf {
            it.query.isUntimed && it.query.isMatching(request.chunk.key)
        }

        return result
    }

    private fun pushBackDataQueryResult(request: QueryRequest) {
        val originator by lazy { request.pushTarget }
        val queryResult = dataChunkStore.query(request)
        queryResult.forEach { originator.push(PushRequest(peerId, it)) }
    }

    private val QueryRequest.pushTarget get() = getPushTargetFor(originatorId)

    private fun getQuerySourceFor(id: Id) = network.getQuerySource(id)

    private data class PendingQuery(val query: QueryRequest, val receiveTime: LocalDateTime)

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