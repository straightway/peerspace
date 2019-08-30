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
import straightway.peerspace.transport.ChunkProperties
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class ChunkPropertiesHelperTest {

    private companion object {
        const val CHUNK_SIZE_BYTES = 32
        const val MAX_REFERENCES = 2
        const val REFERENCES_BLOCK_SIZE_BYTES = 7
        const val DATA_BLOCK_SIZE_BYTES = 8
        const val CRYPTO_CONTAINER_HEADER_SIZE_BYTES = 19
        val sut = ChunkProperties(
                CHUNK_SIZE_BYTES,
                MAX_REFERENCES,
                REFERENCES_BLOCK_SIZE_BYTES,
                DATA_BLOCK_SIZE_BYTES,
                CRYPTO_CONTAINER_HEADER_SIZE_BYTES)
    }

    private val test get() = Given { sut }

    @Test
    fun `cryptoContainerPayloadSizeBytes is computed properly`() =
            test when_ {
                cryptoContainerPayloadSizeBytes
            } then {
                expect(it.result is_ Equal to_ CHUNK_SIZE_BYTES - CRYPTO_CONTAINER_HEADER_SIZE_BYTES)
            }

    @Test
    fun `cryptoContainerPayloadSizeRespectingBlockSizeBytes for one byte`() =
            Given {
                sut.copy(chunkSizeBytes = 0, cryptoContainerHeaderSizeBytes = 0)
            } when_ {
                cryptoContainerPayloadSizeRespectingBlockSizeBytes
            } then {
                expect(it.result is_ Equal to_ 0)
            }

    @Test
    fun `cryptoContainerPayloadSizeRespectingBlockSizeBytes for block size minus one bytes`() =
            Given {
                sut.copy(chunkSizeBytes = DATA_BLOCK_SIZE_BYTES - 1, cryptoContainerHeaderSizeBytes = 0)
            } when_ {
                cryptoContainerPayloadSizeRespectingBlockSizeBytes
            } then {
                expect(it.result is_ Equal to_ 0)
            }

    @Test
    fun `cryptoContainerPayloadSizeRespectingBlockSizeBytes for block size bytes`() =
            Given {
                sut.copy(chunkSizeBytes = DATA_BLOCK_SIZE_BYTES, cryptoContainerHeaderSizeBytes = 0)
            } when_ {
                cryptoContainerPayloadSizeRespectingBlockSizeBytes
            } then {
                expect(it.result is_ Equal to_ DATA_BLOCK_SIZE_BYTES)
            }

    @Test
    fun `cryptoContainerPayloadSizeRespectingBlockSizeBytes for block twice size minus one bytes`() =
            Given {
                sut.copy(chunkSizeBytes = 2 * DATA_BLOCK_SIZE_BYTES - 1, cryptoContainerHeaderSizeBytes = 0)
            } when_ {
                cryptoContainerPayloadSizeRespectingBlockSizeBytes
            } then {
                expect(it.result is_ Equal to_ DATA_BLOCK_SIZE_BYTES)
            }

    @Test
    fun `cryptoContainerPayloadSizeRespectingBlockSizeBytes for twice block size bytes`() =
            Given {
                sut.copy(chunkSizeBytes = 2 * DATA_BLOCK_SIZE_BYTES, cryptoContainerHeaderSizeBytes = 0)
            } when_ {
                cryptoContainerPayloadSizeRespectingBlockSizeBytes
            } then {
                expect(it.result is_ Equal to_ 2 * DATA_BLOCK_SIZE_BYTES)
            }

    @Test
    fun `cryptoContainerPayloadSizeRespectingBlockSizeBytes for three times block size minus one bytes`() =
            Given {
                sut.copy(chunkSizeBytes = 3 * DATA_BLOCK_SIZE_BYTES - 1, cryptoContainerHeaderSizeBytes = 0)
            } when_ {
                cryptoContainerPayloadSizeRespectingBlockSizeBytes
            } then {
                expect(it.result is_ Equal to_ 2 * DATA_BLOCK_SIZE_BYTES)
            }

    @Test
    fun `maxVersion2PayloadSizeInCrytoContainerBytes is crypto container size minus version 2 header size`() =
            Given {
                sut.copy(
                        chunkSizeBytes = 3 * DATA_BLOCK_SIZE_BYTES - 1,
                        cryptoContainerHeaderSizeBytes = 0)
            } when_ {
                maxVersion2PayloadSizeInCryptoContainerBytes
            } then {
                expect(it.result is_ Equal to_ 2 * DATA_BLOCK_SIZE_BYTES - DataChunkVersion2.Header.MIN_SIZE)
            }

    @Test
    fun `version0PayloadSizeInCryptoContainerBytes is crypto container size minus version 0 header size`() =
            Given {
                sut.copy(
                        chunkSizeBytes = 3 * DATA_BLOCK_SIZE_BYTES - 1,
                        cryptoContainerHeaderSizeBytes = 0)
            } when_ {
                version0PayloadSizeInCryptoContainerBytes
            } then {
                expect(it.result is_ Equal to_ 2 * DATA_BLOCK_SIZE_BYTES - DataChunkVersion0.Header.SIZE)
            }

    @Test
    fun `getMaxVersion2PayloadSizeWithReferencesInCryptoContainerBytes with 0 references`() =
            Given {
                sut.copy(
                        chunkSizeBytes = 3 * DATA_BLOCK_SIZE_BYTES - 1,
                        cryptoContainerHeaderSizeBytes = 0)
            } when_ {
                getMaxVersion2PayloadSizeWithReferencesInCryptoContainerBytes(0)
            } then {
                expect(it.result is_ Equal to_ maxVersion2PayloadSizeInCryptoContainerBytes)
            }

    @Test
    fun `getMaxVersion2PayloadSizeWithReferencesInCryptoContainerBytes with 1 reference`() =
            Given {
                sut.copy(
                        chunkSizeBytes = 3 * DATA_BLOCK_SIZE_BYTES - 1,
                        cryptoContainerHeaderSizeBytes = 0,
                        referenceBlockSizeBytes = 2)
            } when_ {
                getMaxVersion2PayloadSizeWithReferencesInCryptoContainerBytes(1)
            } then {
                expect(it.result is_ Equal to_
                        maxVersion2PayloadSizeInCryptoContainerBytes - referenceBlockSizeBytes)
            }

    @Test
    fun `getMaxVersion2PayloadSizeWithReferencesInCryptoContainerBytes with 2 references`() =
            Given {
                sut.copy(
                        chunkSizeBytes = 3 * DATA_BLOCK_SIZE_BYTES - 1,
                        cryptoContainerHeaderSizeBytes = 0,
                        referenceBlockSizeBytes = 2)
            } when_ {
                getMaxVersion2PayloadSizeWithReferencesInCryptoContainerBytes(2)
            } then {
                expect(it.result is_ Equal to_
                        maxVersion2PayloadSizeInCryptoContainerBytes - 2 * referenceBlockSizeBytes)
            }
}