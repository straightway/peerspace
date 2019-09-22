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
import straightway.peerspace.crypto.hashBytes
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.ChunkProperties
import straightway.peerspace.transport.ChunkTreeCreator
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.TransportComponent
import straightway.peerspace.transport.cryptoFactory
import straightway.peerspace.transport.getTreeInfo
import straightway.peerspace.transport.randomBytes
import straightway.peerspace.transport.trace
import straightway.utils.TraceLevel
import straightway.utils.toChunksOfSize

/**
 * Create a chunk tree from a given data array.
 */
@Suppress("LargeClass")
class ChunkTreeCreatorImpl private constructor(
        val crypto: ChunkerCrypto,
        chunkPropertiesParameter: ChunkProperties,
        private val resultCollector: MutableSet<DataChunk>
) : ChunkTreeCreator, TransportComponent by TransportComponent() {

    private val hasher = cryptoFactory.createHasher()

    private val chunkProperties = chunkPropertiesParameter
            .copy(referenceBlockSizeBytes = hasher.referenceBlockSize)

    constructor(data: ByteArray,
                crypto: ChunkerCrypto,
                chunkProperties: ChunkProperties)
    : this(crypto, chunkProperties, mutableSetOf()) {
        data.chopToChunkStructure()
    }

    override val chunks: Set<DataChunk> get() = resultCollector

    // region private

    private fun ByteArray.chopToChunkStructure(): DataChunk = trace(this) {
        when {
            crypto.encryptor.encryptorProperties.maxClearTextBytes < size ->
                createEncryptedChunkWithOwnKey()
            size <= chunkProperties.version0PayloadSizeInCryptoContainerBytes ->
                createPlainChunkStructure()
            else ->
                createChunkTree()
        }.createChunk()
    }

    private fun ByteArray.createEncryptedChunkWithOwnKey(): DataChunkStructure = trace(this) {
        val contentCryptor = cryptoFactory.createSymmetricCryptor()
        val encryptedContentCryptorKey = crypto.encryptor.getEncryptedKey(contentCryptor)
        val cryptoChopper = createSubInstance(
                crypto.withContentCryptor(contentCryptor),
                DataChunkVersion3.Header.MIN_SIZE + encryptedContentCryptorKey.size)
        with (cryptoChopper) {
            createEncryptedChunkWithOwnKey(encryptedContentCryptorKey)
        }
    }

    private fun ByteArray.createEncryptedChunkWithOwnKey(
            encryptedContentCryptorKey: ByteArray
    ): DataChunkStructure =
            trace(this, encryptedContentCryptorKey) {
                val result = createEncryptedChunkPayloadWithOwnKey(this)
                val encryptedResult = crypto.encryptor.encrypt(result.binary).filled
                DataChunkVersion3(encryptedContentCryptorKey, encryptedResult)
            }

    private fun createEncryptedChunkPayloadWithOwnKey(data: ByteArray): DataChunkStructure =
            trace(data) {
                when {
                    chunkProperties.version0PayloadSizeInCryptoContainerBytes < data.size ->
                        data.createChunkTree()
                    else ->
                        data.createPlainChunkStructure()
                }
            }

    private fun Encryptor.getEncryptedKey(contentCryptor: Encryptor): ByteArray =
            trace(contentCryptor) {
                encrypt(contentCryptor.encryptionKey)
            }

    private fun ByteArray.createPlainChunkStructure(): DataChunkStructure = trace(this) {
        when {
            chunkProperties.version0PayloadSizeInCryptoContainerBytes == size ->
                createPlainVersion0Chunk()
            chunkProperties.maxVersion2PayloadSizeInCryptoContainerBytes < size ->
                createPlainVersion1Chunk()
            else ->
                createPlainVersion2Chunk()
        }
    }

    private fun ByteArray.createPlainVersion0Chunk(): DataChunkVersion0 = trace(this) {
        DataChunkVersion0(this)
    }

    private fun ByteArray.createPlainVersion1Chunk(): DataChunkVersion1 = trace(this) {
        DataChunkVersion1(this, additionalVersion1PayloadBytes)
    }

    private fun ByteArray.createPlainVersion2Chunk(): DataChunkVersion2 = trace(this) {
        DataChunkVersion2Builder(
                chunkProperties.cryptoContainerPayloadSizeRespectingBlockSizeBytes
        ).also {
            it.payload = this
        }.chunkStructure
    }

    private val ByteArray.additionalVersion1PayloadBytes: Int get() = trace(this) {
        chunkProperties.cryptoContainerPayloadSizeRespectingBlockSizeBytes -
                DataChunkVersion1.Header.SIZE -
                size
    }

    private fun ByteArray.createChunkTree(): DataChunkVersion2 = trace(this) {
        DataChunkVersion2Builder(
                chunkProperties.cryptoContainerPayloadSizeRespectingBlockSizeBytes
        ).apply {
            references = recursiveSubChunks.hashes
            setPayloadPart(this@createChunkTree)
        }.chunkStructure
    }

    private val Iterable<DataChunk>.hashes: List<ByteArray> get() = trace(this) {
        map { hasher.getHash(it.data) }
    }

    private val ByteArray.recursiveSubChunks: List<DataChunk> get() = trace(this) {
        subTreeChunks.map {
            with (createSubInstance(crypto, DataChunkVersion0.Header.SIZE))
            {
                it.chopToChunkStructure()
            }
        }
    }

    private fun createSubInstance(
            crypto: ChunkerCrypto, cryptoContainerHeaderSizeBytes: Int
    ): ChunkTreeCreatorImpl =
            trace(crypto, cryptoContainerHeaderSizeBytes) {
                withOwnContext {
                    ChunkTreeCreatorImpl(
                            crypto,
                            chunkProperties.copy(
                                cryptoContainerHeaderSizeBytes = cryptoContainerHeaderSizeBytes),
                            resultCollector)
                }
            }

    private val ByteArray.subTreeChunks: List<ByteArray> get() = trace(this) {
        with(getTreeInfo(chunkProperties, size)) {
            sliceArray(directoryPayloadSizeBytes..lastIndex).toChunksOfSize(maxSubTreeSizeBytes)
        }
    }

    private fun DataChunkStructure.createChunk(): DataChunk = trace(this) {
        if (version == DataChunkVersion3.VERSION)
            createChunk(Key(Id(hasher.getHash(binary))))
        else {
            val crytoContainerChunk =
                    encryptedChunkForCryptoContainer.inCryptoContainerWithoutOwnKey
            crytoContainerChunk.createChunk(Key(Id(hasher.getHash(crytoContainerChunk.binary))))
        }
        .also {
            resultCollector.add(it)
        }
    }

    private val ByteArray.inCryptoContainerWithoutOwnKey: DataChunkVersion0 get() = trace(this) {
        DataChunkVersion0(filledForCryptoContainer)
    }

    private val ByteArray.filledForCryptoContainer: ByteArray get() = trace(this) {
        this + ByteArray(cryptoContainerFillBytes) { randomBytes.next() }
    }

    private val cryptoContainerFillBytes: Int get() = trace(this) {
        with(chunkProperties) {
            cryptoContainerPayloadSizeBytes - cryptoContainerPayloadSizeRespectingBlockSizeBytes
        }
    }

    private val DataChunkStructure.encryptedChunkForCryptoContainer: ByteArray get() = trace {
        crypto.encryptor.encrypt(binary.filled)
    }

    private val ByteArray.filled: ByteArray get() = trace(this) {
        this + ByteArray(
                chunkProperties.cryptoContainerPayloadSizeRespectingBlockSizeBytes - size
        ) { randomBytes.next() }
    }

    private val Hasher.referenceBlockSize get() = trace(this) {
        DataChunkControlBlock.NON_CONTENT_SIZE + hashBytes
    }

    init {
        trace(crypto, chunkPropertiesParameter, resultCollector) {
            trace.traceMessage(TraceLevel.Debug) { "${crypto.encryptor.encryptorProperties}" }
        }
    }

    // endregion
}