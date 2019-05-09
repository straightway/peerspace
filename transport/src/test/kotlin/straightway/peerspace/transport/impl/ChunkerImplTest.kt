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

import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.chunker
import straightway.peerspace.transport.createHasher
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class ChunkerImplTest : KoinLoggingDisabler() {

    private companion object {
        const val chunkSizeBytes = 0x20
        const val maxReferences = 2
        const val version0PayloadSize = chunkSizeBytes - 2 * DataChunkVersion0.Header.SIZE
    }

    private fun test(
            cryptoBlockSize: Int = 1,
            dataToChopToChunkGetter: ChunkEnvironmentValues.() -> ByteArray
    ) =
            Given {
                ChunkingTestEnvironment(
                        chunkSizeBytes,
                        maxReferences,
                        cryptoBlockSize,
                        dataToChopToChunkGetter)
            }

    @Test
    fun `small plain data results in single chunk version 2`() {
        test { byteArrayOf() } while_ {
            data.createPlainDataChunkVersion2().end()
        } when_ {
            chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
        } then {
            assertExpectedChunks(it.result)
        }
    }

    @Test
    fun `data is encrypted using passed encryptor`() {
        test { byteArrayOf() } while_ {
            cryptor = negatingEncryptor
            data.createPlainDataChunkVersion2().end()
        } when_ {
            chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(cryptor))
        } then {
            assertExpectedChunks(it.result)
        }
    }

    @Test
    fun `plain data of exact version 0 payload size produces version 0 chunk`() {
        test { ByteArray(version0PayloadSize) { it.toByte() } } while_ {
            data.createPlainDataChunkVersion0().end()
        } when_ {
            chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
        } then {
            assertExpectedChunks(it.result)
        }
    }

    @Test
    fun `plain data between version 0 and version 2 payload size produces version 1 chunk`() {
        test { ByteArray(version0PayloadSize - 2) { it.toByte() } } while_ {
            data.createPlainDataChunkVersion1(1).end()
        } when_ {
            chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
        } then {
            assertExpectedChunks(it.result)
        }
    }

    @Test
    fun `plain data chunk id is equal to hash of chunk's binary data`() =
            test { byteArrayOf(1, 2, 3) } while_ {
                data.createPlainDataChunkVersion2().end()
            } when_ {
                chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                expect(it.result.single().key is_ Equal
                        to_ hashKey(setUpChunks.single()))
            }

    @Test
    fun `plain data larger than chunk size results in two chunks`() =
            test { ByteArray(version0PayloadSize + 1) { it.toByte() } } while_ {
                data.reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(1))
                        .createPlainDataChunkVersion2()
                        .createDirectoryDataChunkWithNumberOfReferences(1)
                        .end()
            } when_ {
                chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                assertExpectedChunks(it.result)
            }

    @Test
    fun `plain data larger than twice the chunk size results in three chunks`() =
            test { ByteArray(2 * version0PayloadSize + 1) { it.toByte() } } while_ {
                data.reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                        .createPlainDataChunkVersion0()
                        .createPlainDataChunkVersion2()
                        .createDirectoryDataChunkWithNumberOfReferences(2)
                        .end()
            } when_ {
                chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                assertExpectedChunks(it.result)
            }

    @Test
    fun `plain data larger than three times the chunk size results in hierarchical chunks`() =
            test {
                ByteArray(3 * version0PayloadSize + unencryptedChunkSizeBytes / 2) { it.toByte() }
            } while_ {
                data.reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                        .reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                        .createPlainDataChunkVersion0()
                        .createPlainDataChunkVersion0()
                        .createDirectoryDataChunkWithNumberOfReferences(2)
                        .createPlainDataChunkVersion2()
                        .createDirectoryDataChunkWithNumberOfReferences(2)
                        .end()
            } when_ {
                chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                assertExpectedChunks(it.result)
            }

    @Test
    fun `plain data rest just fits into directory chunk`() =
            test {
                ByteArray(version0PayloadSize +
                        maxChunkVersion2PayloadSizeWithReferences(1)) { it.toByte() }
            } while_ {
                data.reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(1))
                        .createPlainDataChunkVersion0()
                        .createDirectoryDataChunkWithNumberOfReferences(1)
                        .end()
            } when_ {
                chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                assertExpectedChunks(it.result)
            }

    @Test
    fun `plain data rest is less than version 0 size but does not fit into version 2`() {
        val secondChunkPayloadSize =
                chunkSizeBytes - DataChunkVersion2.Header.MIN_SIZE + 1
        test {
            val directoryPayloadSize = maxChunkVersion2PayloadSizeWithReferences(2)
            ByteArray(directoryPayloadSize +
                    version0PayloadSize + secondChunkPayloadSize) { it.toByte() }
        } while_ {
            data.reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                    .createPlainDataChunkVersion0()
                    .createPlainDataChunkVersion1(unencryptedChunkSizeBytes -
                            DataChunkVersion1.Header.SIZE - secondChunkPayloadSize)
                    .createDirectoryDataChunkWithNumberOfReferences(2)
                    .end()
        } when_ {
            chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
        } then {
            assertExpectedChunks(it.result)
        }
    }

    @Test
    fun `encryption respects data block size`() =
            test(cryptoBlockSize = 0x08) {
                byteArrayOf(1, 2, 3)
            } while_ {
                data.createPlainDataChunkVersion2().end()
            } when_ {
                chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
            } then {
                assertExpectedChunks(it.result)
            }

    /*@Test
    fun `asymmetric encryption creates content key`() =
            test{
                byteArrayOf(1, 2, 3)
            } when_ {

            } then {

            }*/

    //region Private

    private val ChunkingTestEnvironment.chunker get() = env.context.chunker
    private val ChunkingTestEnvironment.hasher get() = env.context.createHasher()

    private fun ChunkingTestEnvironment.keys(chunks: Iterable<DataChunkStructure>) =
            chunks.map { hashKey(it) }.toSet()

    private fun ChunkingTestEnvironment.hashKey(chunk: DataChunkStructure) =
            Key(Id(hash(chunk)))

    private fun ChunkingTestEnvironment.hash(chunk: DataChunkStructure) =
            hasher.getHash(chunk.binary)

    private fun ChunkingTestEnvironment.assertExpectedChunks(actualChunks: Set<DataChunk>) {
        expect(actualChunks.map { it.key } is_ Equal to_ keys(setUpChunks))
        expect(actualChunks.map { DataChunkStructure.fromBinary(it.data) } is_ Equal
                to_ setUpChunks)
    }

    //endregion
}