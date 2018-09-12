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
import straightway.peerspace.data.DataQuery
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.PendingDataQuery
import straightway.peerspace.net.PendingDataQueryTracker
import straightway.peerspace.net.Request
import straightway.peerspace.net.configuration
import straightway.peerspace.net.isPending
import straightway.peerspace.net.timeProvider
import straightway.units.Time
import straightway.units.minus
import straightway.units.UnitNumber

/**
 * Default implementation of the PendingDataQueryTracker interface.
 */
class PendingDataQueryTrackerImpl(
        private val pendingTimeoutConfiguration: Configuration.() -> UnitNumber<Time>
) : PendingDataQueryTracker, PeerComponent by PeerComponent() {

    override fun setPending(query: Request<DataQuery>) {
        if (!isPending(query.content))
            _pendingQueries += PendingDataQuery(query, timeProvider.now)
    }

    override val pendingDataQueries: Set<PendingDataQuery> get() {
        _pendingQueries = _pendingQueries.filter { !isTooOld }
        return _pendingQueries
    }

    override fun removePendingQueriesIf(predicate: Request<DataQuery>.() -> Boolean) {
        _pendingQueries = _pendingQueries.filter { !query.predicate() }
    }

    override fun addForwardedChunk(pendingQuery: PendingDataQuery, chunkKey: Key) {
        val oldPendingQuery = _pendingQueries.single { it == pendingQuery }
        val newPendingQuery = oldPendingQuery.copy(
                        forwardedChunkKeys = oldPendingQuery.forwardedChunkKeys + chunkKey)
        setPendingQuery(newPendingQuery)
    }

    private fun setPendingQuery(pendingQuery: PendingDataQuery) {
        _pendingQueries = _pendingQueries.update(pendingQuery)
    }

    private val PendingDataQuery.isTooOld get() = receiveTime < tooOldThreshold
    private val pendingTimeout by lazy { configuration.pendingTimeoutConfiguration() }
    private val tooOldThreshold get() = timeProvider.now - pendingTimeout

    private fun <T> Set<T>.filter(predicate: T.() -> Boolean) =
            (this as Iterable<T>).filter(predicate).toSet()
    private fun Set<PendingDataQuery>.update(toUpdate: PendingDataQuery) =
            filter { query != toUpdate.query } + toUpdate

    private var _pendingQueries = setOf<PendingDataQuery>()
}