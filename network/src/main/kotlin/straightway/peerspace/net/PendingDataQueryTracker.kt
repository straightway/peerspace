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

package straightway.peerspace.net

import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Key
import straightway.peerspace.data.isMatching

/**
 * Track pending queries through time and while chunks come in which my satisfy
 * pending queries.
 */
interface PendingDataQueryTracker {
    val pendingDataQueries: Set<PendingDataQuery>
    fun setPending(query: Request<DataQuery>)
    fun removePendingQueriesIf(predicate: Request<DataQuery>.() -> Boolean)
    fun addForwardedChunk(pendingQuery: PendingDataQuery, chunkKey: Key)
}

fun PendingDataQueryTracker.isPending(query: DataQuery) =
        pendingDataQueries.any { it.query.content == query }

fun PendingDataQueryTracker.getPendingQueriesForChunk(chunkKey: Key) =
        pendingDataQueries.filter { it.query.content.isMatching(chunkKey) }
