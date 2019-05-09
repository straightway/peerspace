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
import straightway.utils.indent
import straightway.utils.joinMultiLine

/**
 * Representation of the internal structure of a data chunk.
 */
class DataChunkVersion0 constructor(override val payload: ByteArray) : DataChunkStructure
{
    override val version: Byte = 0
    override val binary get() = byteArrayOf(version) + payload

    @Suppress("MagicNumber")
    override fun toString() = "DataChunkVersion0 " + listOf(
            "payload (size: ${payload.size}):\n" +
            payload.hex.indent(2)).joinMultiLine(2)

    override fun equals(other: Any?) =
            other is DataChunkVersion0 && binary.contentEquals(other.binary)

    override fun hashCode() = binary.contentHashCode()

    object Header {
        const val SIZE = DataChunkStructure.VERSION_FIELD_SIZE
    }

    companion object {
        const val VERSION: Byte = 0
        fun fromBinary(binary: ByteArray) =
                if (binary.version != VERSION)
                    throw Panic("Invalid data chunk VERSION: ${binary.version}")
                else DataChunkVersion0(binary.sliceArray(Header.SIZE..binary.lastIndex))

        // region Private companion

        // endregion
    }
}