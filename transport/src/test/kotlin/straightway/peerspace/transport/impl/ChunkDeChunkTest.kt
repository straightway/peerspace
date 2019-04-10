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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.DeChunkerCrypto
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import java.util.stream.Stream

class ChunkDeChunkTest : KoinLoggingDisabler() {

    private companion object {
        const val chunkSizeBytes = 0x14
        const val maxReferences = 2
        val testValueRange = (0..(chunkSizeBytes * maxReferences * 3))

         @Suppress("unused")
         @JvmStatic
         fun dataSizesForTest() =
                Stream.of(*testValueRange.map { Arguments.of(it) }.toTypedArray())
    }

    private fun test(dataToChopSizeGetter: ChunkEnvironmentValues.() -> Int) =
            Given {
                ChunkingTestEnvironment(chunkSizeBytes, maxReferences) {
                    ByteArray(dataToChopSizeGetter()) { it.toByte() }
                }.apply {
                    isCreatingHashOnTheFly = true
                }
            }

    @ParameterizedTest
    @MethodSource("dataSizesForTest")
    fun `test chunking and deChunking of size`(size: Int) =
            test { size } when_ {
                val chunks =
                        env.chunker.chopToChunks(data, ChunkerCrypto.forPlainChunk(notEncryptor))
                env.deChunker.tryCombining(chunks, DeChunkerCrypto(decryptor = notEncryptor))
            } then {
                expect(it.result is_ Equal to_ data)
            }
}