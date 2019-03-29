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
import org.junit.jupiter.api.Test
import straightway.error.Panic
import straightway.expr.minus
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.crypto.CryptoFactory
import straightway.peerspace.crypto.Encryptor
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkControlBlock
import straightway.peerspace.data.DataChunkVersion2Builder
import straightway.peerspace.data.DataChunkStructure
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.chunker
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Less
import straightway.testing.flow.Not
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.than
import straightway.testing.flow.to_
import straightway.utils.toByteArray
import java.util.Base64
import kotlin.math.min

class ChunkerImplTest : KoinLoggingDisabler() {

    private companion object {
        const val chunkSizeBytes = 0x20
        const val maxReferences = 2
        const val version0PayloadSize = chunkSizeBytes - DataChunkStructure.Header.Version0.SIZE
    }

    private val test get() =
        Given { TestEnvironment(chunkSizeBytes, maxReferences) }

    @Test
    fun `small plain data results in single chunk version 1`() {
        test while_ {
            data = byteArrayOf(1, 2, 3)
            data.createPlainDataChunkVersion2()
        } when_ {
            sut.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
        } then {
            expect(DataChunkStructure.fromBinary(it.result.single().data) is_ Equal
                    to_ expectedChunks.single())
        }
    }

    @Test
    fun `plain data of exact version 0 payload size produces version 0 chunk`() {
        test while_ {
            data = ByteArray(version0PayloadSize) { it.toByte() }
            expect(data.createPlainDataChunkVersion0() is_ Empty)
        } when_ {
            sut.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
        } then {
            expect(DataChunkStructure.fromBinary(it.result.single().data) is_ Equal
                    to_ expectedChunks.single())
        }
    }

    @Test
    fun `plain data between version 0 and version 2 payload size produces version 1 chunk`() {
        test while_ {
            data = ByteArray(version0PayloadSize - 2) { it.toByte() }
            expect(data.createPlainDataChunkVersion1(1) is_ Empty)
        } when_ {
            sut.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
        } then {
            expect(DataChunkStructure.fromBinary(it.result.single().data) is_ Equal
                    to_ expectedChunks.single())
        }
    }

    @Test
    fun `plain data chunk id is equal to hash of chunk's binary data`() =
            test while_ {
                data = byteArrayOf(1, 2, 3)
                expect(data.createPlainDataChunkVersion2() is_ Empty)
            } when_ {
                sut.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                expect(it.result.single().key is_ Equal to_ expectedChunks.single().hashKey)
            }

    @Test
    fun `plain data larger than chunk size results in two chunks`() =
            test while_ {
                data = ByteArray(version0PayloadSize + 1) { it.toByte() }
                expect(data
                        .reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(1))
                        .createPlainDataChunkVersion2()
                        .createDirectoryDataChunkWithNumberOfReferences(1) is_ Empty)
            } when_ {
                sut.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                assertExpectedChunks(it.result)
            }

    @Test
    fun `plain data larger than twice the chunk size results in three chunks`() =
            test while_ {
                data = ByteArray(2 * version0PayloadSize + 1) { it.toByte() }
                expect(data
                        .reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                        .createPlainDataChunkVersion0()
                        .createPlainDataChunkVersion2()
                        .createDirectoryDataChunkWithNumberOfReferences(2) is_ Empty)
            } when_ {
                sut.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                assertExpectedChunks(it.result)
            }

    @Test
    fun `plain data larger than three times the chunk size results in hierarchical chunks`() =
            test while_ {
                data = ByteArray(3 * version0PayloadSize + chunkSizeBytes / 2) { it.toByte() }
                expect(data
                        .reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                        .reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                        .createPlainDataChunkVersion0()
                        .createPlainDataChunkVersion0()
                        .createDirectoryDataChunkWithNumberOfReferences(2)
                        .createPlainDataChunkVersion2()
                        .createDirectoryDataChunkWithNumberOfReferences(2) is_ Empty)
            } when_ {
                sut.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                assertExpectedChunks(it.result)
            }

    @Test
    fun `plain data rest just fits into directory chunk`() =
            test while_ {
                data = ByteArray(version0PayloadSize +
                        maxChunkVersion2PayloadSizeWithReferences(1)) { it.toByte() }
                expect(data
                        .reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(1))
                        .createPlainDataChunkVersion0()
                        .createDirectoryDataChunkWithNumberOfReferences(1) is_ Empty)
            } when_ {
                sut.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                assertExpectedChunks(it.result)
            }

    @Test
    fun `plain data rest is less than version 0 size but does not fit into version 2`() =
            test while_ {
                val directoryPayloadSize = maxChunkVersion2PayloadSizeWithReferences(2)
                val secondChunkPayloadSize = chunkSizeBytes -
                        DataChunkStructure.Header.Version2.MIN_SIZE + 1
                data = ByteArray(directoryPayloadSize +
                        version0PayloadSize + secondChunkPayloadSize) { it.toByte() }
                expect(data
                        .reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                        .createPlainDataChunkVersion0()
                        .createPlainDataChunkVersion1(chunkSizeBytes -
                                DataChunkStructure.Header.Version1.SIZE - secondChunkPayloadSize)
                        .createDirectoryDataChunkWithNumberOfReferences(2) is_ Empty)
            } when_ {
                sut.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                assertExpectedChunks(it.result)
            }

    private class TestEnvironment(val chunkSizeBytes: Int, val maxReferences: Int) {
        private val base64Encoder = Base64.getEncoder()
        private fun asBase64(data: ByteArray) = base64Encoder.encodeToString(data)
        private var hashes = mutableMapOf<String, ByteArray>()
        private var nextHashValue = 1
        private val _expectedChunks = mutableSetOf<DataChunkStructure>()
        private val chunksToAdd = mutableListOf<DataChunkStructure>()
        private val directoryReservations = mutableListOf<ByteArray>()
        var data = byteArrayOf()
        val expectedChunks: Set<DataChunkStructure> get() = _expectedChunks
        val referenceBlockSize get() = DataChunkControlBlock.NON_CONTENT_SIZE +
                (hasher.hashBits - 1) / Byte.SIZE_BITS + 1
        fun addHash(data: ByteArray) = nextHashValue++.toByteArray().also {
            hashes[asBase64(data)] = it
        }

        fun getHash(data: ByteArray) =
            hashes[asBase64(data)] ?: throw Panic("Hash not found for " +
                    "${DataChunkStructure.fromBinary(data)}")

        val DataChunkStructure.hash get() = getHash(binary)
        val DataChunkStructure.hashKey get() = Key(Id(hash))
        val notEncryptor = mock<Encryptor> {
            on { encrypt(any()) }.thenAnswer { it.getArgument<ByteArray>(0) }
        }

        val Iterable<DataChunkStructure>.keys get() = map { it.hashKey }.toSet()

        val hasher = mock<Hasher> {
            on { getHash(any()) }.thenAnswer { getHash(it.getArgument<ByteArray>(0)) }
            on { hashBits }.thenAnswer { Int.SIZE_BITS }
        }

        val cryptoFactory = mock<CryptoFactory> {
            on { createHasher() }.thenAnswer { hasher }
        }

        val env = TransportTestEnvironment(
                chunkerFactory = { ChunkerImpl(chunkSizeBytes, maxReferences) },
                cryptoFactory = { cryptoFactory })

        val sut get() = env.context.chunker

        fun ByteArray.reserveForDirectory(numberOfBytes: Int): ByteArray {
            directoryReservations.add(sliceArray(0 until numberOfBytes))
            return sliceArray(numberOfBytes..lastIndex)
        }

        fun ByteArray.createDirectoryDataChunkWithNumberOfReferences(numberOfReferences: Int) =
                with(DataChunkVersion2Builder(chunkSizeBytes)) {
                    val targetSize = chunksToAdd.size - numberOfReferences
                    while (targetSize < chunksToAdd.size) {
                        references = listOf(getHash(chunksToAdd.last().binary)) + references
                        chunksToAdd.removeAt(chunksToAdd.lastIndex)
                    }

                    payload = directoryReservations.last()
                    directoryReservations.removeAt(directoryReservations.lastIndex)

                    chunksToAdd.add(chunkStructure)
                    addExpectedChunk(chunkStructure)

                    this@createDirectoryDataChunkWithNumberOfReferences
                }

        fun ByteArray.createPlainDataChunkVersion0(): ByteArray {
            expect(size is_ Not - Less than payloadBytesVersion0)
            val chunkStructure =
                    DataChunkStructure.version0(sliceArray(firstRange(payloadBytesVersion0)))
            addExpectedChunk(chunkStructure)
            chunksToAdd.add(chunkStructure)
            return sliceArray(rest(payloadBytesVersion0))
        }

        fun ByteArray.createPlainDataChunkVersion1(additionalBytes: Int): ByteArray {
            expect(size is_ Not - Less than minPayloadBytesVersion1)
            val numPayloadBytes = maxPayloadBytesVersion1 - additionalBytes
            val chunkStructure = DataChunkStructure.version1(
                    sliceArray(firstRange(numPayloadBytes)), additionalBytes)
            addExpectedChunk(chunkStructure)
            chunksToAdd.add(chunkStructure)
            return sliceArray(rest(numPayloadBytes))
        }

        fun ByteArray.createPlainDataChunkVersion2() =
                with(DataChunkVersion2Builder(chunkSizeBytes)) {
                    payload = sliceArray(0 until min(size, availablePayloadBytes))
                    addExpectedChunk(chunkStructure)
                    chunksToAdd.add(chunkStructure)
                    sliceArray(payload.size..lastIndex)
                }

        fun maxChunkVersion2PayloadSizeWithReferences(numberOfReferences: Int) =
                chunkSizeBytes -
                        DataChunkStructure.Header.Version2.MIN_SIZE -
                        numberOfReferences * referenceBlockSize

        fun assertExpectedChunks(actualChunks: Set<DataChunk>) {
            expect(actualChunks.map { it.key } is_ Equal to_ expectedChunks.keys)
            expect(actualChunks.map { DataChunkStructure.fromBinary(it.data) } is_ Equal
                    to_ expectedChunks)
        }

        private fun firstRange(sz: Int) = 0 until sz

        private fun ByteArray.rest(sz: Int) = sz..lastIndex

        private fun addExpectedChunk(chunk: DataChunkStructure) {
            addHash(chunk.binary)
            _expectedChunks += chunk
        }

        val payloadBytesVersion0 =
                chunkSizeBytes - DataChunkStructure.Header.Version0.SIZE

        private val maxPayloadBytesVersion1 =
                chunkSizeBytes - DataChunkStructure.Header.Version1.SIZE

        private val minPayloadBytesVersion1 =
                maxPayloadBytesVersion1 - DataChunkStructure.Header.Version1.MAX_ADDITIONAL_BYTES
    }
}