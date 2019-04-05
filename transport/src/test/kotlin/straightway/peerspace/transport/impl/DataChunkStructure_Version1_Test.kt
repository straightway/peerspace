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
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.toByteArray

@Suppress("ReplaceCallWithBinaryOperator")
class DataChunkStructure_Version1_Test {

    private companion object {
        const val VERSION1 = 1.toByte()
        const val ADDITIONAL_BYTES = 3
        val PAYLOAD = byteArrayOf(1, 2, 3)
    }

    private val test get() =
        Given {
            DataChunkStructure.version1(PAYLOAD, ADDITIONAL_BYTES)
        }

    @Test
    fun `binary of version 1 has version 1`() =
            test when_ {
                binary[0]
            } then {
                expect(it.result is_ Equal to_ VERSION1)
            }

    @Test
    fun `binary of version 1 has after version additional bytes, payload and filled nulls`() =
            test when_ {
                binary.sliceArray(DataChunkStructure.Header.VERSION_FIELD_SIZE..binary.lastIndex)
            } then {
                expect(it.result is_ Equal to_ ADDITIONAL_BYTES.toByteArray().sliceArray(3..3) +
                        PAYLOAD + ByteArray(ADDITIONAL_BYTES) { 0 })
            }

    @Test
    fun `version1 creates a chunk structure with version 1`() =
            test when_ {
                version
            } then {
                expect(it.result is_ Equal to_ 1)
            }

    @Test
    fun `version1 creates a chunk structure without control blocks`() =
            test when_ {
                controlBlocks
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `version1 creates a chunk structure with given payload`() =
            test when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ PAYLOAD)
            }

    @Test
    fun `version from version 1 binary is 1`() =
            Given {
                DataChunkStructure.fromBinary(byteArrayOf(VERSION1, 0, 1, 2, 3))
            } when_ {
                version
            } then {
                expect(it.result is_ Equal to_ VERSION1)
            }

    @Test
    fun `version 1 binary payload without additional bytes`() =
            Given {
                DataChunkStructure.fromBinary(byteArrayOf(VERSION1, 0) + PAYLOAD)
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ PAYLOAD)
            }

    @Test
    fun `version 1 binary payload with few additional bytes`() =
            Given {
                DataChunkStructure.fromBinary(
                        byteArrayOf(VERSION1, 3) + PAYLOAD + ByteArray(3) { 0 })
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ PAYLOAD)
            }

    @Test
    fun `version 1 binary payload with many additional bytes`() =
            Given {
                DataChunkStructure.fromBinary(
                        byteArrayOf(VERSION1, 0xFF.toByte()) + PAYLOAD + ByteArray(0xFF) { 0 })
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ PAYLOAD)
            }

    @Test
    fun `version 1 binary control blocks is empty`() =
            Given {
                DataChunkStructure.fromBinary(byteArrayOf(VERSION1, 0) + PAYLOAD)
            } when_ {
                controlBlocks
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `header size is 2`() =
            expect(DataChunkStructure.Header.Version1.SIZE is_ Equal to_ 2)

    @Test
    fun `additional bytes field size is 1`() =
            expect(DataChunkStructure.Header.Version1.ADDITIONAL_BYTES_FIELD_SIZE is_ Equal to_ 1)

    @Test
    fun `max additional bytes is 255`() =
            expect(DataChunkStructure.Header.Version1.MAX_ADDITIONAL_BYTES is_ Equal to_ 0xff)
}