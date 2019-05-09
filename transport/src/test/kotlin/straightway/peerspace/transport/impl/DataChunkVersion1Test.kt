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
import straightway.testing.flow.Equal
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.indent
import straightway.utils.toByteArray
import straightway.utils.toHexBlocks

class DataChunkVersion1Test : DataChunkStructureBaseTest<DataChunkVersion1>() {

    private companion object {
        const val VERSION1 = 1.toByte()
        const val ADDITIONAL_BYTES = 3
    }

    override fun createSut(payload: ByteArray) = DataChunkVersion1(payload, ADDITIONAL_BYTES)

    @Test
    fun `binary of version 1 has version 1`() =
            test when_ {
                binary[0]
            } then {
                expect(it.result is_ Equal to_ VERSION1)
            }

    @Test
    fun `binary  has after version additional bytes, payload and filled nulls`() =
            test when_ {
                binary.sliceArray(DataChunkStructure.VERSION_FIELD_SIZE..binary.lastIndex)
            } then {
                expect(it.result is_ Equal to_ ADDITIONAL_BYTES.toByteArray().sliceArray(3..3) +
                        testPayload + ByteArray(ADDITIONAL_BYTES) { 0 })
            }

    @Test
    fun `constructor creates a chunk structure with version 1`() =
            test when_ {
                version
            } then {
                expect(it.result is_ Equal to_ 1)
            }

    @Test
    fun `constructor creates a chunk structure with given payload`() =
            test when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ testPayload)
            }

    @Test
    fun `fromBinary with wrong chunk version panics`() =
            expect({ DataChunkVersion1.fromBinary(byteArrayOf(0x0, 0, 1, 2, 3)) }
                    does Throw.type<Panic>())

    @Test
    fun `version from binary is 1`() =
            Given {
                DataChunkVersion1.fromBinary(byteArrayOf(VERSION1, 0, 1, 2, 3))
            } when_ {
                version
            } then {
                expect(it.result is_ Equal to_ VERSION1)
            }

    @Test
    fun `payload from binary without additional bytes`() =
            Given {
                DataChunkVersion1.fromBinary(byteArrayOf(VERSION1, 0) + testPayload)
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ testPayload)
            }

    @Test
    fun `payload from binary with few additional bytes`() =
            Given {
                DataChunkVersion1.fromBinary(
                        byteArrayOf(VERSION1, 3) + testPayload + ByteArray(3) { 0 })
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ testPayload)
            }

    @Test
    fun `payload from binary  with many additional bytes`() =
            Given {
                DataChunkVersion1.fromBinary(
                        byteArrayOf(VERSION1, 0xFF.toByte()) + testPayload + ByteArray(0xFF) { 0 })
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ testPayload)
            }

    @Test
    fun `toString yields proper string representation`() =
            Given {
                DataChunkVersion1(ByteArray(33) { it.toByte() }, ADDITIONAL_BYTES)
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "DataChunkVersion1 {\n" +
                        "  payload (size: 33 + $ADDITIONAL_BYTES):\n" +
                        payload.toHexBlocks(32).indent(4) + "\n" +
                        "}")
            }

    @Test
    fun `header size is 2`() =
            expect(DataChunkVersion1.Header.SIZE is_ Equal to_ 2)

    @Test
    fun `additional bytes field size is 1`() =
            expect(DataChunkVersion1.Header.ADDITIONAL_BYTES_FIELD_SIZE is_ Equal to_ 1)

    @Test
    fun `max additional bytes is 255`() =
            expect(DataChunkVersion1.Header.MAX_ADDITIONAL_BYTES is_ Equal to_ 0xff)
}