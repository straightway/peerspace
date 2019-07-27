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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import straightway.error.Panic
import straightway.expr.minus
import straightway.peerspace.crypto.CryptoFactory
import straightway.peerspace.crypto.Cryptor
import straightway.peerspace.crypto.DecryptorProperties
import straightway.peerspace.crypto.EncryptorProperties
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.tracer
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Less
import straightway.testing.flow.Not
import straightway.testing.flow.Size
import straightway.testing.flow.expect
import straightway.testing.flow.has
import straightway.testing.flow.is_
import straightway.testing.flow.of
import straightway.testing.flow.than
import straightway.testing.flow.to_
import straightway.utils.*
import java.util.Base64

class ChunkingTestEnvironment(
        chunkSizeBytes: Int,
        maxReferences: Int,
        cryptoBlockSize: Int,
        val data: ByteArray,
        tracerFactory: TransportTestEnvironment.() -> Tracer = { BufferTracer(RealTimeProvider()) }
) : ChunkEnvironmentValues(chunkSizeBytes, cryptoBlockSize) {

    constructor(chunkSizeBytes: Int,
                maxReferences: Int,
                cryptoBlockSize: Int,
                dataGetter: ChunkEnvironmentValues.() -> ByteArray)
            : this(
            chunkSizeBytes,
            maxReferences,
            cryptoBlockSize,
            ChunkEnvironmentValues(chunkSizeBytes, cryptoBlockSize).dataGetter())

    constructor(chunkSizeBytes: Int,
                maxReferences: Int,
                cryptoBlockSize: Int,
                dataGetter: ChunkEnvironmentValues.() -> ByteArray,
                tracerFactory: TransportTestEnvironment.() -> Tracer)
            : this(
            chunkSizeBytes,
            maxReferences,
            cryptoBlockSize,
            ChunkEnvironmentValues(chunkSizeBytes, cryptoBlockSize).dataGetter(),
            tracerFactory)

    companion object {

        val notEncryptorKey = byteArrayOf(1, 1, 2, 3, 5)
        val negatingEncryptorKey = byteArrayOf(-1, -1, -2, -3, -5)
        val newSymmetricEncryptorDefaultKey = byteArrayOf(2, 3, 5, 7, 11)

        private fun asBase64(data: ByteArray) = base64Encoder.encodeToString(data)
        private val base64Encoder = Base64.getEncoder()
        private fun firstRange(sz: Int) = 0 until sz
        private fun ByteArray.rest(sz: Int) = sz..lastIndex
    }

    val env = TransportTestEnvironment(
            chunkerFactory = { ChunkerImpl(chunkSizeBytes, maxReferences) },
            deChunkerFactory = { DeChunkerImpl() },
            cryptoFactory = { cryptoFactory },
            randomBytesFactory = {
                mock {
                    onGeneric { next() }.thenAnswer { randomBytes.next() }
                    on { hasNext() }.thenAnswer { randomBytes.hasNext() }
                }
            },
            tracerFactory = tracerFactory)

    val trace = env.context.tracer

    val setUpChunks: Set<DataChunkStructure> get() = _setUpChunks

    data class TestEncryptorProperties(
            override var maxClearTextBytes: Int = Int.MAX_VALUE,
            override var blockBytes: Int = 1,
            var outputBytesForInputSize: (Int) -> Int = { it }
    ) : EncryptorProperties {
        override fun getOutputBytes(inputSize: Int) =
                getFullBlockSize(outputBytesForInputSize(inputSize))

        fun getFullBlockSize(size: Int) =
                getNumberOfBlocksForSize(size) * blockBytes

        private fun getNumberOfBlocksForSize(size: Int) =
                ((size - 1) / blockBytes + 1)
    }

    val encryptorProperties = TestEncryptorProperties(blockBytes = cryptoBlockSize)

    data class TestDecryptorProperties(
            override var fixedCipherTextBytes: Int = 0
    ) : DecryptorProperties

    val decryptorProperties = TestDecryptorProperties()

    val notEncryptor = createCryptor(encryptionKey = notEncryptorKey)

    fun createCryptor(
            encryptionKey: ByteArray,
            decryptionKey: ByteArray = encryptionKey,
            encrypt: (ByteArray) -> ByteArray = { it },
            decrypt: (ByteArray) -> ByteArray = encrypt,
            encryptorProperties: EncryptorProperties? = null,
            decryptorProperties: DecryptorProperties? = null
    ) = mock<Cryptor> {
        on { this.encrypt(any()) }
                .thenAnswer { encrypt(it.getArgument<ByteArray>(0)).filledToBlockSize }
        on { this.decrypt(any()) }
                .thenAnswer { decrypt(it.getArgument<ByteArray>(0)).cutToBlockSize }
        on { this.encryptionKey }.thenAnswer { encryptionKey }
        on { this.decryptionKey }.thenAnswer { decryptionKey }
        on { this.encryptorProperties }.thenAnswer {
            encryptorProperties ?: this@ChunkingTestEnvironment.encryptorProperties
        }
        on { this.decryptorProperties }.thenAnswer {
            decryptorProperties ?: this@ChunkingTestEnvironment.decryptorProperties
        }
    }

    val negatingEncryptor =
            createCryptor(negatingEncryptorKey, encrypt = { it.negatedElements })

    var cryptor = notEncryptor

    var newSymmetricCryptor = createCryptor(
            newSymmetricEncryptorDefaultKey,
            encryptorProperties = TestEncryptorProperties(),
            decryptorProperties = TestDecryptorProperties(),
            encrypt = { ByteArray(it.size) { i -> (it[i] + 1).toByte() } },
            decrypt = { ByteArray(it.size) { i -> (it[i] - 1).toByte() } })

    fun DataChunkStructure.encrypted(theCryptor: Cryptor) = trace(this, theCryptor) {
        asBase64(binary).let {
            val cachedStructure = encryptedChunks[it] ?: encrypt(theCryptor)
            encryptedChunks[it] = cachedStructure
            cachedStructure
        }
    }

    fun DataChunkStructure.createChunk() = trace(this) {
        createChunk(Key(Id(getHash(binary))))
    }

    fun ByteArray.reserveForDirectory(numberOfBytes: Int): ByteArray =
            trace(this, numberOfBytes) {
                directoryReservations.add(sliceArray(0 until numberOfBytes))
                sliceArray(numberOfBytes..lastIndex)
            }

    fun ByteArray.createDirectoryDataChunkWithNumberOfReferences(numberOfReferences: Int) =
            trace(this, numberOfReferences) {
                createChunkVersion2 {
                    addReferences(numberOfReferences)
                    addDirectoryPayload()
                    addSetUpChunk(chunkStructure, cryptor)
                    this@createDirectoryDataChunkWithNumberOfReferences
                }
            }

    fun ByteArray.createPlainDataChunkVersion0(theCryptor: Cryptor = cryptor): ByteArray =
            trace(this, theCryptor) {
                expect(size is_ Not - Less than payloadBytesVersion0)
                val chunkStructure =
                        DataChunkVersion0(sliceArray(firstRange(payloadBytesVersion0)))
                addSetUpChunk(chunkStructure, theCryptor)
                sliceArray(rest(payloadBytesVersion0))
            }

    fun ByteArray.createPlainDataChunkVersion1(
            additionalBytes: Int,
            theCryptor: Cryptor = cryptor
    ): ByteArray = trace(this, additionalBytes, theCryptor) {
        expect(size is_ Not - Less than minPayloadBytesVersion1)
        val numPayloadBytes = maxPayloadBytesVersion1 - additionalBytes
        val chunkStructure = DataChunkVersion1(
                sliceArray(firstRange(numPayloadBytes)), additionalBytes)
        addSetUpChunk(chunkStructure, theCryptor)
        sliceArray(rest(numPayloadBytes))
    }

    fun ByteArray.createPlainDataChunkVersion2(theCryptor: Cryptor = cryptor) = trace(this, theCryptor) {
        createChunkVersion2 {
            payload = sliceArray(0 until kotlin.math.min(size, availablePayloadBytes))
            addSetUpChunk(chunkStructure, theCryptor)
            sliceArray(payload.size..lastIndex)
        }
    }

    fun ByteArray.createEncryptedPlainDataChunkVersion0(contentEncryptor: Cryptor): ByteArray =
            trace(this, contentEncryptor) {
                val contentEncryptorKey = contentEncryptor.encryptionKey
                val encryptedContentEncryptorKey = cryptor.encrypt(contentEncryptorKey)
                val encryptedChunkSize = chunkSizeBytes -
                        DataChunkVersion3.Header.MIN_SIZE -
                        contentEncryptorKey.size
                val encryptedPayloadSize = encryptedChunkSize - DataChunkVersion0.Header.SIZE
                val chunkToEncrypt = DataChunkVersion0(sliceArray(0 until encryptedPayloadSize))
                val encryptedChunk = contentEncryptor.encrypt(chunkToEncrypt.binary)
                addSetUpChunk(DataChunkVersion3(
                        encryptedContentEncryptorKey,
                        encryptedChunk.fillWithRandomBytes(encryptedChunkSize - encryptedChunk.size)),
                        notEncryptor)
                sliceArray(encryptedPayloadSize..lastIndex)
            }

    fun ByteArray.createEncryptedPlainDataChunkVersion1(
            contentEncryptor: Cryptor,
            additionalBytes: Int
    ): ByteArray = trace(this, contentEncryptor, additionalBytes) {
        val contentEncryptorKey = contentEncryptor.encryptionKey
        val encryptedContentEncryptorKey = cryptor.encrypt(contentEncryptorKey)
        val availableSpace = chunkSizeBytes -
                DataChunkVersion3.Header.MIN_SIZE -
                contentEncryptorKey.size
        val availablePayloadSize = availableSpace - DataChunkVersion1.Header.SIZE - additionalBytes
        val chunkToEncrypt =
                DataChunkVersion1(sliceArray(0 until availablePayloadSize), additionalBytes)
        val encryptedChunk = contentEncryptor.encrypt(chunkToEncrypt.binary)
        addSetUpChunk(DataChunkVersion3(
                encryptedContentEncryptorKey,
                encryptedChunk.fillWithRandomBytes(availableSpace - encryptedChunk.size)),
                notEncryptor)
        sliceArray(availablePayloadSize..lastIndex)
    }

    fun ByteArray.createEncryptedPlainDataChunkVersion2(contentEncryptor: Cryptor): ByteArray =
            trace(this, contentEncryptor) {
                val contentEncryptorKey = contentEncryptor.encryptionKey
                val encryptedContentEncryptorKey = cryptor.encrypt(contentEncryptorKey)
                val availableSpace =
                        chunkSizeBytes - DataChunkVersion3.Header.MIN_SIZE - contentEncryptorKey.size
                with(DataChunkVersion2Builder(availableSpace)) {
                    payload = sliceArray(0 until kotlin.math.min(size, availablePayloadBytes))
                    val encryptedChunk = contentEncryptor.encrypt(chunkStructure.binary)
                    addSetUpChunk(DataChunkVersion3(
                            encryptedContentEncryptorKey,
                            encryptedChunk.fillWithRandomBytes(availableSpace - encryptedChunk.size)),
                            notEncryptor)
                    sliceArray(payload.size..lastIndex)
                }
            }

    fun ByteArray.createEncryptedDirectoryDataChunkWithNumberOfReferences(
            contentEncryptor: Cryptor,
            numberOfReferences: Int
    ): ByteArray = trace(this, contentEncryptor, numberOfReferences) {
        val contentEncryptorKey = contentEncryptor.encryptionKey
        val encryptedContentEncryptorKey = cryptor.encrypt(contentEncryptorKey)
        val availableSpace =
                chunkSizeBytes - DataChunkVersion3.Header.MIN_SIZE - contentEncryptorKey.size
        with(DataChunkVersion2Builder(availableSpace)) {
            addReferences(numberOfReferences)
            addDirectoryPayload()
            trace(TraceLevel.Debug) { "Created $chunkStructure" }
            val encryptedChunk = contentEncryptor.encrypt(chunkStructure.binary)
            addSetUpChunk(DataChunkVersion3(
                    encryptedContentEncryptorKey,
                    encryptedChunk.fillWithRandomBytes(availableSpace - encryptedChunk.size)),
                    notEncryptor)
            this@createEncryptedDirectoryDataChunkWithNumberOfReferences
        }
    }

    fun ByteArray.end() = trace {
        expect(this@end is_ Empty)
        expect(chunksReferencesToAddToDirectory has Size of 1)
        resetRandomStream()
    }

    fun addHash(data: ByteArray): ByteArray = trace(data) {
        nextHashValue++.toByteArray().also { hashes[asBase64(data)] = it }
    }

    fun setHash(data: ByteArray, hash: ByteArray) = trace(data, hash) {
        hashes[asBase64(data)] = hash
    }

    var isCreatingHashOnTheFly = false

    fun ByteArray.fillWithRandomBytes(bytes: Int) = trace(this, bytes) {
        this@fillWithRandomBytes + ByteArray(bytes) { randomBytes.next() }
    }

    fun resetRandomStream() = trace() {
        randomBytes = randomStream.iterator()
    }

    //region Private

    private fun DataChunkStructure.encrypt(theCryptor: Cryptor) =
            theCryptor.encrypt(binary.filled).let {
                DataChunkVersion0(it + ByteArray(encryptedPayloadSizeBytes - it.size) {
                    randomBytes.next()
                })
            }

    private val randomStream = ByteArray(1000) { (-it).toByte() }

    private val ByteArray.filledToBlockSize get() =
            this + ByteArray(encryptorProperties.getFullBlockSize(size) - size)

    private val ByteArray.cutToBlockSize get() =
            this.sliceArray(0 until
                    (size / encryptorProperties.blockBytes) * encryptorProperties.blockBytes)

    private val ByteArray.negatedElements get() =
            ByteArray(size) { (this[it].toInt().inv()).toByte() }

    private fun getHash(data: ByteArray) =
            hashes[asBase64(data)] ?:
            if (isCreatingHashOnTheFly) addHash(data)
            else throw Panic("Hash not found for ${DataChunkStructure.fromBinary(data)}, " +
                "hashable chunks: ${hashes.keys.map {
                    DataChunkStructure.fromBinary(Base64.getDecoder().decode(it))
                }}")

    private val encryptedChunks = mutableMapOf<String, DataChunkStructure>()
    private var randomBytes = randomStream.iterator()
    private val ByteArray.filled get() = fillWithRandomBytes(unencryptedChunkSizeBytes - size)
    private var hashes = mutableMapOf<String, ByteArray>()
    private var nextHashValue = 1
    private val _setUpChunks = mutableSetOf<DataChunkStructure>()
    private val chunksReferencesToAddToDirectory = mutableListOf<ByteArray>()
    private val directoryReservations = mutableListOf<ByteArray>()

    private fun DataChunkVersion2Builder.addDirectoryPayload() {
        payload = directoryReservations.last()
        directoryReservations.removeAt(directoryReservations.lastIndex)
    }

    private fun DataChunkVersion2Builder.addReferences(numberOfReferences: Int) =
            (0 until numberOfReferences).forEach { _ -> addLastReference() }

    private fun DataChunkVersion2Builder.addLastReference() =
            with(chunksReferencesToAddToDirectory) {
                references = listOf(last()) + references
                chunksReferencesToAddToDirectory.removeAt(lastIndex)
            }

    private fun createChunkVersion2(action: DataChunkVersion2Builder.() -> ByteArray): ByteArray =
            with(DataChunkVersion2Builder(unencryptedChunkSizeBytes), action)

    private val hasher: Hasher = mock<Hasher> {
        on { getHash(any()) }.thenAnswer {
            val toHash = it.getArgument<ByteArray>(0)
            trace(toHash) { getHash(toHash) }
        }
        on { hashBits }.thenAnswer { HASH_BITS }
    }

    private val cryptoFactory = mock<CryptoFactory> {
        on { createHasher() }.thenAnswer { hasher }
        on { createSymmetricCryptor() }.thenAnswer { newSymmetricCryptor }
    }

    private fun addSetUpChunk(chunk: DataChunkStructure, theCryptor: Cryptor) {
        val encrypted = if (chunk.version == 3.toByte()) chunk else chunk.encrypted(theCryptor)
        expect(encrypted.binary.size is_ Equal to_ chunkSizeBytes)
        val hash = addHash(encrypted.binary)
        _setUpChunks += encrypted
        chunksReferencesToAddToDirectory += hash
    }

    private val minPayloadBytesVersion1 =
            maxPayloadBytesVersion1 - DataChunkVersion1.Header.MAX_ADDITIONAL_BYTES

    //endregion
}