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
package straightway.peerspace.transport.impl

import straightway.error.Panic
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Key
import straightway.peerspace.transport.impl.DataChunkStructure.Companion.hex
import straightway.peerspace.transport.impl.DataChunkStructure.Companion.version
import straightway.utils.getInt
import straightway.utils.indent
import straightway.utils.joinMultiLine
import straightway.utils.toByteArray

/**
 * Representation of the internal structure of a data chunk.
 */
class DataChunkVersion3 constructor(
        val contentKey: ByteArray,
        override val payload: ByteArray
) : DataChunkStructure
{
    override val version = VERSION

    override val binary: ByteArray get() =
            byteArrayOf(version) +
            contentKey.size.toByteArray(Short.SIZE_BYTES) + contentKey +
            payload

    fun createChunk(key: Key) = DataChunk(key, binary)

    override fun toString() = "DataChunkVersion3 " + listOf(
            "content key (size: ${contentKey.size}):\n" +
                    contentKey.hex.indent(2),
            "payload (size: ${payload.size}):\n" +
                    payload.hex.indent(2)).joinMultiLine(2)

    override fun equals(other: Any?) =
            other is DataChunkVersion3 && binary.contentEquals(other.binary)

    override fun hashCode() = binary.contentHashCode()

    object Header {
        const val MAX_CONTENT_KEY_SIZE = 0xFFFF
        const val MIN_SIZE = 3
    }

    companion object {
        const val VERSION: Byte = 3
        fun fromBinary(binary: ByteArray) =
            if (binary.version != VERSION) throw Panic("Invalid binary VERSION: ${binary.version}")
            else DataChunkVersion3(binary.contentKey, binary.payload)

        private val ByteArray.payloadStartIndex get() =
                (byteArrayOf(0, 0) + sliceArray(1..2)).getInt() + 3
        private val ByteArray.payload get() =
            sliceArray(payloadStartIndex..lastIndex)
        private val ByteArray.contentKey get() =
            sliceArray(3 until payloadStartIndex)
    }

    init {
        if (Header.MAX_CONTENT_KEY_SIZE < contentKey.size)
            throw Panic("Content key too long (${contentKey.size})")
    }
}