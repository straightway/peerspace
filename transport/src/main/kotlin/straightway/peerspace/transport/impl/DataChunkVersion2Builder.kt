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

import straightway.peerspace.transport.DataChunkSignMode
import kotlin.math.min

/**
 * Builder class for data chunks (via DataChunkStructure) of version 1.
 */
class DataChunkVersion2Builder(val chunkSize: Int) {

    companion object {
        const val VERSION: Byte = 2
    }

    var publicKey: ByteArray? = null
    var references = listOf<ByteArray>()
    var payload = byteArrayOf()

    fun setPayloadPart(fullPayload: ByteArray): ByteArray {
        payload = fullPayload.sliceArray(0 until min(availablePayloadBytes, fullPayload.size))
        return fullPayload.sliceArray(availablePayloadBytes..fullPayload.lastIndex)
    }

    val availableBytes get() =
        availablePayloadBytes - payload.size
    val availablePayloadBytes get() =
        chunkSize - controlBlocksSize - DataChunkStructure.Header.Version2.MIN_SIZE

    val chunkStructure get() = getDataChunkStructure(controlBlocks)

    // region Private

    private val controlBlocksSize get() =
        controlBlocks.fold(0) { acc, block -> acc + block.binarySize }
    private fun getDataChunkStructure(blocks: List<DataChunkControlBlock>) =
            DataChunkStructure.version2(blocks, payload)
    private val controlBlocks get() = publicKeyBlock + referenceBlocks
    private val publicKeyBlock get() =
            createControlBlock(DataChunkControlBlockType.PublicKey, publicKey)
    private val referenceBlocks get() = references.map {
        DataChunkControlBlock(DataChunkControlBlockType.ReferencedChunk, 0, it)
    }
    @Suppress("LongParameterList")
    private fun createControlBlock(
            type: DataChunkControlBlockType, content: ByteArray?, cpls: Byte = 0
    ) =
            if (content == null) listOf() else listOf(DataChunkControlBlock(type, cpls, content))

    // endregion
}