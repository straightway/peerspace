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
import straightway.utils.toHexBlocks

class DataChunkVersion0Test : DataChunkStructureBaseTest<DataChunkVersion0>() {

    private companion object {
        const val VERSION0 = 0.toByte()
        const val INVALID_VERSION = 1.toByte()
    }

    override fun createSut(payload: ByteArray) = DataChunkVersion0(payload)

    @Test
    fun `binary of version 0 has version 0`() =
            test when_ {
                binary[0]
            } then {
                expect(it.result is_ Equal to_ VERSION0)
            }

    @Test
    fun `binary of version 0 has only payload after version`() =
            test when_ {
                binary.sliceArray(1..binary.lastIndex)
            } then {
                expect(it.result is_ Equal to_ testPayload)
            }

    @Test
    fun `constructor creates a chunk structure with version 0`() =
            test when_ {
                version
            } then {
                expect(it.result is_ Equal to_ 0)
            }

    @Test
    fun `constructor creates a chunk structure with given payload`() =
            test when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ testPayload)
            }

    @Test
    fun `version from version 0 binary is 0`() =
            Given {
                DataChunkVersion0.fromBinary(byteArrayOf(VERSION0))
            } when_ {
                version
            } then {
                expect(it.result is_ Equal to_ VERSION0)
            }

    @Test
    fun `version 0 binary payload`() =
            Given {
                DataChunkVersion0.fromBinary(byteArrayOf(VERSION0) + testPayload)
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ testPayload)
            }

    @Test
    fun `fromBinary with invalid chunk version panics`() =
            expect({ DataChunkVersion0.fromBinary(byteArrayOf(INVALID_VERSION) + testPayload) }
                    does Throw.type<Panic>())

    @Test
    fun `toString yields proper string representation`() =
            Given {
                DataChunkVersion0(ByteArray(33) { it.toByte() })
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "DataChunkVersion0 {\n" +
                        "  payload (size: 33):\n" +
                        payload.toHexBlocks(32).indent(4) + "\n" +
                        "}")
            }

    @Test
    fun `header size is 1`() =
            expect(DataChunkVersion0.Header.SIZE is_ Equal to_ 1)
}