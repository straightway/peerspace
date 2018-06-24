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

import straightway.peerspace.data.Key
import straightway.peerspace.koinutils.Bean.get
import straightway.peerspace.koinutils.Bean.inject
import straightway.peerspace.koinutils.KoinModuleComponent
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.QueryRequest
import straightway.units.Time
import straightway.utils.TimeProvider
import straightway.units.UnitNumber

/**
 * Default implementation of the PendingQueryTracker interface.
 */
class PendingQueryTrackerImpl(
        private val pendingTimeoutConfiguration: Configuration.() -> UnitNumber<Time>
) : PendingQueryTracker, KoinModuleComponent by KoinModuleComponent() {

    override fun setPending(query: QueryRequest) {
        if (!isPending(query)) _pendingQueries += PendingQuery(query, timeProvider.currentTime)
    }

    override val pendingQueries: List<PendingQuery> get() {
        _pendingQueries = _pendingQueries.filter { !it.isTooOld }
        return _pendingQueries
    }

    override fun removePendingQueriesIf(predicate: QueryRequest.() -> Boolean) {
        _pendingQueries = _pendingQueries.filter { !it.query.predicate() }
    }

    override fun addForwardedChunk(pendingQuery: PendingQuery, chunkKey: Key) {
        val oldPendingQuery = _pendingQueries.single { it == pendingQuery }
        setPendingQuery(oldPendingQuery.copy(
                forwardedChunkKeys = oldPendingQuery.forwardedChunkKeys + chunkKey))
    }

    private fun setPendingQuery(pendingQuery: PendingQuery) {
        _pendingQueries = _pendingQueries.filter { it.query != pendingQuery.query } + pendingQuery
    }

    private val PendingQuery.isTooOld get() = receiveTime < tooOldThreshold

    private val timeProvider: TimeProvider by inject()

    private val pendingTimeout by lazy { get<Configuration>().pendingTimeoutConfiguration() }
    private val tooOldThreshold get() = timeProvider.nowPlus(-pendingTimeout)

    private var _pendingQueries = listOf<PendingQuery>()
}