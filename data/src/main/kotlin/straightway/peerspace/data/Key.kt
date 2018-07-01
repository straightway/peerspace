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

/**
 * A key for a network data chunk.
 */
data class Key(override val id: Id, val timestamp: Long) : KeyHashable, Serializable {

    constructor(id: Id) : this(id, 0)

    override val timestamps get() = LongRange(timestamp, timestamp)

    override fun toString() =
            if (timestamp == 0L) "Key(${id.identifier})" else "Key(${id.identifier}@$timestamp)"

    companion object {
        const val serialVersionUID = 1L
    }
}

val Key.isUntimed get() = timestamp == 0L