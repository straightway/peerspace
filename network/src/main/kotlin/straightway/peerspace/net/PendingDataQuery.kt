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

import straightway.peerspace.data.Key
import java.time.LocalDateTime

/**
 * A data record for a pending query.
 */
data class PendingDataQuery(
        val query: DataQueryRequest,
        val receiveTime: LocalDateTime,
        val forwardedChunkKeys: Set<Key> = setOf()) {

    override fun toString() = "Pending($query@$receiveTime$forwardedChunksStringRepresentation)"

    private val forwardedChunksStringRepresentation get() =
            if (forwardedChunkKeys.any())
                ":" + forwardedChunkKeys.sortedBy { it.toString() }.joinToString(",")
            else ""
}