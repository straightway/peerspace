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

import straightway.error.Panic
import java.io.Serializable

/**
 * A key for a network data chunk.
 */
class Key private constructor(
        override val id: Id,
        val timestamp: Long,
        override val epoch: Int?
) : KeyHashable, Serializable {

    constructor(id: Id) : this(id, 0L, null)
    constructor(id: Id, timestamp: Long, epoch: Int) : this(id, timestamp, epoch as Int?)

    @Suppress("LongParameterList")
    fun copy(id: Id = this.id, timestamp: Long = this.timestamp, epoch: Int? = this.epoch) =
            Key(id, timestamp, epoch)

    override val timestamps get() = LongRange(timestamp, timestamp)

    override fun equals(other: Any?) =
            other is Key &&
            id == other.id &&
            timestamp == other.timestamp &&
            epoch == other.epoch

    override fun hashCode() =
            id.hashCode() xor timestamp.hashCode() xor (epoch?.hashCode() ?: 0)

    override fun toString() =
            when {
                isUntimed -> "Key(${id.identifier})"
                epoch === null -> "Key(${id.identifier}@$timestamp)"
                else -> "Key(${id.identifier}@$timestamp[$epoch])"
            }

    init {
        if (timestamp == 0L && epoch !== null)
            throw Panic("Invalid epoch on key construction " +
                    "(Key(${id.identifier}@$timestamp[$epoch]))")
    }

    companion object {
        const val serialVersionUID = 1L
    }
}

val Key.isUntimed get() = timestamp == 0L