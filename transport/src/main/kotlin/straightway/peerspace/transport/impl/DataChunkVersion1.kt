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
import straightway.peerspace.transport.impl.DataChunkStructure.Companion.VERSION_FIELD_SIZE
import straightway.utils.indent
import straightway.utils.joinMultiLine
import straightway.utils.toByteArray
import straightway.utils.toIntUnsigned

/**
 * Representation of the internal structure of a data chunk.
 */
class DataChunkVersion1 constructor(
        override val payload: ByteArray,
        private val additionalBytes: Int
) : DataChunkStructure
{
    override val version: Byte = VERSION
    override val binary get() =
        byteArrayOf(version) + numberOfAdditionalBytes + payload + version1FilledBytes

    fun createChunk(key: Key) = DataChunk(key, binary)

    override fun toString() = "DataChunkVersion1 " + listOf(
            "payload (size: ${payload.size} + $additionalBytes):\n" +
                    payload.hex.indent(2)).joinMultiLine(2)

    override fun equals(other: Any?) =
            other is DataChunkVersion1 && binary.contentEquals(other.binary)

    override fun hashCode() = binary.contentHashCode()

    object Header {
        const val ADDITIONAL_BYTES_FIELD_SIZE = Byte.SIZE_BYTES
        const val SIZE = VERSION_FIELD_SIZE + ADDITIONAL_BYTES_FIELD_SIZE
        const val MAX_ADDITIONAL_BYTES =
                (1 shl (ADDITIONAL_BYTES_FIELD_SIZE * Byte.SIZE_BITS)) - 1
    }

    companion object {
        const val VERSION: Byte = 1
        fun fromBinary(binaryData: ByteArray) =
                if (binaryData.version == VERSION) analyzeBinaryVersion1(binaryData)
                else throw Panic("Invalid data chunk VERSION: ${binaryData.version}")

        fun version1(payload: ByteArray, additionalBytes: Int) =
                DataChunkVersion1(payload, additionalBytes)

        // region Private companion

        private val shortBytesInIntBinary = 2..3

        private fun analyzeBinaryVersion1(binaryData: ByteArray): DataChunkVersion1 {
            val additionalBytes = binaryData[1].toIntUnsigned()
            return version1(
                    binaryData.sliceArray(
                            Header.SIZE..(binaryData.lastIndex - additionalBytes)),
                    additionalBytes)
        }

        // endregion
    }

    // region Private

    private val numberOfAdditionalBytes get() = additionalBytes.toByteArray().last()

    private val version1FilledBytes get() = ByteArray(additionalBytes) { 0 }

    // endregion
}