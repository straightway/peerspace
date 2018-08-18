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
import straightway.peerspace.data.Id
import straightway.peerspace.data.KeyHashable

/**
 * A request for querying data in the peerspace network.
 */
data class DataQueryRequest constructor(
        override val originatorId: Id,
        val query: DataQuery
) : KeyHashable by query, Transmittable {

    override val identification get() = query

    override fun withOriginator(newOriginatorId: Id) = copy(originatorId = newOriginatorId)

    override fun toString() = "Request ${originatorId.identifier}: $query"

    companion object {
        const val serialVersionUID = 1L
    }
}
