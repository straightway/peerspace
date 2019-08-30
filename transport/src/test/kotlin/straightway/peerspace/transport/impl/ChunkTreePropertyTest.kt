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

class ChunkTreePropertyTest {

    private companion object {
        const val CHUNK_SIZE_BYTES = 32
        const val REF_BLOCK_SIZE_BYTES = 3
        const val MAX_REFERENCES = 2
        const val MAX_ONE_REF_SIZE =
                CHUNK_SIZE_BYTES - DataChunkVersion2.Header.MIN_SIZE - REF_BLOCK_SIZE_BYTES +
                CHUNK_SIZE_BYTES - DataChunkVersion0.Header.SIZE
        const val MAX_TWO_REF_SIZE =
                CHUNK_SIZE_BYTES - DataChunkVersion2.Header.MIN_SIZE - 2 * REF_BLOCK_SIZE_BYTES +
                2 * (CHUNK_SIZE_BYTES - DataChunkVersion0.Header.SIZE)
    }

    @Test
    fun `directoryPayloadSizeBytes for empty data is full version 2 payload`() =
            Given {
                ChunkProperties(CHUNK_SIZE_BYTES, MAX_REFERENCES, REF_BLOCK_SIZE_BYTES, 1, 0).getChunkTreeInfoFor(0)
            } when_ {
                directoryPayloadSizeBytes
            } then {
                expect(it.result is_ Equal to_ CHUNK_SIZE_BYTES - DataChunkVersion2.Header.MIN_SIZE)
            }

    @Test
    fun `directoryPayloadSizeBytes for data fully fitting into directory without references`() =
            Given {
                ChunkProperties(CHUNK_SIZE_BYTES, MAX_REFERENCES, REF_BLOCK_SIZE_BYTES, 1, 0).getChunkTreeInfoFor(1)
            } when_ {
                directoryPayloadSizeBytes
            } then {
                expect(it.result is_ Equal to_ CHUNK_SIZE_BYTES - DataChunkVersion2.Header.MIN_SIZE)
            }

    @Test
    fun `directoryPayloadSizeBytes for data requiring one reference`() =
            Given {
                ChunkProperties(CHUNK_SIZE_BYTES, MAX_REFERENCES, REF_BLOCK_SIZE_BYTES, 1, 0)
                        .getChunkTreeInfoFor(CHUNK_SIZE_BYTES)
            } when_ {
                directoryPayloadSizeBytes
            } then {
                expect(it.result is_ Equal to_
                        CHUNK_SIZE_BYTES - DataChunkVersion2.Header.MIN_SIZE - REF_BLOCK_SIZE_BYTES)
            }

    @Test
    fun `directoryPayloadSizeBytes for data requiring two references`() =
            Given {
                ChunkProperties(CHUNK_SIZE_BYTES, MAX_REFERENCES, REF_BLOCK_SIZE_BYTES, 1, 0)
                        .getChunkTreeInfoFor(MAX_ONE_REF_SIZE + 1)
            } when_ {
                directoryPayloadSizeBytes
            } then {
                expect(it.result is_ Equal to_
                        CHUNK_SIZE_BYTES - DataChunkVersion2.Header.MIN_SIZE - 2 * REF_BLOCK_SIZE_BYTES)
            }

    @Test
    fun `directoryPayloadSizeBytes for data requiring a deeper tree structure with one sub directory`() =
            Given {
                ChunkProperties(CHUNK_SIZE_BYTES, MAX_REFERENCES, REF_BLOCK_SIZE_BYTES, 1, 0)
                        .getChunkTreeInfoFor(MAX_TWO_REF_SIZE + 1)
            } when_ {
                directoryPayloadSizeBytes
            } then {
                // Top directory has one sub directories with two sub sub directories
                expect(it.result is_ Equal to_
                        CHUNK_SIZE_BYTES - DataChunkVersion2.Header.MIN_SIZE - 1 * REF_BLOCK_SIZE_BYTES)
            }

    @Test
    fun `directoryPayloadSizeBytes for data requiring a deeper tree structure with two sub directories`() =
            Given {
                ChunkProperties(CHUNK_SIZE_BYTES, MAX_REFERENCES, REF_BLOCK_SIZE_BYTES, 1, 0)
                        .getChunkTreeInfoFor(2 * MAX_TWO_REF_SIZE)
            } when_ {
                directoryPayloadSizeBytes
            } then {
                // Top directory has one sub directories with two sub sub directories
                expect(it.result is_ Equal to_
                        CHUNK_SIZE_BYTES - DataChunkVersion2.Header.MIN_SIZE - 2 * REF_BLOCK_SIZE_BYTES)
            }
}