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
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.isMatching

/**
 * Track pending queries through time and while chunks come in which my satisfy
 * pending queries.
 */
interface PendingQueryTracker {
    val pendingQueries: List<PendingQuery>
    fun setPending(query: QueryRequest)
    fun removePendingQueriesIf(predicate: QueryRequest.() -> Boolean)
    fun addForwardedChunk(pendingQuery: PendingQuery, chunkKey: Key)
}

fun PendingQueryTracker.isPending(query: QueryRequest) =
        pendingQueries.any { it.query == query }

fun PendingQueryTracker.getPendingQueriesForChunk(chunkKey: Key) =
        pendingQueries.filter { it.query.isMatching(chunkKey) }
