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
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Throw
import straightway.testing.flow.Values
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.getUnsignedShort

@Suppress("ReplaceCallWithBinaryOperator")
class DataChunkStructure_Version2_Test {

    private companion object {
        const val VERSION2 = 2.toByte()
        val testPayload = byteArrayOf(4, 5, 6)
        val controlBlock = DataChunkControlBlock(
                DataChunkControlBlockType.ReferencedChunk,
                0xA,
                byteArrayOf(1, 2, 3))
        val binaryChunk =
                byteArrayOf(
                        VERSION2,
                        // Begin referenced chunk control block
                        DataChunkControlBlockType.ReferencedChunk.id, // type
                        0xA0.toByte(), 0x03,                 // cpls and content size
                        1, 2, 3,                             // reference content
                        // End referenced chunk control block
                        DataChunkStructure.Header.Version2.CEND,
                        0x00, 0x03,                          // payload size
                        4, 5, 6)                             // payload
    }

    private val test get() =
        Given {
            DataChunkStructure.version2(listOf(controlBlock), testPayload)
        }

    @Test
    fun `binary of version 2 has version 2`() =
            test when_ {
                binary[0]
            } then {
                expect(it.result is_ Equal to_ VERSION2)
            }

    @Test
    fun `binary of version 2 has specified chunk control block`() =
            test when_ {
                binary
            } then {
                expect(DataChunkControlBlock(it.result.sliceArray(1..it.result.lastIndex))
                        is_ Equal to_ controlBlock)
            }

    @Test
    fun `binary of version 2 has specified payload`() =
            test  when_ {
                binary
            } then {
                expect(it.result.sliceArray(
                        (it.result.lastIndex - 2)..it.result.lastIndex) is_
                        Equal to_ testPayload)
                expect(it.result.sliceArray(
                        (it.result.lastIndex - 4)..(it.result.lastIndex - 3))
                        .getUnsignedShort() is_ Equal to_ testPayload.size)
            }

    @Test
    fun `payload smaller than max payload size`() =
            Given {
                DataChunkStructure.version2(listOf(), testPayload)
            } when_ {
                binary
            } then {
                expect(it.result is_ Equal to_
                        byteArrayOf(
                                VERSION2,
                                DataChunkStructure.Header.Version2.CEND,
                                0x00, testPayload.size.toByte()) +
                        testPayload)
            }

    @Test
    fun `version 2 from binary is as specified`() =
            Given {
                DataChunkStructure.fromBinary(byteArrayOf(
                        VERSION2,
                        DataChunkStructure.Header.Version2.CEND,
                        0x00, 0x03,
                        1, 2, 3))
            } when_ {
                version
            } then {
                expect(it.result is_ Equal to_ VERSION2)
            }

    @Test
    fun `payload from version 2 binary, additional data is ignored`() =
            Given {
                DataChunkStructure.fromBinary(byteArrayOf(
                        VERSION2,
                        DataChunkStructure.Header.Version2.CEND,
                        0x00, 0x03,
                        1, 2, 3, 0xF))
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ byteArrayOf(1, 2, 3))
            }

    @Test
    fun `analyze version 2 binary without control blocks`() =
            Given {
                DataChunkStructure.fromBinary(byteArrayOf(
                        VERSION2,
                        DataChunkStructure.Header.Version2.CEND,
                        0x00, 0x03,
                        1, 2, 3))
            } when_ {
                controlBlocks
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `payload from version 2 binary with control block`() =
            Given {
                DataChunkStructure.fromBinary(binaryChunk)
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ byteArrayOf(4, 5, 6))
            }

    @Test
    fun `control block from version 2 binary`() =
            Given {
                DataChunkStructure.fromBinary(binaryChunk)
            } when_ {
                controlBlocks
            } then {
                expect(it.result is_ Equal to_ Values(controlBlock))
            }

    @Test
    fun `multiple control block from version 2 binary`() {
        val chunkStructure = DataChunkVersion2Builder(0xff).apply {
            publicKey = byteArrayOf(1, 2, 3)
            references += byteArrayOf(4, 5, 6)
            payload = byteArrayOf(7, 8, 9)
        }.chunkStructure

        Given {
            DataChunkStructure.fromBinary(chunkStructure.binary)
        } when_ {
            controlBlocks
        } then {
            expect(it.result is_ Equal to_ chunkStructure.controlBlocks)
        }
    }

    @Test
    fun `version 2 data chunk from a binary with an invalid control block panics`() =
            expect({
                DataChunkStructure.fromBinary(
                        byteArrayOf(VERSION2, 0xff.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00))
            } does Throw.type<Panic>())

    @Test
    fun `version 2 binary data chunk without CEND panics`() =
            expect({ DataChunkStructure.fromBinary(byteArrayOf(VERSION2)) }
                    does Throw.type<Panic>())

    @Test
    fun `version 2 binary data chunk without payload size panics`() =
            expect({ DataChunkStructure.fromBinary(byteArrayOf(VERSION2, 0x00)) }
                    does Throw.type<Panic>())

    @Test
    fun `version 2 binary data chunk with partial payload size panics`() =
            expect({ DataChunkStructure.fromBinary(byteArrayOf(VERSION2, 0x00, 0x00)) }
                    does Throw.type<Panic>())

    @Test
    fun `version 2 binary data chunk with invalid payload size panics`() =
            expect({ DataChunkStructure.fromBinary(byteArrayOf(VERSION2, 0x00, 0x00, 0x01)) }
                    does Throw.type<Panic>())

    @Test
    fun `CEND marker is 0`() =
            expect(DataChunkStructure.Header.Version2.CEND is_ Equal to_ 0)

    @Test
    fun `CEND field size is 1`() =
            expect(DataChunkStructure.Header.Version2.CEND_FIELD_SIZE is_ Equal to_ 1)

    @Test
    fun `PAYLOAD_SIZE_FIELD_SIZE field size is 2`() =
            expect(DataChunkStructure.Header.Version2.PAYLOAD_SIZE_FIELD_SIZE is_ Equal to_ 2)

    @Test
    fun `minimum header size is 4`() =
            expect(DataChunkStructure.Header.Version2.MIN_SIZE is_ Equal to_ 4)
}