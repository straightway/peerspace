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

import straightway.peerspace.data.Id
import straightway.peerspace.data.Identifyable
import straightway.peerspace.data.Key
import straightway.peerspace.data.KeyHashable
import java.io.Serializable

val untimedData = LongRange(0L, 0L)

/**
 * A request for querying data in the peerspace network.
 */
@Suppress("DataClassPrivateConstructor")
data class QueryRequest private constructor(
        val originatorId: Id,
        override val id: Id,
        private val timestampsStart: Long,
        private val timestampsEndInclusive: Long,
        override val epoch: Int? = null
) : KeyHashable, Serializable {

    constructor(originatorId: Id, id: Id, timestamps: ClosedRange<Long>)
            : this(originatorId, id, timestamps.start, timestamps.endInclusive)

    constructor(originatorId: Id, id: Id, timestamps: ClosedRange<Long>, epoch: Int) : this(
            originatorId,
            id,
            timestamps.start,
            timestamps.endInclusive,
            if (timestamps == untimedData) null else epoch)

    constructor(originatorId: Id, id: Id)
            : this(originatorId, id, LongRange(0, 0))

    constructor(originatorId: Id, identifyable: Identifyable)
            : this(originatorId, identifyable.id)

    override val timestamps get() = timestampsStart..timestampsEndInclusive

    override fun toString() = when {
        isUntimed -> untimedStringRepresentation
        else -> timedStringRepresentation
    }

    private val untimedStringRepresentation get() =
        "QueryRequest(${id.identifier}->${originatorId.identifier})"

    private val timedStringRepresentation get() =
        "QueryRequest(${id.identifier}[$rangeStringRepresentation]" +
                "->${originatorId.identifier})"

    private val rangeStringRepresentation get() =
        "$timestampsStart..$timestampsEndInclusive$epochStringRepresentation"

    private val epochStringRepresentation get() =
        if (epoch === null) "" else "|$epoch"

    companion object {
        const val serialVersionUID = 1L
    }
}

fun QueryRequest.isMatching(key: Key) =
        key.timestamp in timestamps && key.id == id

val QueryRequest.isUntimed get() = timestamps == untimedData
