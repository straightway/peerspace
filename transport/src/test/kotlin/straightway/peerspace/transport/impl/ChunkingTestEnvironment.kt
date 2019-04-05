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
import straightway.peerspace.crypto.Encryptor
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.testing.flow.Less
import straightway.testing.flow.Not
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.than
import straightway.utils.toByteArray
import java.util.Base64

class ChunkingTestEnvironment(
        chunkSizeBytes: Int,
        maxReferences: Int,
        val data: ByteArray
) : ChunkEnvironmentValues(chunkSizeBytes, maxReferences) {

    constructor(chunkSizeBytes: Int,
                maxReferences: Int,
                dataGetter: ChunkEnvironmentValues.() -> ByteArray)
            : this(
                chunkSizeBytes,
                maxReferences,
                ChunkEnvironmentValues(chunkSizeBytes, maxReferences).dataGetter())

    companion object {
        private fun asBase64(data: ByteArray) = base64Encoder.encodeToString(data)
        private val base64Encoder = Base64.getEncoder()
        private fun firstRange(sz: Int) = 0 until sz
        private fun ByteArray.rest(sz: Int) = sz..lastIndex
    }

    val env = TransportTestEnvironment(
            chunkerFactory = { ChunkerImpl(chunkSizeBytes, maxReferences) },
            deChunkerFactory = { DeChunkerImpl() },
            cryptoFactory = { cryptoFactory })

    val setUpChunks: Set<DataChunkStructure> get() = _setUpChunks

    val notEncryptor = mock<Encryptor> {
        on { encrypt(any()) }.thenAnswer { it.getArgument<ByteArray>(0) }
    }

    fun DataChunkStructure.createChunk() =
            createChunk(Key(Id(getHash(binary))))

    fun ByteArray.reserveForDirectory(numberOfBytes: Int): ByteArray {
        directoryReservations.add(sliceArray(0 until numberOfBytes))
        return sliceArray(numberOfBytes..lastIndex)
    }

    fun ByteArray.createDirectoryDataChunkWithNumberOfReferences(numberOfReferences: Int) =
                createChunkVersion2 {
                    addReferences(numberOfReferences)
                    addDirectoryPayload()
                    addSetUpChunk(chunkStructure)
                    this@createDirectoryDataChunkWithNumberOfReferences
                }

    fun ByteArray.createPlainDataChunkVersion0(): ByteArray {
        expect(size is_ Not - Less than payloadBytesVersion0)
        val chunkStructure =
                DataChunkStructure.version0(sliceArray(firstRange(payloadBytesVersion0)))
        addSetUpChunk(chunkStructure)
        return sliceArray(rest(payloadBytesVersion0))
    }

    fun ByteArray.createPlainDataChunkVersion1(additionalBytes: Int): ByteArray {
        expect(size is_ Not - Less than minPayloadBytesVersion1)
        val numPayloadBytes = maxPayloadBytesVersion1 - additionalBytes
        val chunkStructure = DataChunkStructure.version1(
                sliceArray(firstRange(numPayloadBytes)), additionalBytes)
        addSetUpChunk(chunkStructure)
        return sliceArray(rest(numPayloadBytes))
    }

    fun ByteArray.createPlainDataChunkVersion2() =
            createChunkVersion2 {
                payload = sliceArray(0 until kotlin.math.min(size, availablePayloadBytes))
                addSetUpChunk(chunkStructure)
                sliceArray(payload.size..lastIndex)
            }

    fun ByteArray.end() = expect(isEmpty() && chunksToAddToDirectory.size == 1)

    fun addHash(data: ByteArray) = nextHashValue++.toByteArray().also {
        hashes[asBase64(data)] = it
    }

    fun setHash(data: ByteArray, hash: ByteArray) {
        hashes[asBase64(data)] = hash
    }

    var isCreatingHashOnTheFly = false

    fun getHash(data: ByteArray) =
            hashes[asBase64(data)] ?:
            if (isCreatingHashOnTheFly) addHash(data)
            else throw Panic("Hash not found for ${DataChunkStructure.fromBinary(data)}")

    private var hashes = mutableMapOf<String, ByteArray>()
    private var nextHashValue = 1
    private val _setUpChunks = mutableSetOf<DataChunkStructure>()
    private val chunksToAddToDirectory = mutableListOf<DataChunkStructure>()
    private val directoryReservations = mutableListOf<ByteArray>()

    private fun DataChunkVersion2Builder.addDirectoryPayload() {
        payload = directoryReservations.last()
        directoryReservations.removeAt(directoryReservations.lastIndex)
    }

    private fun DataChunkVersion2Builder.addReferences(numberOfReferences: Int) =
            (0 until numberOfReferences).forEach { _ -> addLastReference() }

    private fun DataChunkVersion2Builder.addLastReference() = with(chunksToAddToDirectory) {
        references = listOf(getHash(last().binary)) + references
        chunksToAddToDirectory.removeAt(lastIndex)
    }

    private fun createChunkVersion2(action: DataChunkVersion2Builder.() -> ByteArray): ByteArray =
            with(DataChunkVersion2Builder(chunkSizeBytes), action)

    private val hasher = mock<Hasher> {
        on { getHash(any()) }.thenAnswer { getHash(it.getArgument<ByteArray>(0)) }
        on { hashBits }.thenAnswer { HASH_BITS }
    }

    private val cryptoFactory = mock<CryptoFactory> {
        on { createHasher() }.thenAnswer { hasher }
    }

    private fun addSetUpChunk(chunk: DataChunkStructure) {
        addHash(chunk.binary)
        _setUpChunks += chunk
        chunksToAddToDirectory += chunk
    }

    private val minPayloadBytesVersion1 =
            maxPayloadBytesVersion1 - DataChunkStructure.Header.Version1.MAX_ADDITIONAL_BYTES
}