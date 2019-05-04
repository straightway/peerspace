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
import straightway.peerspace.data.Id
import straightway.utils.getInt
import straightway.utils.toByteArray
import straightway.utils.toHex
import kotlin.experimental.and

/**
 * Representation of a control block within a version 1 data chunk.
 */
class DataChunkControlBlock(
        val type: DataChunkControlBlockType,
        val cpls: Byte,
        val content: ByteArray
) {
    constructor(binary: ByteArray) : this(binary.type, binary.cpls, binary.content)

    val binary get() = byteArrayOf(type.id) + bytes + content
    val binarySize get() = content.size + NON_CONTENT_SIZE

    init {
        if (cpls !in 0..MAX_CPLS)
            throw Panic("Control block type $type: Invalid CPLS ($cpls)")
        if (MAX_CONTENT_SIZE < content.size)
            throw Panic("Control block type $type: Too much data (${content.size})")
    }

    override fun equals(other: Any?) =
            other is DataChunkControlBlock &&
            type == other.type &&
            cpls == other.cpls &&
            content.contentEquals(other.content)

    override fun hashCode() =
            type.hashCode() xor cpls.hashCode() xor content.contentHashCode()

    override fun toString() =
            "DataChunkControlBlock(" +
                    "$type, " +
                    "0x${cpls.toString(HEX)}, " +
                    when (type) {
                        DataChunkControlBlockType.ReferencedChunk ->
                            "${Id(content)})"
                        else ->
                            "(${content.size} bytes)" +
                                    "[${content.joinToString(" ") { it.toHex() }}])"
                    }

    // region Private

    companion object {
        private const val TYPE_SIZE = Byte.SIZE_BYTES
        private const val CPLS_SIZE = 2 * Byte.SIZE_BYTES
        const val NON_CONTENT_SIZE = TYPE_SIZE + CPLS_SIZE
        private const val HEX = 16
        private const val MAX_CPLS = 0xF
        private const val MAX_CONTENT_SIZE = 0xFFF
        private const val CPLS_MASK = 0xF0
        private const val CONTENT_SIZE_HI_MASK: Byte = 0x0F
        private const val CPLS_BITS = 4
        private const val SIZE_BITS = 12
        private const val TYPE_BYTE = 0
        private const val CPLS_SIZE_BYTE1 = 1
        private const val SIZE_BYTE0 = CPLS_SIZE_BYTE1 + 1
        private val ByteArray.type: DataChunkControlBlockType
            get() = DataChunkControlBlockType.values().singleOrNull { it.id == this[TYPE_BYTE] }
                    ?: throw Panic("Control block type: 0x${this[TYPE_BYTE].toString(16)}: Invalid")
        private val ByteArray.cpls: Byte get() =
            ((this[1].toInt() and CPLS_MASK) shr (Byte.SIZE_BITS - CPLS_BITS)).toByte()
        private val ByteArray.contentSize: Int
            get() = byteArrayOf(
                    0,
                    0,
                    this[CPLS_SIZE_BYTE1] and CONTENT_SIZE_HI_MASK,
                    this[SIZE_BYTE0]).getInt()
        private val ByteArray.content: ByteArray get() =
            if (size <= NON_CONTENT_SIZE + contentSize - 1)
                throw Panic("Control block type $type: Not enough content " +
                        "(encoded content size: $contentSize, " +
                        "actual content size: ${size - NON_CONTENT_SIZE})")
            else sliceArray(NON_CONTENT_SIZE until NON_CONTENT_SIZE + contentSize)
    }

    private val bytes = sizeAndCPLSCombined.toByteArray().takeLast(2)
    private val sizeAndCPLSCombined get() = content.size or (cpls.toInt() shl SIZE_BITS)

    // endregion
}