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
import straightway.error.Panic
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Id
import straightway.peerspace.transport.DeChunkerCrypto
import straightway.peerspace.transport.deChunker
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Null
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DeChunkerImplTest : KoinLoggingDisabler() {

    private companion object {
        const val chunkSizeBytes = 0x20
        const val maxReferences = 2
    }

    private val test get() = test { byteArrayOf() }
    private fun test(dataToChopToChunkGetter: ChunkEnvironmentValues.() -> ByteArray) =
            Given {
                ChunkingTestEnvironment(chunkSizeBytes, maxReferences, dataToChopToChunkGetter)
            }

    @Test
    fun `getReferencedChunks yields no references for version 0 chunk data`() =
            test when_ {
                deChunker.getReferencedChunks(
                        DataChunkStructure.version0(byteArrayOf(1, 2, 3)).binary,
                        DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `getReferencedChunks yields no references for version 1 chunk data`() =
            test when_ {
                deChunker.getReferencedChunks(
                        DataChunkStructure.version1(byteArrayOf(1, 2, 3), 2).binary,
                        DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `getReferencedChunks yields references for version 2 chunk data`() =
            test when_ {
                val chunk = DataChunkVersion2Builder(unencryptedChunkSizeBytes).apply {
                    references = listOf(byteArrayOf(1, 2, 3))
                }.chunkStructure.binary
                deChunker.getReferencedChunks(chunk, DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Equal to_ listOf(Id(byteArrayOf(1, 2, 3))))
            }

    @Test
    fun `tryCombining of no chunks is null`() =
            test when_ {
                deChunker.tryCombining(listOf(), DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.nullableResult is_ Null)
            }

    @Test
    fun `tryCombining of single chunk without references returns payload`() =
            test { byteArrayOf(1, 2, 3) } when_ {
                data.createPlainDataChunkVersion2().end()
                deChunker.tryCombining(chunks, DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Equal to_ data)
            }

    @Test
    fun `tryCombining of directory last chunk with one reference`() =
            test { ByteArray(payloadBytesVersion0 + 3) { it.toByte() } } when_ {
                data.reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(1))
                    .createPlainDataChunkVersion2()
                    .createDirectoryDataChunkWithNumberOfReferences(1)
                    .end()
                deChunker.tryCombining(chunks, DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Equal to_ data)
            }

    @Test
    fun `tryCombining of directory first chunk with one reference`() =
            test { ByteArray(payloadBytesVersion0 + 3) { it.toByte() } } when_ {
                data.reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(1))
                    .createPlainDataChunkVersion2()
                    .createDirectoryDataChunkWithNumberOfReferences(1)
                    .end()
                deChunker.tryCombining(chunks.reversed(), DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Equal to_ data)
            }

    @Test
    fun `tryCombining of directory with two references`() =
            test { ByteArray(2 * payloadBytesVersion0 + 3) { it.toByte() } } when_ {
                data.reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                    .createPlainDataChunkVersion0()
                    .createPlainDataChunkVersion2()
                    .createDirectoryDataChunkWithNumberOfReferences(2)
                    .end()
                deChunker.tryCombining(chunks, DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Equal to_ data)
            }

    @Test
    fun `tryCombining of directory with two references in reverse order`() =
            test { ByteArray(2 * payloadBytesVersion0 + 3) { it.toByte() } } when_ {
                data.reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                    .createPlainDataChunkVersion0()
                    .createPlainDataChunkVersion2()
                    .createDirectoryDataChunkWithNumberOfReferences(2)
                    .end()
                deChunker.tryCombining(chunks.reversed(), DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Equal to_ data)
            }

    @Test
    fun `tryCombining of two directories`() =
            test {
                ByteArray(2 * maxChunkVersion2PayloadSizeWithReferences(1) +
                        payloadBytesVersion0 + 3) {
                    it.toByte()
                }
            } when_ {
                data.reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(2))
                    .reserveForDirectory(maxChunkVersion2PayloadSizeWithReferences(1))
                    .createPlainDataChunkVersion0()
                    .createDirectoryDataChunkWithNumberOfReferences(1)
                    .createPlainDataChunkVersion2()
                    .createDirectoryDataChunkWithNumberOfReferences(2)
                    .end()
                deChunker.tryCombining(chunks, DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Equal to_ data)
            }

    @Test
    fun `tryCombining of two independent chunks yields null`() =
            test { ByteArray(2 * payloadBytesVersion0) { it.toByte() } } when_ {
                data.createPlainDataChunkVersion0()
                    .createPlainDataChunkVersion0()
                deChunker.tryCombining(chunks, DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.nullableResult is_ Null)
            }

    @Test
    fun `tryCombining with multiply referenced chunk`() =
            test { byteArrayOf() } when_ {
                val plainChunk = DataChunkStructure.version2(listOf(), byteArrayOf(1)).encrypted
                val plainChunkHash = addHash(plainChunk.binary)
                val directory = DataChunkStructure.version2(
                        listOf(
                                DataChunkControlBlock(
                                        DataChunkControlBlockType.ReferencedChunk,
                                        0x0,
                                        plainChunkHash),
                                DataChunkControlBlock(
                                        DataChunkControlBlockType.ReferencedChunk,
                                        0x0,
                                        plainChunkHash)),
                        byteArrayOf(2)).encrypted
                addHash(directory.binary)

                deChunker.tryCombining(
                        listOf(directory.createChunk(), plainChunk.createChunk()),
                        DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Equal to_ byteArrayOf(2, 1, 1))
            }

    @Test
    fun `tryCombining with reference loop`() =
            test { byteArrayOf() } when_ {
                val loopHash = byteArrayOf(1, 2, 3)
                val loop = DataChunkStructure.version2(
                        listOf(DataChunkControlBlock(
                                DataChunkControlBlockType.ReferencedChunk,
                                0x0,
                                loopHash)),
                        byteArrayOf(4, 5, 6))
                setHash(loop.binary, loopHash)
                val directory = DataChunkStructure.version2(
                        listOf(DataChunkControlBlock(
                                DataChunkControlBlockType.ReferencedChunk,
                                0x0,
                                loopHash)),
                        byteArrayOf(7, 8, 9))
                addHash(directory.binary)

                deChunker.tryCombining(
                        listOf(directory.createChunk(), loop.createChunk()),
                        DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect({ it.result } does Throw.type<Panic>())
            }

    @Test
    fun `tryCombining with indirect reference loop`() =
            test { byteArrayOf() } when_ {
                val loop1Hash = byteArrayOf(1, 2, 3)
                val loop2Hash = byteArrayOf(2, 3, 4)
                val loop1 = DataChunkStructure.version2(
                        listOf(DataChunkControlBlock(
                                DataChunkControlBlockType.ReferencedChunk,
                                0x0,
                                loop2Hash)),
                        byteArrayOf(4, 5, 6))
                setHash(loop1.binary, loop1Hash)
                val loop2 = DataChunkStructure.version2(
                        listOf(DataChunkControlBlock(
                                DataChunkControlBlockType.ReferencedChunk,
                                0x0,
                                loop1Hash)),
                        byteArrayOf(5, 6, 7))
                setHash(loop2.binary, loop2Hash)
                val directory = DataChunkStructure.version2(
                        listOf(DataChunkControlBlock(
                                DataChunkControlBlockType.ReferencedChunk,
                                0x0,
                                loop1Hash)),
                        byteArrayOf(7, 8, 9))
                addHash(directory.binary)

                deChunker.tryCombining(
                        listOf(directory.createChunk(), loop2.createChunk(), loop1.createChunk()),
                        DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect({ it.result } does Throw.type<Panic>())
            }

    @Test
    fun `tryCombining of directory with missing referenced chunk is null`() =
            test { ByteArray(payloadBytesVersion0 + 3) { it.toByte() } } when_ {
                val directory = DataChunkStructure.version2(
                        listOf(DataChunkControlBlock(
                                DataChunkControlBlockType.ReferencedChunk,
                                0x0,
                                byteArrayOf(1, 2, 3))),
                        byteArrayOf(7, 8, 9)).encrypted
                addHash(directory.binary)
                deChunker.tryCombining(
                        listOf(directory.createChunk()),
                        DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.nullableResult is_ Null)
            }

    @Test
    fun `tryCombining decrypts chunks`() =
            test { byteArrayOf(1, 2, 3) } when_ {
                cryptor = negatingEncryptor
                data.createPlainDataChunkVersion2().end()
                deChunker.tryCombining(chunks, DeChunkerCrypto(decryptor = cryptor))
            } then {
                expect(it.result is_ Equal to_ data)
            }

    private val ChunkingTestEnvironment.deChunker get() = env.context.deChunker
    private val ChunkingTestEnvironment.chunks get() = setUpChunks.map { it.createChunk() }
}