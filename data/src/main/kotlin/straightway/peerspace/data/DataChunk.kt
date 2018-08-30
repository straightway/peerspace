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

import java.util.Arrays

/**
 * A chunk of data with a key.
 */
data class DataChunk(val key: Key, val data: ByteArray) : Transmittable {

    companion object {
        const val serialVersionUID = 1L
        private const val KeyHashFactor = 31
    }

    fun withEpoch(epoch: Int) =
            DataChunk(key.copy(epoch = epoch), data)

    override val id get() = key

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataChunk

        return key == other.key && data contentEquals other.data
    }

    @Suppress("SENSELESS_COMPARISON")
    override fun hashCode() =
            KeyHashFactor * if (key == null) 0 else key.hashCode() +
                if (data == null) 0 else Arrays.hashCode(data)
}