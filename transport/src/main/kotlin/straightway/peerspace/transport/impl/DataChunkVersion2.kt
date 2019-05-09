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
import straightway.peerspace.transport.impl.DataChunkStructure.Companion.hex
import straightway.peerspace.transport.impl.DataChunkStructure.Companion.version
import straightway.peerspace.transport.impl.DataChunkStructure.Companion.VERSION_FIELD_SIZE
import straightway.utils.getUnsignedShort
import straightway.utils.indent
import straightway.utils.joinMultiLine
import straightway.utils.toByteArray

/**
 * Representation of the internal structure of a data chunk.
 */
class DataChunkVersion2 constructor(
        val controlBlocks: List<DataChunkControlBlock>,
        override val payload: ByteArray
) : DataChunkStructure
{
    override val version: Byte = VERSION

    override val binary get() =
        byteArrayOf(VERSION) + controlBlockBinaries + payloadMarker + payload

    @Suppress("MagicNumber")
    override fun toString() = "DataChunkVersion2 " + listOf(
            "control blocks " + controlBlocks.joinMultiLine(2),
            "payload (size: ${payload.size}):\n" +
                    payload.hex.indent(2)).joinMultiLine(2)

    override fun equals(other: Any?) =
            other is DataChunkVersion2 && binary.contentEquals(other.binary)

    override fun hashCode() = binary.contentHashCode()

    object Header {
        const val CEND_FIELD_SIZE = Byte.SIZE_BYTES
        const val CEND: Byte = 0x00
        const val PAYLOAD_SIZE_FIELD_SIZE = Short.SIZE_BYTES
        const val MIN_SIZE = VERSION_FIELD_SIZE + CEND_FIELD_SIZE + PAYLOAD_SIZE_FIELD_SIZE
    }

    companion object {
        const val VERSION: Byte = 2
        fun fromBinary(binaryData: ByteArray) =
                if (binaryData.version == VERSION)
                    BinaryAnalyzerVersion2(binaryData).result
                else throw Panic("Invalid data chunk VERSION: ${binaryData.version}")

        // region Private companion

        private val PAYLOAD_SIZE_BYTES = Header.CEND_FIELD_SIZE..Header.CEND_FIELD_SIZE + 1
        private val PAYLOAD_START_INDEX = PAYLOAD_SIZE_BYTES.endInclusive + 1

        private class BinaryAnalyzerVersion2(binaryData: ByteArray) {

            val result get() = DataChunkVersion2(controlBlocks, payload)

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
                if (size <= PAYLOAD_SIZE_BYTES.endInclusive)
                    throw Panic("Data chunk has invalid payload:\n${hex.indent(2)}")
            }

            private val ByteArray.payloadRange
                get() = PAYLOAD_START_INDEX until (PAYLOAD_START_INDEX + payloadSize)
            private val ByteArray.payloadSize
                get() = sliceArray(PAYLOAD_SIZE_BYTES).getUnsignedShort()
            private fun ByteArray.readControlBlock(): Boolean =
                    (controlBlockType != Header.CEND).also {
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

    private val controlBlockBinaries: ByteArray get() =
            controlBlocks.fold(byteArrayOf()) { acc, it -> acc + it.binary }

    private val payloadMarker: ByteArray get() =
            byteArrayOf(Header.CEND) +
            payload.size.toByteArray(Short.SIZE_BYTES)

    // endregion
}