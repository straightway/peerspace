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

/**
 * Representation of the internal structure of a data chunk.
 */
class DataChunkStructure(
        val controlBlocks: List<DataChunkControlBlock>,
        val payload: ByteArray)
{
    val version: Byte get() = if (controlBlocks.isEmpty()) 0x00 else 0x01
    val binary get() = byteArrayOf(version) + controlBlockBinaries + payload
    fun createChunk(key: Key) = DataChunk(key, binary)

    companion object {
        operator fun invoke(binaryData: ByteArray) = BinaryAnalyzer(binaryData).result

        // region Private companion

        private class BinaryAnalyzer(binaryData: ByteArray) {

            val result get() = DataChunkStructure(controlBlocks, payload)

            private val controlBlocks = mutableListOf<DataChunkControlBlock>()
            private var payload = byteArrayOf()

            private fun parseChunkStructureVersion1(binaryData: ByteArray) {
                when (binaryData[0].toInt()) {
                    0 -> payload = binaryData.sliceArray(1..binaryData.lastIndex)
                    else -> {
                        val newControlBlock = DataChunkControlBlock(binaryData)
                        controlBlocks.add(newControlBlock)
                        val restIndexRange = newControlBlock.binarySize..binaryData.lastIndex
                        parseChunkStructureVersion1(binaryData.sliceArray(restIndexRange))
                    }
                }
            }

            init {
                val version = binaryData[0].toInt()
                val rest = binaryData.sliceArray(1..binaryData.lastIndex)
                when (version) {
                    0 -> payload = rest
                    1 -> parseChunkStructureVersion1(rest)
                }
            }
        }

        private val CEND = byteArrayOf(0x00)

        // endregion
    }

    // region Private

    private val controlBlockBinaries: ByteArray get() {
        var result = byteArrayOf()
        controlBlocks.forEach { result += it.binary }
        if (result.any()) result += CEND
        return result
    }

    // endregion
}

