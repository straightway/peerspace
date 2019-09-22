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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.transport.ChunkProperties
import straightway.peerspace.transport.ChunkTreeInfo
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.createChunkTreeCreator
import straightway.peerspace.transport.trace
import straightway.testing.TestTraceProvider
import straightway.testing.TraceOnFailure
import straightway.testing.bdd.Given
import straightway.utils.TraceProvider
import straightway.utils.Tracer

@ExtendWith(TraceOnFailure::class)
class ChunkTreeCreatorImplTest : KoinLoggingDisabler(), TestTraceProvider {

    private companion object {
        const val chunkSizeBytes = 0x20
        const val maxReferences = 2
        const val version0PayloadSize = chunkSizeBytes - 2 * DataChunkVersion0.Header.SIZE
    }

    private var testTrace: Tracer? =  null

    private fun test(
            cryptoBlockSize: Int = 1,
            dataToChopToChunkGetter: ChunkEnvironmentValues.() -> ByteArray
    ) =
            Given {
                val result = object {
                    val treeInfos = mutableMapOf<Int, ChunkTreeInfo>()
                    val env = ChunkingTestEnvironment(
                            chunkSizeBytes,
                            maxReferences,
                            cryptoBlockSize,
                            dataToChopToChunkGetter,
                            treeInfoFactory = { _, size -> treeInfos[size]!! },
                            tracerFactory = { Tracer { true } })
                    var crypto = ChunkerCrypto.forPlainChunk(env.cryptor)
                    val sut by lazy {
                        env.env.context.createChunkTreeCreator(env.data, crypto, ChunkProperties(
                                chunkSizeBytes = env.chunkSizeBytes,
                                maxReferences = 2,
                                referenceBlockSizeBytes = 0,
                                dataBlockSizeBytes = cryptoBlockSize,
                                cryptoContainerHeaderSizeBytes = DataChunkVersion0.Header.SIZE
                        ))
                    }
                }

                testTrace = result.env.env.context.trace

                result
            }

    override val traces: Collection<String> get() = (testTrace as TraceProvider).traces
            .map { it.toString().replace("straightway.peerspace.transport.impl.", "") }

    @BeforeEach
    fun setUp() {
        testTrace = null
    }

    @Test
    fun `chunk fitting into exactly int version 0 single data structure without own crypto key`() =
            test {
                ByteArray(version0PayloadSize) { it.toByte() }
            } while_ {
                with(env) {
                    cryptor = negatingEncryptor
                    crypto = ChunkerCrypto.forPlainChunk(cryptor)
                    data.createPlainDataChunkVersion0().end()
                }
            } when_ {
                sut.chunks
            } then {
                env.assertExpectedChunks(it.result)
            }

    @Test
    fun `chunk smaller than version 1 single data structure without own crypto key`() =
            test {
                byteArrayOf(0x33)
            } while_ {
                with(env) {
                    data.createPlainDataChunkVersion2().end()
                }
            } when_ {
                sut.chunks
            } then {
                env.assertExpectedChunks(it.result)
            }

    @Test
    fun `chunk smaller than version 0 but larger than version 2 single data structure without own crypto key`() =
            test {
                ByteArray(version0PayloadSize - 2) { it.toByte() }
            } while_ {
                with(env) {
                    data.createPlainDataChunkVersion1(1).end()
                }
            } when_ {
                sut.chunks
            } then {
                env.assertExpectedChunks(it.result)
            }

    @Test
    fun `chunk larger than version 0 chunk tree without own crypto key`() =
            test {
                ByteArray(version0PayloadSize + 1) { it.toByte() }
            } while_ {
                with(env) {
                    val directoryPayloadSizeBytes = maxChunkVersion2PayloadSizeWithReferences(1)
                    treeInfos[data.size] = ChunkTreeInfo(version0PayloadSize, directoryPayloadSizeBytes)
                    data.reserveForDirectory(directoryPayloadSizeBytes)
                            .createPlainDataChunkVersion2()
                            .createDirectoryDataChunkWithNumberOfReferences(1)
                            .end()
                }
            } when_ {
                sut.chunks
            } then {
                env.assertExpectedChunks(it.result)
            }

    @Test
    fun `chunk requiring two sub chunks without own crypto key`() =
            test {
                ByteArray(maxChunkVersion2PayloadSizeWithReferences(1) + version0PayloadSize + 1) { it.toByte() }
            } while_ {
                with(env) {
                    val directoryPayloadSizeBytes = maxChunkVersion2PayloadSizeWithReferences(2)
                    treeInfos[data.size] = ChunkTreeInfo(version0PayloadSize, directoryPayloadSizeBytes)
                    data.reserveForDirectory(directoryPayloadSizeBytes)
                            .createPlainDataChunkVersion0()
                            .createPlainDataChunkVersion2()
                            .createDirectoryDataChunkWithNumberOfReferences(2)
                            .end()
                }
            } when_ {
                sut.chunks
            } then {
                env.assertExpectedChunks(it.result)
            }

    @Test
    fun `block size leading to padding with random bytes`() =
            test(cryptoBlockSize = 0x8) {
                byteArrayOf(0x33)
            } while_ {
                with(env) {
                    data.createPlainDataChunkVersion2().end()
                }
            } when_ {
                sut.chunks
            } then {
                env.assertExpectedChunks(it.result)
            }

    @Test
    fun `plain version 2 chunk with own crypto key`() =
            test {
                byteArrayOf(0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x78, 0x79)
            } while_ {
                with(env) {
                    cryptor = notEncryptor
                    encryptorProperties.maxClearTextBytes = 8
                    crypto = ChunkerCrypto.forPlainChunk(notEncryptor)
                    data.createEncryptedPlainDataChunkVersion2(newSymmetricCryptor).end()
                }
            } when_ {
                sut.chunks
            } then {
                env.assertExpectedChunks(it.result)
            }
}