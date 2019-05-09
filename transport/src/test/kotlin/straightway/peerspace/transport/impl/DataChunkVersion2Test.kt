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
import straightway.utils.joinMultiLine
import straightway.utils.toByteArray

class DataChunkVersion2Test : DataChunkStructureBaseTest<DataChunkVersion2>() {

    private companion object {
        const val VERSION2 = 2.toByte()
        const val CPLS = 0xA.toByte()
        val chunkReference = byteArrayOf(1, 2, 3)
        val controlBlock = DataChunkControlBlock(
                DataChunkControlBlockType.ReferencedChunk,
                CPLS,
                chunkReference)
        val binaryChunk =
                byteArrayOf(VERSION2) +
                // Begin referenced chunk control block
                DataChunkControlBlockType.ReferencedChunk.id +
                ((CPLS.toInt() shl 12) or chunkReference.size).toByteArray().sliceArray(2..3) +
                chunkReference +
                // End referenced chunk control block
                DataChunkVersion2.Header.CEND +
                testPayload.size.toByteArray().sliceArray(2..3) +
                testPayload
    }

    override fun createSut(payload: ByteArray) =
            DataChunkVersion2(listOf(controlBlock), payload)

    @Test
    fun `version is accessible`() =
            test when_ {
                version
            } then {
                expect(it.result is_ Equal to_ VERSION2)
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
                expect(it.result is_ Equal to_ testPayload)
            }

    @Test
    fun `binary has version 2`() =
            test when_ {
                binary[0]
            } then {
                expect(it.result is_ Equal to_ VERSION2)
            }

    @Test
    fun `binary has specified chunk control block`() =
            test when_ {
                binary
            } then {
                expect(DataChunkControlBlock(it.result.sliceArray(1..it.result.lastIndex))
                        is_ Equal to_ controlBlock)
            }

    @Test
    fun `binary has specified payload`() =
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
                DataChunkVersion2(listOf(), testPayload)
            } when_ {
                binary
            } then {
                expect(it.result is_ Equal to_
                        byteArrayOf(
                                VERSION2,
                                DataChunkVersion2.Header.CEND,
                                0x00, testPayload.size.toByte()) +
                        testPayload)
            }

    @Test
    fun `version from binary is as specified`() =
            Given {
                DataChunkVersion2.fromBinary(byteArrayOf(
                        VERSION2,
                        DataChunkVersion2.Header.CEND,
                        0x00, 0x03,
                        1, 2, 3))
            } when_ {
                version
            } then {
                expect(it.result is_ Equal to_ VERSION2)
            }

    @Test
    fun `payload from binary, additional data is ignored`() =
            Given {
                DataChunkVersion2.fromBinary(byteArrayOf(
                        VERSION2,
                        DataChunkVersion2.Header.CEND,
                        0x00, 0x03,
                        1, 2, 3, 0xF))
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ byteArrayOf(1, 2, 3))
            }

    @Test
    fun `analyze binary without control blocks`() =
            Given {
                DataChunkVersion2.fromBinary(byteArrayOf(
                        VERSION2,
                        DataChunkVersion2.Header.CEND,
                        0x00, 0x03,
                        1, 2, 3))
            } when_ {
                controlBlocks
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `payload from binary with control block`() =
            Given {
                DataChunkVersion2.fromBinary(binaryChunk)
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ byteArrayOf(4, 5, 6))
            }

    @Test
    fun `control block from binary`() =
            Given {
                DataChunkVersion2.fromBinary(binaryChunk)
            } when_ {
                controlBlocks
            } then {
                expect(it.result is_ Equal to_ Values(controlBlock))
            }

    @Test
    fun `multiple control block from binary`() {
        val chunkStructure = DataChunkVersion2Builder(0xff).apply {
            publicKey = byteArrayOf(1, 2, 3)
            references += byteArrayOf(4, 5, 6)
            payload = byteArrayOf(7, 8, 9)
        }.chunkStructure

        Given {
            DataChunkVersion2.fromBinary(chunkStructure.binary)
        } when_ {
            controlBlocks
        } then {
            expect(it.result is_ Equal to_ chunkStructure.controlBlocks)
        }
    }

    @Test
    fun `toString yields proper result`() =
            Given {
                DataChunkVersion2(
                        listOf(
                                DataChunkControlBlock(
                                        DataChunkControlBlockType.ReferencedChunk,
                                        0x00,
                                        byteArrayOf(1, 2, 3)),
                                DataChunkControlBlock(
                                        DataChunkControlBlockType.PublicKey,
                                        0x00,
                                        byteArrayOf(4, 5, 6))),
                        ByteArray(33) { it.toByte() })            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "DataChunkVersion2 " +
                        listOf(
                                "control blocks ${this.controlBlocks.joinMultiLine(2)}\n" +
                                "payload (size: 33):\n" +
                                "  00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f " +
                                "10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f\n" +
                                "  20").joinMultiLine(2))
            }

    @Test
    fun `fromBinary with an invalid control block panics`() =
            expect({
                DataChunkVersion2.fromBinary(
                        byteArrayOf(VERSION2, 0xff.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00))
            } does Throw.type<Panic>())

    @Test
    fun `fromBinary data chunk without CEND panics`() =
            expect({ DataChunkVersion2.fromBinary(byteArrayOf(VERSION2)) }
                    does Throw.type<Panic>())

    @Test
    fun `fromBinary without payload size panics`() =
            expect({ DataChunkVersion2.fromBinary(byteArrayOf(VERSION2, 0x00)) }
                    does Throw.type<Panic>())

    @Test
    fun `fromBinary with partial payload size panics`() =
            expect({ DataChunkVersion2.fromBinary(byteArrayOf(VERSION2, 0x00, 0x00)) }
                    does Throw.type<Panic>())

    @Test
    fun `fromBinary with invalid payload size panics`() =
            expect({ DataChunkVersion2.fromBinary(byteArrayOf(VERSION2, 0x00, 0x00, 0x01)) }
                    does Throw.type<Panic>())

    @Test
    fun `CEND marker is 0`() =
            expect(DataChunkVersion2.Header.CEND is_ Equal to_ 0)

    @Test
    fun `CEND field size is 1`() =
            expect(DataChunkVersion2.Header.CEND_FIELD_SIZE is_ Equal to_ 1)

    @Test
    fun `PAYLOAD_SIZE_FIELD_SIZE field size is 2`() =
            expect(DataChunkVersion2.Header.PAYLOAD_SIZE_FIELD_SIZE is_ Equal to_ 2)

    @Test
    fun `minimum header size is 4`() =
            expect(DataChunkVersion2.Header.MIN_SIZE is_ Equal to_ 4)
}