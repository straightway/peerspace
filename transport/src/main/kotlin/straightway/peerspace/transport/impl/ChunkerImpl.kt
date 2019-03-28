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

import straightway.koinutils.Bean.get
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkControlBlock
import straightway.peerspace.data.DataChunkVersion2Builder
import straightway.peerspace.data.DataChunkStructure
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.Chunker
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.TransportComponent
import straightway.utils.toChunksOfSize

/**
 * Default implementation of the Chunker interface.
 */
class ChunkerImpl(
        private val chunkSizeBytes: Int,
        private val maxReferences: Int
) : Chunker, TransportComponent by TransportComponent() {

    private val hasher get() = get<Hasher>()

    override fun chopToChunks(data: ByteArray, crypto: ChunkerCrypto): Set<DataChunk> {
        val result = mutableSetOf<DataChunk>()
        chopToChunks(data, result)
        return result
    }

    private fun chopToChunks(data: ByteArray, chunks: MutableSet<DataChunk>): DataChunk {
        val treeDepthInfo = getTreeDepthInfoForSize(data.size)
        if (treeDepthInfo.depth == 0) {
            val result = data.createPlainChunkStructure().createChunk()
            chunks.add(result)
            return result
        } else {
            val directoryDataSize = maxChunkVersion2PayloadSizeWithReferences(
                    treeDepthInfo.getNumberOfReferencesForSize(data.size))
            val subChunks = data.sliceArray(directoryDataSize..data.lastIndex)
                    .toChunksOfSize(treeDepthInfo.subSize)
                    .map { chopToChunks(it, chunks) }
            val result = DataChunkVersion2Builder(chunkSizeBytes).apply {
                references = subChunks.map { hasher.getHash(it.data) }
                payload = data.sliceArray(0 until directoryDataSize)
            }.chunkStructure.createChunk()
            chunks.add(result)
            return result
        }
    }

    private fun ByteArray.createPlainChunkStructure() =
            when {
                version0PayloadSize == size ->
                    DataChunkStructure.version0(this)
                maxVersion2PayloadSize < size -> {
                    val additionalBytes = chunkSizeBytes -
                            DataChunkStructure.Header.Version1.SIZE - size
                    DataChunkStructure.version1(this, additionalBytes)
                }
                else ->
                    DataChunkVersion2Builder(chunkSizeBytes)
                            .apply { payload = this@createPlainChunkStructure }.chunkStructure
            }

    private fun DataChunkStructure.createChunk() =
            createChunk(Key(Id(hasher.getHash(binary))))

    private val maxVersion2PayloadSize =
            chunkSizeBytes - DataChunkStructure.Header.Version2.MIN_SIZE

    private fun getTreeDepthInfoForSize(dataSize: Int) =
            TreeDepthInfo().getMinimumForSize(dataSize)

    private fun maxChunkVersion2PayloadSizeWithReferences(numberOfReferences: Int) =
            chunkSizeBytes -
                    DataChunkStructure.Header.Version2.MIN_SIZE -
                    numberOfReferences * referenceBlockSize

    val referenceBlockSize get() = DataChunkControlBlock.NON_CONTENT_SIZE +
            (hasher.hashBits - 1) / Byte.SIZE_BITS + 1

    private val version0PayloadSize =
            chunkSizeBytes - DataChunkStructure.Header.Version0.SIZE

    private inner class TreeDepthInfo private constructor(
            val depth: Int,
            val subSize: Int
    ) {
        constructor() : this(0, 0)

        fun getMinimumForSize(dataSize: Int): TreeDepthInfo =
                if (dataSize <= maxSize) this
                else TreeDepthInfo(depth + 1, maxSize).getMinimumForSize(dataSize)

        fun getNumberOfReferencesForSize(dataSize: Int) =
                (0..maxReferences).first { dataSize <= getSizeForReferences(it) }

        private val maxSize =
                if (depth <= 0) version0PayloadSize else getSizeForReferences(maxReferences)

        private fun getSizeForReferences(references: Int) =
                references * subSize + maxChunkVersion2PayloadSizeWithReferences(references)
    }
}