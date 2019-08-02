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

import straightway.koinutils.withOwnContext
import straightway.peerspace.crypto.Encryptor
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.TransportComponent
import straightway.peerspace.transport.cryptoFactory
import straightway.peerspace.transport.randomBytes
import straightway.peerspace.transport.tracer
import straightway.utils.toChunksOfSize

/**
 * Create a chunk tree from a given data array.
 */
class ChunkTreeCreator private constructor(
        val crypto: ChunkerCrypto,
        private val hasher: Hasher,
        private val chunkProperties: ChunkProperties,
        private val resultCollector: MutableSet<DataChunk>
) : TransportComponent by TransportComponent() {

    constructor(data: ByteArray,
                crypto: ChunkerCrypto,
                hasher: Hasher,
                chunkProperties: ChunkProperties)
    : this(crypto, hasher, chunkProperties, mutableSetOf()) {
        data.chopToChunkStructure()
    }

    val chunks: Set<DataChunk> get() = resultCollector

    // region private

    private val trace = tracer

    private fun ByteArray.chopToChunkStructure(): DataChunk = trace(this) {
        when {
            crypto.encryptor.encryptorProperties.maxClearTextBytes < size ->
                createEncryptedChunkWithOwnKey()
            size <= chunkProperties.version0PayloadSizeInCryptoContainer ->
                createPlainChunkStructure()
            else ->
                createChunkTree()
        }.createChunk()
    }

    private fun ByteArray.createEncryptedChunkWithOwnKey() = trace(this) {
        val contentCryptor = cryptoFactory.createSymmetricCryptor()
        val encryptedContentCryptorKey = crypto.encryptor.getEncryptedKey(contentCryptor)
        val cryptoChopper = createSubInstance(
                crypto.withContentCryptor(contentCryptor),
                DataChunkVersion3.Header.MIN_SIZE + encryptedContentCryptorKey.size)
        with (cryptoChopper) {
            this@createEncryptedChunkWithOwnKey.createEncryptedChunkWithOwnKey(encryptedContentCryptorKey)
        }
    }

    private fun ByteArray.createEncryptedChunkWithOwnKey(encryptedContentCryptorKey: ByteArray) =
            trace(this, encryptedContentCryptorKey) {
                val result = this@ChunkTreeCreator.createEncryptedChunkWithOwnKey(this@createEncryptedChunkWithOwnKey)
                val encryptedResult = crypto.encryptor.encrypt(result.binary).filled
                DataChunkVersion3(encryptedContentCryptorKey, encryptedResult)
            }

    private fun createEncryptedChunkWithOwnKey(data: ByteArray) = trace(data) {
        when {
            chunkProperties.version0PayloadSizeInCryptoContainer < data.size -> data.createChunkTree()
            else -> data.createPlainChunkStructure()
        }
    }

    private fun Encryptor.getEncryptedKey(contentCryptor: Encryptor) = trace(contentCryptor) {
        encrypt(contentCryptor.encryptionKey)
    }

    private fun ByteArray.createPlainChunkStructure() = trace(this) {
        when {
            chunkProperties.version0PayloadSizeInCryptoContainer == size ->
                createPlainVersion0Chunk()
            chunkProperties.maxVersion2PayloadSize < size ->
                createPlainVersion1Chunk()
            else ->
                createPlainVersion2Chunk()
        }
    }

    private fun ByteArray.createPlainVersion0Chunk() = trace(this) {
        DataChunkVersion0(this@createPlainVersion0Chunk)
    }

    private fun ByteArray.createPlainVersion1Chunk() = trace(this) {
        DataChunkVersion1(this@createPlainVersion1Chunk, additionalVersion1PayloadBytes)
    }

    private fun ByteArray.createPlainVersion2Chunk() = trace(this) {
        DataChunkVersion2Builder(chunkProperties.chunkSizeBytesRespectingCryptoDataBlocks).also {
            it.payload = this@createPlainVersion2Chunk
        }.chunkStructure
    }

    private val ByteArray.additionalVersion1PayloadBytes get() = trace(this) {
        chunkProperties.chunkSizeBytesRespectingCryptoDataBlocks - DataChunkVersion1.Header.SIZE - size
    }

    private fun ByteArray.createChunkTree() = trace(this) {
        DataChunkVersion2Builder(chunkProperties.chunkSizeBytesRespectingCryptoDataBlocks).apply {
            references = recursiveSubChunks.hashes
            setPayloadPart(this@createChunkTree)
        }.chunkStructure
    }

    private val Iterable<DataChunk>.hashes get() = trace(this) {
        map { hasher.getHash(it.data) }
    }

    private val ByteArray.recursiveSubChunks get() = trace(this) {
        subTreeChunks.map {
            with (createSubInstance(crypto, DataChunkVersion0.Header.SIZE))
            {
                it.chopToChunkStructure()
            }
        }
    }

    private fun createSubInstance(crypto: ChunkerCrypto, cryptoContainerHeaderSizeBytes: Int) =
            trace (crypto, cryptoContainerHeaderSizeBytes) {
                withOwnContext {
                    ChunkTreeCreator(
                            crypto,
                            hasher,
                            chunkProperties.copy(cryptoContainerHeaderSizeBytes = cryptoContainerHeaderSizeBytes),
                            resultCollector)
                }
            }

    private val ByteArray.subTreeChunks get() = trace(this) {
        with(treeDepthInfo) {
            sliceArray(getDirectoryPayloadSize(size)..lastIndex).toChunksOfSize(maxSubTreeSize)
        }
    }

    private val ByteArray.treeDepthInfo
        get() = trace(this) { getTreeDepthInfoForSize(size) }

    private fun DataChunkStructure.createChunk(): DataChunk = trace(this) {
        if (version == 3.toByte())
            createChunk(Key(Id(hasher.getHash(binary))))
        else {
            val crytoContainerChunk = encryptedChunkForCryptoContainer.inCryptoContainerWithoutOwnKey
            crytoContainerChunk.createChunk(Key(Id(hasher.getHash(crytoContainerChunk.binary))))
        }.also {
            resultCollector.add(it)
        }
    }

    private val ByteArray.inCryptoContainerWithoutOwnKey get() = trace(this) {
        DataChunkVersion0(filledForCryptoContainer)
    }

    private val ByteArray.filledForCryptoContainer get() =
            this + ByteArray(cryptoContainerFillBytes) { randomBytes.next() }

    private val cryptoContainerFillBytes get() = with (chunkProperties) {
        cryptoContainerPayloadSizeBytes - chunkSizeBytesRespectingCryptoDataBlocks
    }

    private val DataChunkStructure.encryptedChunkForCryptoContainer get() =
            crypto.encryptor.encrypt(binary.filled)

    private val ByteArray.filled get() =
            this + ByteArray(chunkProperties.chunkSizeBytesRespectingCryptoDataBlocks - size) { randomBytes.next() }

    private fun getTreeDepthInfoForSize(dataSize: Int) = trace (this, dataSize) {
        ChunkTreeDepthInfo(chunkProperties).getMinimumForSize(dataSize)
    }

    // endregion
}