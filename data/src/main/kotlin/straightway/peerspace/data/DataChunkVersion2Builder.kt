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

import kotlin.math.max

/**
 * Builder class for data chunks (via DataChunkStructure) of version 1.
 */
class DataChunkVersion2Builder(val chunkSize: Int) {

    companion object {
        const val VERSION: Byte = 2
    }

    var signMode: DataChunkSignMode = DataChunkSignMode.NoKey
    var signature: ByteArray? = null
    var publicKey: ByteArray? = null
    var contentKey: ByteArray? = null
    var references = listOf<ByteArray>()
    var payload = byteArrayOf()

    fun setPayloadPart(fullPayload: ByteArray): ByteArray {
        val startIndex = max(0, fullPayload.size - availablePayloadBytes)
        payload = fullPayload.sliceArray(startIndex..fullPayload.lastIndex)
        return fullPayload.sliceArray(0 until startIndex)
    }

    val signablePart get() =
        getDataChunkStructure(signableBlocks).binary.allBytesExceptVersion

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
    private val controlBlocks get() = signatureBlock + signableBlocks
    private val signableBlocks get() = publicKeyBlock + contentKeyBlock + referenceBlocks
    private val signatureBlock get() =
            createControlBlock(DataChunkControlBlockType.Signature, signature, signMode.id)
    private val publicKeyBlock get() =
            createControlBlock(DataChunkControlBlockType.PublicKey, publicKey)
    private val contentKeyBlock get() =
            createControlBlock(DataChunkControlBlockType.ContentKey, contentKey)
    private val referenceBlocks get() = references.map {
        DataChunkControlBlock(DataChunkControlBlockType.ReferencedChunk, 0, it)
    }
    @Suppress("LongParameterList")
    private fun createControlBlock(
            type: DataChunkControlBlockType, content: ByteArray?, cpls: Byte = 0
    ) =
            if (content == null) listOf() else listOf(DataChunkControlBlock(type, cpls, content))
    private val ByteArray.allBytesExceptVersion get() = sliceArray(1..lastIndex)

    // endregion
}