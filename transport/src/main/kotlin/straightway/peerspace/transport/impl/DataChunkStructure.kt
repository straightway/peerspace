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
import straightway.utils.toHexBlocks
import straightway.utils.toIntUnsigned

/**
 * Base interface for structures representations of data chunks.
 */
interface DataChunkStructure {
    val version: Byte
    val payload: ByteArray
    val binary: ByteArray

    companion object {
        const val MAX_SUPPORTED_VERSION: Byte = 3
        const val VERSION_FIELD_SIZE = Byte.SIZE_BYTES
        const val HEX_STRING_BYTES_PER_LINE = 32

        @Suppress("ComplexMethod")
        fun fromBinary(binary: ByteArray) = when (binary.version) {
            DataChunkVersion0.VERSION -> DataChunkVersion0.fromBinary(binary)
            DataChunkVersion1.VERSION -> DataChunkVersion1.fromBinary(binary)
            DataChunkVersion2.VERSION -> DataChunkVersion2.fromBinary(binary)
            DataChunkVersion3.VERSION -> DataChunkVersion3.fromBinary(binary)
            else -> throw Panic("Invalid data chunk version ${binary.version}")
        }

        val ByteArray.version get() =
                if (isEmpty()) throw Panic("Empty binary")
                else this[0].toIntUnsigned().toByte()

        val ByteArray.hex get() = toHexBlocks(HEX_STRING_BYTES_PER_LINE)
    }
}

fun DataChunkStructure.createChunk(key: Key) = DataChunk(key, binary)
