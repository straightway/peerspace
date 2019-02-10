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
package straightway.peerspace.data

import org.junit.jupiter.api.Test
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataChunkStructureTest {

    private companion object {
        val payload = byteArrayOf(4, 5, 6)
        val controlBlock = DataChunkControlBlock(
                DataChunkControlBlockType.ReferencedChunk,
                0xA,
                byteArrayOf(1, 2, 3))
        val binaryChunk =
                byteArrayOf(
                        0x01,                                // version
                        // Begin referenced chunk control block
                        DataChunkControlBlockType.ReferencedChunk.id, // type
                        0xA0.toByte(), 0x03,                 // cpls and content size
                        1, 2, 3,                             // reference content
                        // End referenced chunk control block
                        0,                                   // CEND
                        4, 5, 6)                             // payload

    }

    private val test get() =
        Given {
            DataChunkStructure(listOf(controlBlock), payload)
        }

    @Test
    fun `version is accessible`() =
            test when_ {
                version
            } then {
                expect(it.result is_ Equal to_ 0x01)
            }

    @Test
    fun `controlBlocks is accessible`() =
            test when_ {
                controlBlocks
            } then {
                expect(it.result is_ Equal to_ listOf(controlBlock))
            }

    @Test
    fun `payload is accessible`() =
            test when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ payload)
            }

    @Test
    fun `created chunk has specified key`() =
            test when_ {
                createChunk(Key(Id("id")))
            } then {
                expect(it.result.key is_ Equal to_ Key(Id("id")))
            }

    @Test
    fun `created chunk has specified data`() =
            test when_ {
                createChunk(Key(Id("id")))
            } then {
                expect(it.result.data is_ Equal to_ binary)
            }

    @Test
    fun `binary has specified version`() =
            test when_ {
                binary[0]
            } then {
                expect(it.result is_ Equal to_ 0x01)
            }

    @Test
    fun `binary has specified chunk control block`() =
            test when_ {
                binary
            } then {
                expect(DataChunkControlBlock(it.result, 1) is_ Equal to_ controlBlock)
            }

    @Test
    fun `binary has specified payload`() =
            test  when_ {
                binary
            } then {
                expect(it.result.sliceArray((it.result.lastIndex - 2)..it.result.lastIndex) is_
                        Equal to_ payload)
            }

    @Test
    fun `structure without control block has version 0`() =
            Given {
                DataChunkStructure(listOf(), payload)
            } when_ {
                version
            } then {
                expect(it.result is_ Equal to_ 0)
            }

    @Test
    fun `version 0 chunk has no CEND marker`() =
            Given {
                DataChunkStructure(listOf(), payload)
            } when_ {
                binary
            } then {
                expect(it.result is_ Equal to_ byteArrayOf(0x00) + payload)
            }

    @Test
    fun `version from binary version 0`() =
            Given {
                DataChunkStructure(byteArrayOf(0x00, 1, 2, 3))
            } when_ {
                version
            } then {
                expect(it.result is_ Equal to_ 0)
            }

    @Test
    fun `payload from binary version 0`() =
            Given {
                DataChunkStructure(byteArrayOf(0x00, 1, 2, 3))
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ byteArrayOf(1, 2, 3))
            }

    @Test
    fun `control blocks from binary version 0 is empty`() =
            Given {
                DataChunkStructure(byteArrayOf(0x00, 1, 2, 3))
            } when_ {
                controlBlocks
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `version from binary version 1`() =
            Given {
                DataChunkStructure(binaryChunk)
            } when_ {
                version
            } then {
                expect(it.result is_ Equal to_ 1)
            }

    @Test
    fun `payload from binary version 1`() =
            Given {
                DataChunkStructure(binaryChunk)
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ byteArrayOf(4, 5, 6))
            }

    @Test
    fun `control block from binary version 1`() =
            Given {
                DataChunkStructure(binaryChunk)
            } when_ {
                controlBlocks
            } then {
                expect(it.result is_ Equal to_ Values(controlBlock))
            }

    @Test
    fun `multiple control block from binary version 1`() {
        val chunkStructure = DataChunkBuilder {
            signMode = DataChunkSignMode.ListIdKey
            signature = byteArrayOf(1, 2, 3)
            contentKey = byteArrayOf(4, 5, 6)
            payload = byteArrayOf(7, 8, 9)
        }

        Given {
            DataChunkStructure(chunkStructure.binary)
        } when_ {
            controlBlocks
        } then {
            expect(it.result is_ Equal to_ chunkStructure.controlBlocks)
        }
    }
}