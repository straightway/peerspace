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
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.Chunker
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.TransportComponent
import straightway.peerspace.transport.createHasher
import straightway.peerspace.transport.randomBytes
import straightway.utils.toChunksOfSize

/**
 * Default implementation of the Chunker interface.
 */
class ChunkerImpl(
        chunkSizeBytes: Int,
        private val maxReferences: Int
) : Chunker, TransportComponent by TransportComponent() {

    override fun chopToChunks(data: ByteArray, crypto: ChunkerCrypto) =
            mutableSetOf<DataChunk>().also { data.chopToChunks(crypto, it) }

    // region Private

    private val unencryptedChunkSizeBytes =
            chunkSizeBytes - DataChunkStructure.Header.Version0.SIZE

    private fun ByteArray.chopToChunks(
            crypto: ChunkerCrypto,
            resultCollector: MutableSet<DataChunk>
    ) =
            chopToChunkStructure(crypto, resultCollector).createChunk(crypto)
                    .also { resultCollector.add(it) }

    private fun ByteArray.chopToChunkStructure(
            crypto: ChunkerCrypto, resultCollector: MutableSet<DataChunk>
    ) =
            if (size <= version0PayloadSize) createPlainChunkStructure()
            else createChunkTree(crypto, resultCollector)

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
            DataChunkVersion2Builder(unencryptedChunkSizeBytes)
                    .also { it.payload = this }.chunkStructure

    private val ByteArray.additionalVersion1PayloadBytes get() =
        unencryptedChunkSizeBytes - DataChunkStructure.Header.Version1.SIZE - size

    private fun ByteArray.createChunkTree(
            crypto: ChunkerCrypto, resultCollector: MutableSet<DataChunk>
    ) =
            DataChunkVersion2Builder(unencryptedChunkSizeBytes).apply {
                references = createSubChunks(crypto, resultCollector).hashes
                setPayloadPart(this@createChunkTree)
            }.chunkStructure

    private val Iterable<DataChunk>.hashes get() =
            map { hasher.getHash(it.data) }

    private fun ByteArray.createSubChunks(
            crypto: ChunkerCrypto, resultCollector: MutableSet<DataChunk>
    ): List<DataChunk> =
            toSubTreeChunks().map { it.chopToChunks(crypto, resultCollector) }

    private fun ByteArray.toSubTreeChunks() =
            with(treeDepthInfo) {
                sliceArray(getDirectoryPayloadSize(size)..lastIndex).toChunksOfSize(maxSubTreeSize)
            }

    private val ByteArray.treeDepthInfo get() = getTreeDepthInfoForSize(size)

    private fun DataChunkStructure.createChunk(crypto: ChunkerCrypto): DataChunk {
        val encryptedChunk = encryptedChunk(crypto)
        val crytoContainerChunk = DataChunkStructure.version0(encryptedChunk)
        return crytoContainerChunk.createChunk(Key(Id(hasher.getHash(crytoContainerChunk.binary))))
    }

    @Suppress("UNUSED_PARAMETER")
    private fun DataChunkStructure.encryptedChunk(crypto: ChunkerCrypto) =
            crypto.encryptor.encrypt(binary.filled)

    private val ByteArray.filled get() =
            (this + ByteArray(unencryptedChunkSizeBytes - size) { randomBytes.next() })

    private val maxVersion2PayloadSize =
            unencryptedChunkSizeBytes - DataChunkStructure.Header.Version2.MIN_SIZE

    private fun getTreeDepthInfoForSize(dataSize: Int) =
            TreeDepthInfo().getMinimumForSize(dataSize)

    private fun maxChunkVersion2PayloadSizeWithReferences(numberOfReferences: Int) =
            unencryptedChunkSizeBytes -
                    DataChunkStructure.Header.Version2.MIN_SIZE -
                    numberOfReferences * referenceBlockSize

    private val referenceBlockSize get() = DataChunkControlBlock.NON_CONTENT_SIZE +
            (hasher.hashBits - 1) / Byte.SIZE_BITS + 1

    private val version0PayloadSize =
            unencryptedChunkSizeBytes - DataChunkStructure.Header.Version0.SIZE

    private val hasher = createHasher()

    private inner class TreeDepthInfo private constructor(
            val depth: Int,
            val maxSubTreeSize: Int
    ) {
        constructor() : this(0, 0)

        fun getMinimumForSize(dataSize: Int): TreeDepthInfo =
                if (dataSize <= maxSize) this
                else TreeDepthInfo(depth + 1, maxSize).getMinimumForSize(dataSize)

        fun getDirectoryPayloadSize(aggregatedPayloadSize: Int) =
                maxChunkVersion2PayloadSizeWithReferences(
                        getNumberOfReferencesForSize(aggregatedPayloadSize))

        private fun getNumberOfReferencesForSize(dataSize: Int) =
                (0..maxReferences).first { dataSize <= getSizeForReferences(it) }

        private val maxSize =
                if (depth <= 0) version0PayloadSize else getSizeForReferences(maxReferences)

        private fun getSizeForReferences(references: Int) =
                references * maxSubTreeSize + maxChunkVersion2PayloadSizeWithReferences(references)
    }

    // endregion
}