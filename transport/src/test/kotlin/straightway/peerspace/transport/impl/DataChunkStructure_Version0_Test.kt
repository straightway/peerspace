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

@Suppress("ReplaceCallWithBinaryOperator")
class DataChunkStructure_Version0_Test {

    private companion object {
        const val VERSION0 = 0.toByte()
        val PAYLOAD = byteArrayOf(1, 2, 3)
    }

    private val test get() =
        Given {
            DataChunkStructure.version0(PAYLOAD)
        }

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
                expect(it.result is_ Equal to_ PAYLOAD)
            }

    @Test
    fun `version0 creates a chunk structure with version 0`() =
            test when_ {
                version
            } then {
                expect(it.result is_ Equal to_ 0)
            }

    @Test
    fun `version0 creates a chunk structure without control blocks`() =
            test when_ {
                controlBlocks
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `version0 creates a chunk structure with given payload`() =
            test when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ PAYLOAD)
            }

    @Test
    fun `version from version 0 binary is 0`() =
            Given {
                DataChunkStructure.fromBinary(byteArrayOf(VERSION0))
            } when_ {
                version
            } then {
                expect(it.result is_ Equal to_ VERSION0)
            }

    @Test
    fun `version 0 binary payload`() =
            Given {
                DataChunkStructure.fromBinary(byteArrayOf(VERSION0) + PAYLOAD)
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ PAYLOAD)
            }

    @Test
    fun `version 0 binary control blocks is empty`() =
            Given {
                DataChunkStructure.fromBinary(byteArrayOf(VERSION0) + PAYLOAD)
            } when_ {
                controlBlocks
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `header size is 1`() =
            expect(DataChunkStructure.Header.Version0.SIZE is_ Equal to_ 1)
}