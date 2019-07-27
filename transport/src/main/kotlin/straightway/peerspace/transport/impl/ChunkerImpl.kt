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
import straightway.peerspace.transport.*
import straightway.utils.TraceLevel
import straightway.utils.toChunksOfSize

/**
 * Default implementation of the Chunker interface.
 */
class ChunkerImpl(
        private val chunkSizeBytes: Int,
        private val maxReferences: Int
) : Chunker, TransportComponent by TransportComponent() {

    override fun chopToChunks(data: ByteArray, crypto: ChunkerCrypto) = trace(data, crypto) {
        Chopper(data, crypto).chunks
    }

    // region Private

    private val trace = tracer

    private inner class Chopper(
            data: ByteArray,
            var crypto: ChunkerCrypto
    ) {
        val chunks: Set<DataChunk> get() = resultCollector

        val rootChunk: DataChunk

        // region private

        private fun ByteArray.chopToChunkStructure(): DataChunkStructure = trace(this) {
            if (crypto.encryptor.encryptorProperties.maxClearTextBytes < size)
                createEncryptedChunkWithOwnKey()
            else if (size <= getVersion0PayloadSizeInCryptoContainer(chunkSizeBytes, DataChunkVersion0.Header.SIZE))
                createPlainChunkStructure(chunkSizeBytes, DataChunkVersion0.Header.SIZE)
            else createChunkTree(chunkSizeBytes, DataChunkVersion0.Header.SIZE)
        }

        private fun ByteArray.createEncryptedChunkWithOwnKey() = trace(this) {
            val contentCryptor = cryptoFactory.createSymmetricCryptor()
            val contentCryptorKey = contentCryptor.encryptionKey
            val encryptedContentCryptorKey = crypto.encryptor.encrypt(contentCryptorKey)
            crypto = crypto.withContentCryptor(contentCryptor)
            val cryptoContainerHeaderSizeBytes = DataChunkVersion3.Header.MIN_SIZE + encryptedContentCryptorKey.size

            val result = if (getVersion0PayloadSizeInCryptoContainer(chunkSizeBytes, cryptoContainerHeaderSizeBytes) < size) {
                createChunkTree(chunkSizeBytes, cryptoContainerHeaderSizeBytes)
            } else {
                createPlainChunkStructure(chunkSizeBytes, cryptoContainerHeaderSizeBytes)
            }

            val encryptedResult = contentCryptor.encrypt(result.binary).filled(chunkSizeBytes, cryptoContainerHeaderSizeBytes)

            DataChunkVersion3(encryptedContentCryptorKey, encryptedResult)
        }

        private fun ByteArray.createPlainChunkStructure(chunkSizeBytes: Int, cryptoContainerHeaderSize: Int) = trace(this, chunkSizeBytes) {
            when {
                getVersion0PayloadSizeInCryptoContainer(chunkSizeBytes, cryptoContainerHeaderSize) == size ->
                    createPlainVersion0Chunk()
                maxVersion2PayloadSize(chunkSizeBytes, cryptoContainerHeaderSize) < size ->
                    createPlainVersion1Chunk(chunkSizeBytes, cryptoContainerHeaderSize)
                else ->
                    createPlainVersion2Chunk(chunkSizeBytes, cryptoContainerHeaderSize)
            }
        }

        private fun ByteArray.createPlainVersion0Chunk() = trace(this) {
            DataChunkVersion0(this@createPlainVersion0Chunk)
        }

        private fun ByteArray.createPlainVersion1Chunk(
                chunkSizeBytes: Int,
                cryptoContainerHeaderSizeBytes: Int
        ) = trace(this, chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            DataChunkVersion1(
                    this@createPlainVersion1Chunk,
                    additionalVersion1PayloadBytes(chunkSizeBytes, cryptoContainerHeaderSizeBytes))
        }

        private fun ByteArray.createPlainVersion2Chunk(
                chunkSizeBytes: Int,
                cryptoContainerHeaderSizeBytes: Int
        ) = trace(this, chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            DataChunkVersion2Builder(
                    getChunkSizeBytesRespectingCryptoDataBlocks(chunkSizeBytes, cryptoContainerHeaderSizeBytes)
            ).also { it.payload = this@createPlainVersion2Chunk }.chunkStructure
        }

        private fun ByteArray.additionalVersion1PayloadBytes(
                chunkSizeBytes: Int,
                cryptoContainerHeaderSizeBytes: Int
        ) = trace(this, chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            getChunkSizeBytesRespectingCryptoDataBlocks(chunkSizeBytes, cryptoContainerHeaderSizeBytes) - DataChunkVersion1.Header.SIZE - size
        }

        private fun ByteArray.createChunkTree(
                chunkSizeBytes: Int,
                cryptoContainerHeaderSizeBytes: Int
        ) = trace(this, chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            DataChunkVersion2Builder(getChunkSizeBytesRespectingCryptoDataBlocks(chunkSizeBytes, cryptoContainerHeaderSizeBytes)).apply {
                trace.trace(TraceLevel.Debug) {"Creating chunk v2 of size $chunkSize" }
                references = createSubChunks(chunkSizeBytes, cryptoContainerHeaderSizeBytes).hashes
                setPayloadPart(this@createChunkTree)
            }.chunkStructure
        }

        private val Iterable<DataChunk>.hashes get() = trace(this) {
            map { hasher.getHash(it.data) }
        }

        private fun ByteArray.createSubChunks(
                chunkSizeBytes: Int,
                cryptoContainerHeaderSizeBytes: Int
        ): List<DataChunk> = trace(this, chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            toSubTreeChunks(chunkSizeBytes, cryptoContainerHeaderSizeBytes).map {
                val chopper = Chopper(it, crypto)
                resultCollector.addAll(chopper.chunks)
                chopper.rootChunk
            }
        }

        private fun ByteArray.toSubTreeChunks(
                chunkSizeBytes: Int,
                cryptoContainerHeaderSizeBytes: Int
        ) = trace(this, chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            with(treeDepthInfo(chunkSizeBytes, cryptoContainerHeaderSizeBytes)) {
                sliceArray(getDirectoryPayloadSize(size)..lastIndex)
                        .toChunksOfSize(maxSubTreeSize)
            }
        }

        private fun ByteArray.treeDepthInfo(
                chunkSizeBytes: Int,
                cryptoContainerHeaderSizeBytes: Int
        ) = trace(this, chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            getTreeDepthInfoForSize(size, chunkSizeBytes, cryptoContainerHeaderSizeBytes)
        }

        private fun DataChunkStructure.createChunk(chunkSizeBytes: Int): DataChunk = trace(this, chunkSizeBytes) {
            if (version == 3.toByte()) createChunk(Key(Id(hasher.getHash(binary))))
            else {
                val encryptedChunk = encryptedChunk(crypto, chunkSizeBytes, DataChunkVersion0.Header.SIZE)
                val crytoContainerChunk = encryptedChunk.inCryptoContainerWithoutOwnKey(chunkSizeBytes)
                crytoContainerChunk.createChunk(Key(Id(hasher.getHash(crytoContainerChunk.binary))))
            }
        }

        private fun ByteArray.inCryptoContainerWithoutOwnKey(
                chunkSizeBytes: Int
        ) = trace(this, chunkSizeBytes) {
            DataChunkVersion0(filledForCryptoContainer(chunkSizeBytes, DataChunkVersion0.Header.SIZE))
        }

        private fun ByteArray.filledForCryptoContainer(chunkSizeBytes: Int, cryptoContainerHeaderSize: Int) = trace(this, chunkSizeBytes) {
            this@filledForCryptoContainer + ByteArray(getCryptoContainerPayloadSizeBytes(chunkSizeBytes, cryptoContainerHeaderSize) -
                    getChunkSizeBytesRespectingCryptoDataBlocks(chunkSizeBytes, cryptoContainerHeaderSize)
            ) {
                randomBytes.next()
            }
        }

        private fun DataChunkStructure.encryptedChunk(
                crypto: ChunkerCrypto,
                chunkSizeBytes: Int,
                cryptoContainerHeaderSizeBytes: Int
        ) = trace(this, crypto, chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            crypto.encryptor.encrypt(binary.filled(chunkSizeBytes, cryptoContainerHeaderSizeBytes))
        }

        private fun ByteArray.filled(
                chunkSizeBytes: Int, cryptoContainerHeaderSizeBytes: Int
        ) = trace(chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            (this@filled + ByteArray(getChunkSizeBytesRespectingCryptoDataBlocks(chunkSizeBytes, cryptoContainerHeaderSizeBytes) - size) {
                randomBytes.next()
            })
        }

        private fun getTreeDepthInfoForSize(
                dataSize: Int,
                chunkSizeBytes: Int,
                cryptoContainerHeaderSizeBytes: Int
        ) = trace (this, dataSize, chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            TreeDepthInfo(chunkSizeBytes, cryptoContainerHeaderSizeBytes).getMinimumForSize(dataSize)
        }

        private fun getMaxChunkVersion2PayloadSizeWithReferences(
                numberOfReferences: Int,
                chunkSizeBytes: Int,
                cryptorContainerHeaderSizeBytes: Int
        ) = trace(this, numberOfReferences, chunkSizeBytes, cryptorContainerHeaderSizeBytes) {
            getChunkSizeBytesRespectingCryptoDataBlocks(chunkSizeBytes, cryptorContainerHeaderSizeBytes) -
                    DataChunkVersion2.Header.MIN_SIZE -
                    numberOfReferences * referenceBlockSize
        }

        private val referenceBlockSize get() = trace {
            DataChunkControlBlock.NON_CONTENT_SIZE +
                    (hasher.hashBits - 1) / Byte.SIZE_BITS + 1
        }

        private val hasher = createHasher()

        private fun getCryptoContainerPayloadSizeBytes(
                chunkSizeBytes: Int,
                cryptoContainerHeaderSize: Int
        ) = trace(chunkSizeBytes, cryptoContainerHeaderSize) {
            chunkSizeBytes - cryptoContainerHeaderSize
        }

        private val blockSizeBytes = crypto.encryptor.encryptorProperties.blockBytes

        private fun getChunkSizeBytesRespectingCryptoDataBlocks(
                chunkSizeBytes: Int,
                cryptoContainerHeaderSizeBytes: Int
        ) = trace(chunkSizeBytes, cryptoContainerHeaderSizeBytes) {
            getBlockedSizeBytesFor(getCryptoContainerPayloadSizeBytes(chunkSizeBytes, cryptoContainerHeaderSizeBytes))
        }

        private fun getBlockedSizeBytesFor(chunkSizeBytes: Int) = trace(chunkSizeBytes) {
            (chunkSizeBytes / blockSizeBytes) * blockSizeBytes
        }

        private fun getVersion0PayloadSizeInCryptoContainer(
                chunkSizeBytes: Int,
                cryproContainerHeaderSizeBytes: Int
        ) = trace(chunkSizeBytes, cryproContainerHeaderSizeBytes) {
            getChunkSizeBytesRespectingCryptoDataBlocks(chunkSizeBytes, cryproContainerHeaderSizeBytes) - DataChunkVersion0.Header.SIZE
        }

        private fun maxVersion2PayloadSize(
                chunkSizeBytes: Int,
                cryproContainerHeaderSizeBytes: Int
        ) = trace(chunkSizeBytes, cryproContainerHeaderSizeBytes) {
            getChunkSizeBytesRespectingCryptoDataBlocks(chunkSizeBytes, cryproContainerHeaderSizeBytes) - DataChunkVersion2.Header.MIN_SIZE
        }

        private val resultCollector: MutableSet<DataChunk> = mutableSetOf()

        init {
            rootChunk = trace(data, crypto) {
                data.chopToChunkStructure().createChunk(chunkSizeBytes).also {
                    resultCollector.add(it)
                }
            }
        }

        private inner class TreeDepthInfo private constructor(
                val depth: Int,
                val maxSubTreeSize: Int,
                val chunkSizeBytes: Int,
                val rootCryptoContainerHeaderSizeBytes: Int
        ) {
            constructor(chunkSizeBytes: Int, rootCryptoContainerHeaderSizeBytes: Int)
                    : this(0, 0, chunkSizeBytes, rootCryptoContainerHeaderSizeBytes)

            fun getMinimumForSize(dataSize: Int): TreeDepthInfo = trace(this, dataSize) {
                if (dataSize <= getMaxSize(rootCryptoContainerHeaderSizeBytes)) this@TreeDepthInfo
                else TreeDepthInfo(depth + 1, getMaxSize(DataChunkVersion0.Header.SIZE), chunkSizeBytes, rootCryptoContainerHeaderSizeBytes)
                        .getMinimumForSize(dataSize)
            }

            fun getDirectoryPayloadSize(aggregatedPayloadSize: Int) = trace(this, aggregatedPayloadSize) {
                getMaxChunkVersion2PayloadSizeWithReferences(
                        getNumberOfReferencesForSize(aggregatedPayloadSize),
                        chunkSizeBytes,
                        rootCryptoContainerHeaderSizeBytes)
            }

            private fun getNumberOfReferencesForSize(dataSize: Int) = trace(this, dataSize) {
                (0..maxReferences).first { dataSize <= getSizeForReferences(it, rootCryptoContainerHeaderSizeBytes) }
            }

            private fun getMaxSize(cryptoContainerHeaderSizeBytes: Int) = trace(cryptoContainerHeaderSizeBytes) {
                if (depth <= 0) getVersion0PayloadSizeInCryptoContainer(chunkSizeBytes, cryptoContainerHeaderSizeBytes)
                else getSizeForReferences(maxReferences, cryptoContainerHeaderSizeBytes)
            }

            private fun getSizeForReferences(
                    references: Int,
                    cryproContainerHeaderSizeBytes: Int
            ) = trace(references, cryproContainerHeaderSizeBytes) {
                references * maxSubTreeSize +
                        getMaxChunkVersion2PayloadSizeWithReferences(references, chunkSizeBytes, cryproContainerHeaderSizeBytes)
            }

            init {
                trace(depth, maxSubTreeSize, chunkSizeBytes, rootCryptoContainerHeaderSizeBytes) { this }
            }
        }

        // endregion
    }

    // endregion
}