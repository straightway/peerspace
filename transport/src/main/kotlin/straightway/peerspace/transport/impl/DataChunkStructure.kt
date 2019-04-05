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
import straightway.utils.getUnsignedShort
import straightway.utils.indent
import straightway.utils.joinMultiLine
import straightway.utils.toByteArray
import straightway.utils.toHexBlocks
import straightway.utils.toIntUnsigned

/**
 * Representation of the internal structure of a data chunk.
 */
class DataChunkStructure private constructor(
        val version: Byte,
        val controlBlocks: List<DataChunkControlBlock>,
        val payload: ByteArray,
        private val additionalBytes: Int = 0)
{
    val binary get() = when (version.toIntUnsigned()) {
        0 -> byteArrayOf(version) + payload
        1 -> byteArrayOf(version) + version1AdditionalBytes + payload + version1FilledBytes
        2 -> byteArrayOf(version) + controlBlockBinaries + payloadMarker + payload
        else -> throw Panic("Invalid chunk version: $this")
    }

    fun createChunk(key: Key) = DataChunk(key, binary)

    @Suppress("MagicNumber")
    override fun toString() = "DataChunkStructure " + listOf(
            "version: $version",
            "control blocks " + controlBlocks.joinMultiLine(2),
            "payload (size: ${payload.size}$additionalBytesString):\n" +
                    payload.toHexBlocks(32).indent(2)).joinMultiLine(2)

    private val additionalBytesString get() =
        if (version == 1.toByte()) " + $additionalBytes" else ""

    override fun equals(other: Any?) =
            other is DataChunkStructure && binary.contentEquals(other.binary)

    override fun hashCode() = binary.contentHashCode()

    object Header {
        const val MAX_SUPPORTED_VERSION: Byte = 2
        const val VERSION_FIELD_SIZE = Byte.SIZE_BYTES

        object Version0 {
            const val SIZE = VERSION_FIELD_SIZE
        }

        object Version1 {
            const val ADDITIONAL_BYTES_FIELD_SIZE = Byte.SIZE_BYTES
            const val SIZE = VERSION_FIELD_SIZE + ADDITIONAL_BYTES_FIELD_SIZE
            const val MAX_ADDITIONAL_BYTES =
                    (1 shl (ADDITIONAL_BYTES_FIELD_SIZE * Byte.SIZE_BITS)) - 1
        }

        object Version2 {
            const val CEND_FIELD_SIZE = Byte.SIZE_BYTES
            const val CEND: Byte = 0x00
            const val PAYLOAD_SIZE_FIELD_SIZE = Short.SIZE_BYTES
            const val MIN_SIZE = VERSION_FIELD_SIZE + CEND_FIELD_SIZE + PAYLOAD_SIZE_FIELD_SIZE
        }
    }

    companion object {
        fun fromBinary(binaryData: ByteArray) = when (binaryData.version) {
            0 -> version0(binaryData.sliceArray(1..binaryData.lastIndex))
            1 -> analyzeBinaryVersion1(binaryData)
            2 -> BinaryAnalyzerVersion2(binaryData).result
            else -> throw Panic("Invalid data chunk version: ${binaryData.version}")
        }

        fun version0(payload: ByteArray) =
                DataChunkStructure(0, listOf(), payload)
        fun version1(payload: ByteArray, additionalBytes: Int) =
                DataChunkStructure(1, listOf(), payload, additionalBytes)
        fun version2(controlBlocks: List<DataChunkControlBlock>, payload: ByteArray) =
                DataChunkStructure(2, controlBlocks, payload)

        // region Private companion

        private val shortBytesInIntBinary = 2..3
        private val payloadSizeBytes = 1..2
        private const val payloadStartIndex = 3
        private const val BINARY_AS_STRING_BLOCK_SIZE = 16

        private val ByteArray.version get() = when (size) {
            0 -> throw Panic("Empty binary")
            else -> get(0).toIntUnsigned()
        }

        private fun analyzeBinaryVersion1(binaryData: ByteArray): DataChunkStructure {
            val additionalBytes = binaryData[1].toIntUnsigned()
            return version1(
                    binaryData.sliceArray(
                            Header.Version1.SIZE..(binaryData.lastIndex - additionalBytes)),
                    additionalBytes)
        }

        private class BinaryAnalyzerVersion2(binaryData: ByteArray) {

            val result get() = version2(controlBlocks, payload)

            private val controlBlocks = mutableListOf<DataChunkControlBlock>()
            private var payload = byteArrayOf()

            private fun parseChunkStructure(binaryData: ByteArray) {
                var rest = binaryData
                while (rest.readControlBlock() || rest.readPayload())
                    rest = rest.sliceArray(controlBlocks.last().binarySize..rest.lastIndex)
            }
            private fun ByteArray.readPayload() = false.also {
                checkConsistentPayloadSize()
                payloadRange.also {
                    checkConsistentPayloadRange(it)
                    payload = sliceArray(it)
                }
            }

            private fun ByteArray.checkConsistentPayloadRange(payloadRange: IntRange) {
                if (size <= payloadRange.endInclusive)
                    throw Panic("Data chunk has invalid payload size: $payloadRange vs. $size")
            }

            private fun ByteArray.checkConsistentPayloadSize() {
                if (size <= payloadSizeBytes.endInclusive)
                    throw Panic("Data chunk has invalid payload: " +
                            toHexBlocks(BINARY_AS_STRING_BLOCK_SIZE))
            }

            private val ByteArray.payloadRange
                get() = payloadStartIndex until (payloadStartIndex + payloadSize)
            private val ByteArray.payloadSize
                get() = sliceArray(payloadSizeBytes).getUnsignedShort()
            private fun ByteArray.readControlBlock(): Boolean =
                    (controlBlockType != Header.Version2.CEND).also {
                        if (it) controlBlocks.add(DataChunkControlBlock(this))
                    }
            private val ByteArray.controlBlockType get() =
                firstOrNull() ?: throw Panic("Data chunk control block type or " +
                        "CEND marker not found")

            init {
                parseChunkStructure(binaryData.sliceArray(1..binaryData.lastIndex))
            }
        }

        // endregion
    }

    // region Private

    private val version1AdditionalBytes get() = additionalBytes.toByteArray().last()

    private val version1FilledBytes get() = ByteArray(additionalBytes) { 0 }

    private val controlBlockBinaries: ByteArray get() =
            controlBlocks.fold(byteArrayOf()) { acc, it -> acc + it.binary }

    private val payloadMarker: ByteArray get() =
            byteArrayOf(Header.Version2.CEND) +
            payload.size.toByteArray().sliceArray(shortBytesInIntBinary)

    // endregion
}