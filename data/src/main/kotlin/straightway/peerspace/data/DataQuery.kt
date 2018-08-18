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
package straightway.peerspace.data

import java.io.Serializable

val untimedData = LongRange(0L, 0L)

/**
 * Specification of a query for data.
 */
@Suppress("DataClassPrivateConstructor")
data class DataQuery private constructor(
        override val id: Id,
        private val timestampsStart: Long,
        private val timestampsEndInclusive: Long,
        override val epoch: Int? = null
) : KeyHashable, Serializable {

    constructor(id: Id, timestamps: ClosedRange<Long>)
            : this(id, timestamps.start, timestamps.endInclusive)

    constructor(id: Id, timestamps: ClosedRange<Long>, epoch: Int) : this(
            id,
            timestamps.start,
            timestamps.endInclusive,
            if (timestamps == untimedData) null else epoch)

    constructor(id: Id)
            : this(id, LongRange(0, 0))

    constructor(identifyable: Identifyable)
            : this(identifyable.id)

    fun withEpoch(epoch: Int) = DataQuery(id, timestamps, epoch)

    override val timestamps get() = timestampsStart..timestampsEndInclusive

    override fun toString() = when {
        isUntimed -> untimedStringRepresentation
        else -> timedStringRepresentation
    }

    private val untimedStringRepresentation get() =
        "DataQuery(${id.identifier})"

    private val timedStringRepresentation get() =
        "DataQuery(${id.identifier}[$rangeStringRepresentation])"

    private val rangeStringRepresentation get() =
        "$timestampsStart..$timestampsEndInclusive$epochStringRepresentation"

    private val epochStringRepresentation get() =
        if (epoch === null) "" else "|$epoch"

    companion object {
        const val serialVersionUID = 1L
    }
}

fun DataQuery.isMatching(key: Key) =
        key.timestamp in timestamps && key.id == id

val DataQuery.isUntimed get() = timestamps == untimedData
