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

import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkControlBlock
import straightway.peerspace.data.DataChunkVersion2Builder
import straightway.peerspace.data.DataChunkStructure
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.Chunker
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.TransportComponent
import straightway.peerspace.transport.createHasher
import straightway.utils.toChunksOfSize

/**
 * Default implementation of the Chunker interface.
 */
class ChunkerImpl(
        private val chunkSizeBytes: Int,
        private val maxReferences: Int
) : Chunker, TransportComponent by TransportComponent() {

    private val hasher = createHasher()

    override fun chopToChunks(data: ByteArray, crypto: ChunkerCrypto) =
            mutableSetOf<DataChunk>().also { data.chopToChunks(it) }

    private fun ByteArray.chopToChunks(resultCollector: MutableSet<DataChunk>) =
            chopToChunkStructure(resultCollector).createChunk().also { resultCollector.add(it) }

    private fun ByteArray.chopToChunkStructure(resultCollector: MutableSet<DataChunk>) =
            if (size <= version0PayloadSize) createPlainChunkStructure()
            else createChunkTree(resultCollector)

    private fun ByteArray.createPlainChunkStructure() =
            when {
                version0PayloadSize == size -> createPlainVersion0Chunk()
                maxVersion2PayloadSize < size -> createPlainVersion1Chunk()
                else -> createPlainVersion2Chunk()
            }

    private fun ByteArray.createPlainVersion0Chunk() =
            DataChunkStructure.version0(this)

    private fun ByteArray.createPlainVersion1Chunk() =
            DataChunkStructure.version1(this, additionalVersion1PayloadBytes)

    private fun ByteArray.createPlainVersion2Chunk() =
            DataChunkVersion2Builder(chunkSizeBytes).also { it.payload = this }.chunkStructure

    private val ByteArray.additionalVersion1PayloadBytes get() =
        chunkSizeBytes - DataChunkStructure.Header.Version1.SIZE - size

    private fun ByteArray.createChunkTree(resultCollector: MutableSet<DataChunk>) =
            DataChunkVersion2Builder(chunkSizeBytes).apply {
                references = createSubChunks(resultCollector).hashes
                setPayloadPart(this@createChunkTree)
            }.chunkStructure

    private val Iterable<DataChunk>.hashes get() =
            map { hasher.getHash(it.data) }

    private fun ByteArray.createSubChunks(resultCollector: MutableSet<DataChunk>): List<DataChunk> =
            toSubTreeChunks().map { it.chopToChunks(resultCollector) }

    private fun ByteArray.toSubTreeChunks() =
            with(treeDepthInfo) {
                sliceArray(getTopLevelDirectoryPayloadSize(size)..lastIndex)
                        .toChunksOfSize(subSize)
            }

    private val ByteArray.treeDepthInfo get() = getTreeDepthInfoForSize(size)

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

    private val referenceBlockSize get() = DataChunkControlBlock.NON_CONTENT_SIZE +
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

        fun getTopLevelDirectoryPayloadSize(aggregatedPayloadSize: Int) =
                maxChunkVersion2PayloadSizeWithReferences(
                        getNumberOfReferencesForSize(aggregatedPayloadSize))

        private fun getNumberOfReferencesForSize(dataSize: Int) =
                (0..maxReferences).first { dataSize <= getSizeForReferences(it) }

        private val maxSize =
                if (depth <= 0) version0PayloadSize else getSizeForReferences(maxReferences)

        private fun getSizeForReferences(references: Int) =
                references * subSize + maxChunkVersion2PayloadSizeWithReferences(references)
    }
}